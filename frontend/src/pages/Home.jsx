import React, { useState, useEffect } from 'react';
import {
  Container, Grid, Card, CardContent, CardActions, Typography,
  Button, TextField, Box, Chip, CircularProgress, Alert
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { useNavigate } from 'react-router-dom';
import { searchPackages } from '../services/api';

export default function Home() {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({ destination: '', minPrice: '', maxPrice: '', type: '' });
  const navigate = useNavigate();

  const load = (params = {}) => {
    setLoading(true);
    setError('');
    searchPackages(params)
      .then(setPackages)
      .catch(() => setError('Error al cargar paquetes'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    const params = {};
    if (filters.destination) params.destination = filters.destination;
    if (filters.minPrice) params.minPrice = filters.minPrice;
    if (filters.maxPrice) params.maxPrice = filters.maxPrice;
    if (filters.type) params.type = filters.type;
    load(params);
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 6 }}>
      <Typography variant="h4" gutterBottom>Paquetes Turísticos</Typography>

      {/* Buscador */}
      <Box component="form" onSubmit={handleSearch} sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 4 }}>
        <TextField size="small" label="Destino" value={filters.destination}
          onChange={(e) => setFilters({ ...filters, destination: e.target.value })} />
        <TextField size="small" label="Precio mín." type="number" value={filters.minPrice}
          onChange={(e) => setFilters({ ...filters, minPrice: e.target.value })} />
        <TextField size="small" label="Precio máx." type="number" value={filters.maxPrice}
          onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })} />
        <TextField size="small" label="Tipo (NATIONAL/INTERNATIONAL)" value={filters.type}
          onChange={(e) => setFilters({ ...filters, type: e.target.value })} />
        <Button type="submit" variant="contained" startIcon={<SearchIcon />}>Buscar</Button>
        <Button variant="outlined" onClick={() => { setFilters({ destination: '', minPrice: '', maxPrice: '', type: '' }); load(); }}>
          Limpiar
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 6 }}><CircularProgress /></Box>
      ) : (
        <Grid container spacing={3}>
          {packages.length === 0 ? (
            <Grid item xs={12}><Alert severity="info">No se encontraron paquetes con esos criterios.</Alert></Grid>
          ) : packages.map((pkg) => (
            <Grid item xs={12} sm={6} md={4} key={pkg.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" gutterBottom>{pkg.name}</Typography>
                  <Typography color="text.secondary" gutterBottom>
                    {pkg.destination || `${pkg.city}, ${pkg.country}`}
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>{pkg.description}</Typography>
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 1 }}>
                    {pkg.travelType && <Chip label={pkg.travelType} size="small" color="primary" variant="outlined" />}
                    {pkg.season && <Chip label={pkg.season} size="small" />}
                    {pkg.availableSpots !== undefined && (
                      <Chip label={`${pkg.availableSpots} cupos`} size="small" color="success" variant="outlined" />
                    )}
                  </Box>
                  <Typography variant="h5" color="primary" sx={{ mt: 2 }}>
                    ${pkg.price?.toLocaleString('es-CL')}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {pkg.startDate} → {pkg.endDate}
                  </Typography>
                </CardContent>
                <CardActions>
                  <Button size="small" onClick={() => navigate(`/packages/${pkg.id}`)}>Ver detalle</Button>
                  <Button size="small" variant="contained" onClick={() => navigate(`/reserve/${pkg.id}`)}>
                    Reservar
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Container>
  );
}
