import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container, Typography, Box, Button, TextField, Alert,
  CircularProgress, Paper, Divider
} from '@mui/material';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import { getReservationById, processPayment } from '../services/api';

export default function Payment() {
  const { reservationId } = useParams();
  const navigate = useNavigate();
  const [reservation, setReservation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [card, setCard] = useState({ cardNumber: '', cardExpirationDate: '', cvv: '' });

  useEffect(() => {
    getReservationById(reservationId).then(setReservation).catch(() => setError('Reserva no encontrada')).finally(() => setLoading(false));
  }, [reservationId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await processPayment(reservationId, card.cardNumber, card.cardExpirationDate, card.cvv);
      setSuccess(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al procesar el pago');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  if (success) return (
    <Container maxWidth="sm" sx={{ mt: 6, textAlign: 'center' }}>
      <Typography variant="h4" color="success.main" gutterBottom>¡Pago exitoso!</Typography>
      <Typography variant="body1" sx={{ mb: 3 }}>Tu reserva ha sido confirmada.</Typography>
      <Button variant="contained" onClick={() => navigate('/my-reservations')}>Ver mis reservas</Button>
    </Container>
  );

  return (
    <Container maxWidth="sm" sx={{ mt: 4, mb: 6 }}>
      <Typography variant="h5" gutterBottom>Pago de reserva</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* Resumen de la reserva */}
      {reservation && (
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="subtitle1" fontWeight="bold" gutterBottom>Resumen de la reserva</Typography>
          <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1 }}>
            <Typography color="text.secondary">Pasajeros</Typography>
            <Typography>{reservation.passengers}</Typography>
            <Typography color="text.secondary">Monto base</Typography>
            <Typography>${reservation.baseAmount?.toLocaleString('es-CL')}</Typography>
            {reservation.discountAmount > 0 && <>
              <Typography color="text.secondary">Descuento aplicado</Typography>
              <Typography color="success.main">-${reservation.discountAmount?.toLocaleString('es-CL')}</Typography>
            </>}
            {reservation.discountDetails && <>
              <Typography color="text.secondary">Detalle descuentos</Typography>
              <Typography variant="caption">{reservation.discountDetails}</Typography>
            </>}
          </Box>
          <Divider sx={{ my: 2 }} />
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h6">Total a pagar</Typography>
            <Typography variant="h5" color="primary">${reservation.totalAmount?.toLocaleString('es-CL')}</Typography>
          </Box>
        </Paper>
      )}

      {/* Formulario de pago simulado */}
      <Paper sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <CreditCardIcon />
          <Typography variant="subtitle1" fontWeight="bold">Datos de tarjeta de crédito (simulado)</Typography>
        </Box>
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField required label="Número de tarjeta" placeholder="1234 5678 9012 3456"
            value={card.cardNumber} onChange={(e) => setCard({ ...card, cardNumber: e.target.value.replace(/\s/g, '') })}
            inputProps={{ maxLength: 16 }} />
          <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
            <TextField required label="Fecha expiración" placeholder="MM/AA"
              value={card.cardExpirationDate} onChange={(e) => setCard({ ...card, cardExpirationDate: e.target.value })} />
            <TextField required label="CVV" placeholder="123"
              value={card.cvv} onChange={(e) => setCard({ ...card, cvv: e.target.value })}
              inputProps={{ maxLength: 4 }} type="password" />
          </Box>
          <Button type="submit" variant="contained" size="large" disabled={submitting}>
            {submitting ? <CircularProgress size={24} /> : `Pagar $${reservation?.totalAmount?.toLocaleString('es-CL')}`}
          </Button>
        </Box>
      </Paper>
    </Container>
  );
}
