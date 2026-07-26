import React, { useEffect, useState } from 'react';
import {
  Container, Typography, Box, Button, TextField, Dialog, DialogTitle,
  DialogContent, DialogActions, Table, TableHead, TableRow, TableCell,
  TableBody, Chip, Alert, CircularProgress, IconButton
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import { getAllPackages, createPackage, updatePackage } from '../../services/api';

const empty = () => ({
  name: '', destination: '', description: '', startDate: '', endDate: '',
  price: '', totalSpots: '', travelType: 'NATIONAL', season: '', category: '',
  services: '', restrictions: '', status: 'AVAILABLE'
});

const statusColor = { AVAILABLE: 'success', SOLD_OUT: 'error', CANCELLED: 'default', NOT_VALID: 'warning' };

export default function AdminPackages() {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(empty());

  const load = () => {
    setLoading(true);
    getAllPackages().then(setPackages).catch(() => setError('Error al cargar')).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const openCreate = () => { setEditing(null); setForm(empty()); setOpen(true); };
  const openEdit = (pkg) => { setEditing(pkg.id); setForm({ ...pkg }); setOpen(true); };

  const handleSave = async () => {
    try {
      if (editing) await updatePackage(editing, form);
      else await createPackage(form);
      setOpen(false);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al guardar');
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 6 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5">Gestión de Paquetes</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>Nuevo paquete</Button>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell><TableCell>Nombre</TableCell><TableCell>Destino</TableCell>
            <TableCell>Precio</TableCell><TableCell>Cupos</TableCell>
            <TableCell>Inicio</TableCell><TableCell>Estado</TableCell><TableCell>Acciones</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {packages.map((pkg) => (
            <TableRow key={pkg.id}>
              <TableCell>{pkg.id}</TableCell>
              <TableCell>{pkg.name}</TableCell>
              <TableCell>{pkg.destination || `${pkg.city}, ${pkg.country}`}</TableCell>
              <TableCell>${pkg.price?.toLocaleString('es-CL')}</TableCell>
              <TableCell>{pkg.availableSpots}/{pkg.totalSpots}</TableCell>
              <TableCell>{pkg.startDate}</TableCell>
              <TableCell><Chip label={pkg.status} color={statusColor[pkg.status] || 'default'} size="small" /></TableCell>
              <TableCell>
                <IconButton size="small" onClick={() => openEdit(pkg)}><EditIcon /></IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>{editing ? 'Editar paquete' : 'Nuevo paquete'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, pt: 1 }}>
            {[['name','Nombre'],['destination','Destino'],['price','Precio','number'],
              ['totalSpots','Cupos totales','number'],['startDate','Fecha inicio','date'],
              ['endDate','Fecha término','date'],['travelType','Tipo viaje'],['season','Temporada'],
              ['category','Categoría']].map(([f, label, type='text']) => (
              <TextField key={f} label={label} type={type} size="small" value={form[f] || ''}
                onChange={(e) => setForm({ ...form, [f]: e.target.value })} />
            ))}
            <TextField label="Descripción" multiline rows={2} size="small" value={form.description || ''}
              onChange={(e) => setForm({ ...form, description: e.target.value })} sx={{ gridColumn: 'span 2' }} />
            <TextField label="Servicios incluidos" size="small" value={form.services || ''}
              onChange={(e) => setForm({ ...form, services: e.target.value })} sx={{ gridColumn: 'span 2' }} />
            <TextField label="Restricciones" size="small" value={form.restrictions || ''}
              onChange={(e) => setForm({ ...form, restrictions: e.target.value })} sx={{ gridColumn: 'span 2' }} />
            <TextField label="Estado" select SelectProps={{ native: true }} size="small" value={form.status || 'AVAILABLE'}
              onChange={(e) => setForm({ ...form, status: e.target.value })}>
              {['AVAILABLE','SOLD_OUT','CANCELLED','NOT_VALID'].map((s) => <option key={s} value={s}>{s}</option>)}
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleSave}>Guardar</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
}
