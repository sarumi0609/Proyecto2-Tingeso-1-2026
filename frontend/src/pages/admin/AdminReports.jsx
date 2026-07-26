import React, { useState } from 'react';
import {
  Container, Typography, Box, Button, TextField, Alert, CircularProgress,
  Table, TableHead, TableRow, TableCell, TableBody, Tabs, Tab, Paper
} from '@mui/material';
import { getSalesByPeriod, getPackageRanking } from '../../services/api';

export default function AdminReports() {
  const [tab, setTab] = useState(0);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleGenerate = async () => {
    if (!startDate || !endDate) { setError('Selecciona ambas fechas'); return; }
    setLoading(true);
    setError('');
    try {
      const result = tab === 0
        ? await getSalesByPeriod(startDate, endDate)
        : await getPackageRanking(startDate, endDate);
      setData(result);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al generar reporte');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 6 }}>
      <Typography variant="h5" gutterBottom>Reportes</Typography>

      <Tabs value={tab} onChange={(_, v) => { setTab(v); setData([]); }} sx={{ mb: 3 }}>
        <Tab label="Ventas por período" />
        <Tab label="Ranking de paquetes" />
      </Tabs>

      <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 3 }}>
        <TextField size="small" label="Fecha inicio" type="date" InputLabelProps={{ shrink: true }}
          value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        <TextField size="small" label="Fecha término" type="date" InputLabelProps={{ shrink: true }}
          value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        <Button variant="contained" onClick={handleGenerate} disabled={loading}>
          {loading ? <CircularProgress size={20} /> : 'Generar'}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {data.length > 0 && (
        <Paper>
          {tab === 0 ? (
            // Reporte 1: Ventas por período
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>ID Reserva</TableCell><TableCell>Usuario</TableCell>
                  <TableCell>Paquete</TableCell><TableCell>Pasajeros</TableCell>
                  <TableCell>Monto base</TableCell><TableCell>Descuento</TableCell>
                  <TableCell>Total</TableCell><TableCell>Estado</TableCell><TableCell>Fecha</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell>{r.id}</TableCell>
                    <TableCell sx={{ maxWidth: 100, overflow: 'hidden', textOverflow: 'ellipsis' }}>{r.keycloakUserId}</TableCell>
                    <TableCell>{r.packageId}</TableCell>
                    <TableCell>{r.passengers}</TableCell>
                    <TableCell>${r.baseAmount?.toLocaleString('es-CL')}</TableCell>
                    <TableCell color="success">-${r.discountAmount?.toLocaleString('es-CL')}</TableCell>
                    <TableCell>${r.totalAmount?.toLocaleString('es-CL')}</TableCell>
                    <TableCell>{r.status}</TableCell>
                    <TableCell>{new Date(r.createdAt).toLocaleDateString('es-CL')}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          ) : (
            // Reporte 2: Ranking de paquetes
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>#</TableCell><TableCell>Paquete</TableCell><TableCell>Tipo</TableCell>
                  <TableCell>Destino</TableCell><TableCell>Reservas</TableCell>
                  <TableCell>Pasajeros</TableCell><TableCell>Ingresos</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.map((r, idx) => (
                  <TableRow key={r.packageId}>
                    <TableCell>{idx + 1}</TableCell>
                    <TableCell>{r.packageName}</TableCell>
                    <TableCell>{r.travelType}</TableCell>
                    <TableCell>{r.city ? `${r.city}, ${r.country}` : ''}</TableCell>
                    <TableCell>{r.totalReservations}</TableCell>
                    <TableCell>{r.totalPassengers}</TableCell>
                    <TableCell>${r.totalRevenue?.toLocaleString('es-CL')}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </Paper>
      )}

      {!loading && data.length === 0 && startDate && endDate && (
        <Alert severity="info">No hay datos para el período seleccionado.</Alert>
      )}
    </Container>
  );
}
