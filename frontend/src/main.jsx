import React from 'react';
import ReactDOM from 'react-dom/client';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import keycloak from './keycloak';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')).render(
  <ReactKeycloakProvider
    authClient={keycloak}
    initOptions={{ onLoad: 'check-sso', silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html` }}
  >
    <App />
  </ReactKeycloakProvider>
);
