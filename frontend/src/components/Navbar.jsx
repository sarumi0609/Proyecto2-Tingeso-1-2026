import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import FlightIcon from '@mui/icons-material/Flight';

export default function Navbar() {
  const { keycloak } = useKeycloak();
  const navigate = useNavigate();
  const isAdmin = keycloak.tokenParsed?.realm_access?.roles?.includes('ADMIN');

  return (
    <AppBar position="sticky">
      <Toolbar>
        <FlightIcon sx={{ mr: 1 }} />
        <Typography variant="h6" sx={{ cursor: 'pointer', flexGrow: 1 }} onClick={() => navigate('/')}>
          TravelAgency
        </Typography>

        {keycloak.authenticated ? (
          <>
            <Button color="inherit" onClick={() => navigate('/my-reservations')}>Mis reservas</Button>
            {isAdmin && (
              <Box>
                <Button color="inherit" onClick={() => navigate('/admin/packages')}>Paquetes</Button>
                <Button color="inherit" onClick={() => navigate('/admin/reservations')}>Reservas</Button>
                <Button color="inherit" onClick={() => navigate('/admin/reports')}>Reportes</Button>
              </Box>
            )}
            <Button color="inherit" onClick={() => keycloak.logout()}>
              Cerrar sesión ({keycloak.tokenParsed?.preferred_username})
            </Button>
          </>
        ) : (
          <Button color="inherit" onClick={() => keycloak.login()}>Iniciar sesión</Button>
        )}
      </Toolbar>
    </AppBar>
  );
}
