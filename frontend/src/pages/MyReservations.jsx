import React, { useEffect, useState } from 'react';
import {
  Container, Typography, Box, Button, Chip, CircularProgress,
  Alert, Card, CardContent, CardActions, Divider
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { getMyReservations, cancelReservation } from '../services/api';

const statusColor = { CONFIRMED: 'success', PENDING: 'warning', CANCELLED: 'default', EXPIRED: 'error' };

export default function MyReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const load = () => {
    setLoading(true);
    getMyReservations().then(setReservations).catch(() => setError('Error al cargar reservas')).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleCancel = async (id) => {
    if (!confirm('¿Seguro que deseas cancelar esta reserva?')) return;
    try {
      await cancelReservation(id);
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Error al cancelar');
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 6 }}>
      <Typography variant="h5" gutterBottom>Mis Reservas</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      {reservations.length === 0 ? (
        <Alert severity="info">No tienes reservas aún. <Button onClick={() => navigate('/')}>Explorar paquetes</Button></Alert>
      ) : reservations.map((r) => (
        <Card key={r.id} sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Typography variant="h6">Reserva #{r.id} — Paquete #{r.packageId}</Typography>
              <Chip label={r.status} color={statusColor[r.status] || 'default'} />
            </Box>
            <Divider sx={{ my: 1 }} />
            <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 1 }}>
              <Box>
                <Typography variant="caption" color="text.secondary">Pasajeros</Typography>
                <Typography>{r.passengers}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Total pagado</Typography>
                <Typography color="primary">${r.totalAmount?.toLocaleString('es-CL')}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Fecha reserva</Typography>
                <Typography>{new Date(r.createdAt).toLocaleDateString('es-CL')}</Typography>
              </Box>
            </Box>
          </CardContent>
          <CardActions>
            <Button size="small" onClick={() => navigate(`/reservations/${r.id}`)}>Ver detalle</Button>
            {r.status === 'PENDING' && (
              <Button size="small" onClick={() => navigate(`/payment/${r.id}`)}>Pagar</Button>
            )}
            {(r.status === 'PENDING' || r.status === 'CONFIRMED') && (
              <Button size="small" color="error" onClick={() => handleCancel(r.id)}>Cancelar</Button>
            )}
          </CardActions>
        </Card>
      ))}
    </Container>
  );
}
