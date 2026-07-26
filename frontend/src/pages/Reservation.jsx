import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container, Typography, Box, Button, TextField, Alert,
  CircularProgress, Divider, IconButton, Paper
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import { getPackageById, createReservation } from '../services/api';

const emptyPassenger = () => ({ name: '', lastName: '', documentId: '', nationality: '' });

export default function Reservation() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [pkg, setPkg] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [passengers, setPassengers] = useState(1);
  const [passengerList, setPassengerList] = useState([emptyPassenger()]);

  useEffect(() => {
    getPackageById(id).then(setPkg).catch(() => setError('Paquete no encontrado')).finally(() => setLoading(false));
  }, [id]);

  const changeCount = (delta) => {
    const next = Math.max(1, passengers + delta);
    setPassengers(next);
    setPassengerList((prev) => {
      const arr = [...prev];
      while (arr.length < next) arr.push(emptyPassenger());
      return arr.slice(0, next);
    });
  };

  const updatePassenger = (idx, field, value) => {
    setPassengerList((prev) => prev.map((p, i) => i === idx ? { ...p, [field]: value } : p));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      const res = await createReservation(id, passengers, passengerList);
      navigate(`/payment/${res.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear la reserva');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  return (
    <Container maxWidth="sm" sx={{ mt: 4, mb: 6 }}>
      <Button onClick={() => navigate(-1)} sx={{ mb: 2 }}>← Volver</Button>
      <Typography variant="h5" gutterBottom>Reservar: {pkg?.name}</Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="subtitle2" color="text.secondary">Precio por persona</Typography>
        <Typography variant="h5" color="primary">${pkg?.price?.toLocaleString('es-CL')}</Typography>
        <Divider sx={{ my: 2 }} />

        <Typography variant="subtitle1" gutterBottom>Cantidad de pasajeros</Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
          <IconButton onClick={() => changeCount(-1)} disabled={passengers <= 1}><RemoveIcon /></IconButton>
          <Typography variant="h6">{passengers}</Typography>
          <IconButton onClick={() => changeCount(1)} disabled={passengers >= (pkg?.availableSpots || 1)}>
            <AddIcon />
          </IconButton>
        </Box>

        <Typography variant="h6" color="primary" gutterBottom>
          Total estimado: ${((pkg?.price || 0) * passengers).toLocaleString('es-CL')}
          <Typography variant="caption" color="text.secondary" sx={{ ml: 1 }}>
            (descuentos se aplican al confirmar)
          </Typography>
        </Typography>
      </Paper>

      <Typography variant="h6" gutterBottom>Datos de los pasajeros</Typography>
      <Box component="form" onSubmit={handleSubmit}>
        {passengerList.map((p, idx) => (
          <Paper key={idx} sx={{ p: 2, mb: 2 }}>
            <Typography variant="subtitle2" gutterBottom>Pasajero {idx + 1}</Typography>
            <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
              <TextField required size="small" label="Nombre" value={p.name}
                onChange={(e) => updatePassenger(idx, 'name', e.target.value)} />
              <TextField required size="small" label="Apellido" value={p.lastName}
                onChange={(e) => updatePassenger(idx, 'lastName', e.target.value)} />
              <TextField required size="small" label="RUT/Pasaporte" value={p.documentId}
                onChange={(e) => updatePassenger(idx, 'documentId', e.target.value)} />
              <TextField size="small" label="Nacionalidad" value={p.nationality}
                onChange={(e) => updatePassenger(idx, 'nationality', e.target.value)} />
            </Box>
          </Paper>
        ))}

        <Button type="submit" variant="contained" fullWidth size="large" disabled={submitting}>
          {submitting ? <CircularProgress size={24} /> : 'Confirmar reserva'}
        </Button>
      </Box>
    </Container>
  );
}
