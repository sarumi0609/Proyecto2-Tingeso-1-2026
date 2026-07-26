import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import { CircularProgress, Box } from '@mui/material';

import Navbar from './components/Navbar';
import Home from './pages/Home';
import PackageDetail from './pages/PackageDetail';
import Reservation from './pages/Reservation';
import Payment from './pages/Payment';
import MyReservations from './pages/MyReservations';
import ReservationDetail from './pages/ReservationDetail';
import AdminPackages from './pages/admin/AdminPackages';
import AdminReservations from './pages/admin/AdminReservations';
import AdminReports from './pages/admin/AdminReports';

// Rutas protegidas: redirige al login si no está autenticado
const PrivateRoute = ({ children, adminOnly = false }) => {
  const { keycloak, initialized } = useKeycloak();
  if (!initialized) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;
  if (!keycloak.authenticated) { keycloak.login(); return null; }
  if (adminOnly) {
    const roles = keycloak.tokenParsed?.realm_access?.roles || [];
    if (!roles.includes('ADMIN')) return <Navigate to="/" />;
  }
  return children;
};

export default function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        {/* Rutas públicas */}
        <Route path="/" element={<Home />} />
        <Route path="/packages/:id" element={<PackageDetail />} />

        {/* Rutas autenticadas */}
        <Route path="/reserve/:id" element={<PrivateRoute><Reservation /></PrivateRoute>} />
        <Route path="/payment/:reservationId" element={<PrivateRoute><Payment /></PrivateRoute>} />
        <Route path="/my-reservations" element={<PrivateRoute><MyReservations /></PrivateRoute>} />
        <Route path="/reservations/:id" element={<PrivateRoute><ReservationDetail /></PrivateRoute>} />

        {/* Rutas de administrador */}
        <Route path="/admin/packages" element={<PrivateRoute adminOnly><AdminPackages /></PrivateRoute>} />
        <Route path="/admin/reservations" element={<PrivateRoute adminOnly><AdminReservations /></PrivateRoute>} />
        <Route path="/admin/reports" element={<PrivateRoute adminOnly><AdminReports /></PrivateRoute>} />
      </Routes>
    </BrowserRouter>
  );
}
