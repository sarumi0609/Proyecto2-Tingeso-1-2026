import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Typography, Box, Button, Chip, CircularProgress, Alert, Divider, Paper } from '@mui/material';
import { getReservationById, getReceipt } from '../services/api';

const statusColor = { CONFIRMED: 'success', PENDING: 'warning', CANCELLED: 'default', EXPIRED: 'error' };

export default function ReservationDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [reservation, setReservation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getReservationById(id).then(setReservation).catch(() => setError('Reserva no encontrada')).finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;
  if (error) return <Container sx={{ mt: 4 }}><Alert severity="error">{error}</Alert></Container>;

  return (
    <Container maxWidth="sm" sx={{ mt: 4, mb: 6 }}>
      <Button onClick={() => navigate(-1)} sx={{ mb: 2 }}>← Volver</Button>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5">Reserva #{reservation.id}</Typography>
        <Chip label={reservation.status} color={statusColor[reservation.status] || 'default'} />
      </Box>

      <Paper sx={{ p: 3 }}>
        <Typography variant="subtitle1" fontWeight="bold" gutterBottom>Detalles</Typography>
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1.5 }}>
          <Typography color="text.secondary">Paquete ID</Typography><Typography>{reservation.packageId}</Typography>
          <Typography color="text.secondary">Pasajeros</Typography><Typography>{reservation.passengers}</Typography>
          <Typography color="text.secondary">Monto base</Typography><Typography>${reservation.baseAmount?.toLocaleString('es-CL')}</Typography>
          <Typography color="text.secondary">Descuento</Typography><Typography color="success.main">-${reservation.discountAmount?.toLocaleString('es-CL')}</Typography>
          <Typography color="text.secondary">Total</Typography><Typography variant="h6" color="primary">${reservation.totalAmount?.toLocaleString('es-CL')}</Typography>
          <Typography color="text.secondary">Fecha creación</Typography><Typography>{new Date(reservation.createdAt).toLocaleString('es-CL')}</Typography>
        </Box>

        {reservation.discountDetails && (
          <>
            <Divider sx={{ my: 2 }} />
            <Typography variant="subtitle2">Detalle de descuentos aplicados</Typography>
            <Typography variant="body2" color="text.secondary">{reservation.discountDetails}</Typography>
          </>
        )}
      </Paper>

      {reservation.status === 'PENDING' && (
        <Button variant="contained" fullWidth sx={{ mt: 2 }} onClick={() => navigate(`/payment/${id}`)}>
          Proceder al pago
        </Button>
      )}
    </Container>
  );
}
