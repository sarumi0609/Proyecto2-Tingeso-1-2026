import React, { useEffect, useState } from 'react';
import {
  Container, Typography, Box, Table, TableHead, TableRow, TableCell,
  TableBody, Chip, Alert, CircularProgress, TextField, Button
} from '@mui/material';
import { getAllReservations, cancelReservation } from '../../services/api';

const statusColor = { CONFIRMED: 'success', PENDING: 'warning', CANCELLED: 'default', EXPIRED: 'error' };

export default function AdminReservations() {
  const [reservations, setReservations] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  const load = () => {
    setLoading(true);
    getAllReservations()
      .then((data) => { setReservations(data); setFiltered(data); })
      .catch(() => setError('Error al cargar reservas'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  useEffect(() => {
    const q = search.toLowerCase();
    setFiltered(reservations.filter((r) =>
      r.id.toString().includes(q) ||
      r.keycloakUserId?.toLowerCase().includes(q) ||
      r.status?.toLowerCase().includes(q)
    ));
  }, [search, reservations]);

  const handleCancel = async (id) => {
    if (!confirm('¿Cancelar esta reserva?')) return;
    try { await cancelReservation(id); load(); }
    catch (err) { alert(err.response?.data?.message || 'Error al cancelar'); }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 6 }}>
      <Typography variant="h5" gutterBottom>Todas las Reservas</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <TextField size="small" label="Buscar por ID, usuario o estado" value={search}
        onChange={(e) => setSearch(e.target.value)} sx={{ mb: 2, width: 350 }} />

      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell><TableCell>Usuario</TableCell><TableCell>Paquete</TableCell>
            <TableCell>Pasajeros</TableCell><TableCell>Total</TableCell><TableCell>Descuento</TableCell>
            <TableCell>Fecha</TableCell><TableCell>Estado</TableCell><TableCell>Acciones</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {filtered.map((r) => (
            <TableRow key={r.id}>
              <TableCell>{r.id}</TableCell>
              <TableCell sx={{ maxWidth: 150, overflow: 'hidden', textOverflow: 'ellipsis' }}>{r.keycloakUserId}</TableCell>
              <TableCell>{r.packageId}</TableCell>
              <TableCell>{r.passengers}</TableCell>
              <TableCell>${r.totalAmount?.toLocaleString('es-CL')}</TableCell>
              <TableCell>${r.discountAmount?.toLocaleString('es-CL')}</TableCell>
              <TableCell>{new Date(r.createdAt).toLocaleDateString('es-CL')}</TableCell>
              <TableCell><Chip label={r.status} color={statusColor[r.status] || 'default'} size="small" /></TableCell>
              <TableCell>
                {(r.status === 'PENDING' || r.status === 'CONFIRMED') && (
                  <Button size="small" color="error" onClick={() => handleCancel(r.id)}>Cancelar</Button>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Container>
  );
}
