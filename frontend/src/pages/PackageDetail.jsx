import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Typography, Box, Button, Chip, CircularProgress, Alert, Divider } from '@mui/material';
import { getPackageById } from '../services/api';

export default function PackageDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [pkg, setPkg] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getPackageById(id).then(setPkg).catch(() => setError('Paquete no encontrado')).finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;
  if (error) return <Container sx={{ mt: 4 }}><Alert severity="error">{error}</Alert></Container>;

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 6 }}>
      <Button onClick={() => navigate(-1)} sx={{ mb: 2 }}>← Volver</Button>
      <Typography variant="h4" gutterBottom>{pkg.name}</Typography>
      <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
        {pkg.travelType && <Chip label={pkg.travelType} color="primary" />}
        {pkg.season && <Chip label={`Temporada: ${pkg.season}`} />}
        {pkg.category && <Chip label={pkg.category} variant="outlined" />}
        <Chip label={pkg.status} color={pkg.status === 'AVAILABLE' ? 'success' : 'default'} />
      </Box>

      <Typography variant="h6" color="text.secondary" gutterBottom>
        {pkg.city ? `${pkg.city}, ${pkg.country}` : pkg.destination}
      </Typography>
      <Divider sx={{ my: 2 }} />
      <Typography variant="body1" gutterBottom>{pkg.description}</Typography>
      <Divider sx={{ my: 2 }} />

      <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, mb: 3 }}>
        <Box>
          <Typography variant="subtitle2" color="text.secondary">Fecha inicio</Typography>
          <Typography>{pkg.startDate}</Typography>
        </Box>
        <Box>
          <Typography variant="subtitle2" color="text.secondary">Fecha término</Typography>
          <Typography>{pkg.endDate}</Typography>
        </Box>
        <Box>
          <Typography variant="subtitle2" color="text.secondary">Cupos disponibles</Typography>
          <Typography>{pkg.availableSpots} / {pkg.totalSpots}</Typography>
        </Box>
        <Box>
          <Typography variant="subtitle2" color="text.secondary">Precio por persona</Typography>
          <Typography variant="h5" color="primary">${pkg.price?.toLocaleString('es-CL')}</Typography>
        </Box>
      </Box>

      {pkg.services && (
        <>
          <Typography variant="subtitle1" fontWeight="bold">Servicios incluidos</Typography>
          <Typography variant="body2" sx={{ mb: 2 }}>{pkg.services}</Typography>
        </>
      )}
      {pkg.restrictions && (
        <>
          <Typography variant="subtitle1" fontWeight="bold">Restricciones</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>{pkg.restrictions}</Typography>
        </>
      )}

      <Button
        variant="contained"
        size="large"
        disabled={pkg.status !== 'AVAILABLE'}
        onClick={() => navigate(`/reserve/${pkg.id}`)}
      >
        {pkg.status === 'AVAILABLE' ? 'Reservar ahora' : 'No disponible'}
      </Button>
    </Container>
  );
}
