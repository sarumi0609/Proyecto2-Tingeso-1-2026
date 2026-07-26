import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8090',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'travelagency',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'travelagency-frontend',
});

export default keycloak;
