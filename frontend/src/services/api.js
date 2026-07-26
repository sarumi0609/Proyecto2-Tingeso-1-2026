import axios from 'axios';
import keycloak from '../keycloak';

const GATEWAY = import.meta.env.VITE_GATEWAY_URL || '';

const api = axios.create({ baseURL: GATEWAY });

// Adjuntar token JWT a cada petición si el usuario está autenticado
api.interceptors.request.use((config) => {
  if (keycloak.authenticated && keycloak.token) {
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  }
  return config;
});

// ── Paquetes (M2 — package-service) ─────────────────────────
export const getAvailablePackages = () =>
  api.get('/api/touristpackages/available').then((r) => r.data);

export const getAllPackages = () =>
  api.get('/api/touristpackages').then((r) => r.data);

export const getPackageById = (id) =>
  api.get(`/api/touristpackages/${id}`).then((r) => r.data);

export const createPackage = (pkg) =>
  api.post('/api/touristpackages', pkg).then((r) => r.data);

export const updatePackage = (id, pkg) =>
  api.put(`/api/touristpackages/${id}`, pkg).then((r) => r.data);

export const deletePackage = (id) =>
  api.delete(`/api/touristpackages/${id}`);

// ── Búsqueda (M3 — search-service) ──────────────────────────
export const searchPackages = (params) =>
  api.get('/api/search', { params }).then((r) => r.data);

// ── Reservas (M4 — reservation-service) ─────────────────────
export const createReservation = (packageId, passengers, passengerList) =>
  api.post('/api/reservations', passengerList, {
    params: { packageId, passengers },
  }).then((r) => r.data);

export const getMyReservations = () =>
  api.get('/api/reservations/my').then((r) => r.data);

export const getAllReservations = () =>
  api.get('/api/reservations').then((r) => r.data);

export const getReservationById = (id) =>
  api.get(`/api/reservations/${id}`).then((r) => r.data);

export const cancelReservation = (id) =>
  api.patch(`/api/reservations/${id}/cancel`).then((r) => r.data);

export const getReceipt = (id) =>
  api.get(`/api/reservations/${id}/receipt`).then((r) => r.data);

// ── Pagos (M5 — payment-service) ────────────────────────────
export const processPayment = (reservationId, cardNumber, cardExpirationDate, cvv) =>
  api.post('/api/payments', null, {
    params: { reservationId, cardNumber, cardExpirationDate, cvv },
  }).then((r) => r.data);

export const getPaymentByReservation = (reservationId) =>
  api.get(`/api/payments/reservation/${reservationId}`).then((r) => r.data);

// ── Seguimiento (M6 — tracking-service) ─────────────────────
export const getReservationStatus = (reservationId) =>
  api.get(`/api/tracking/${reservationId}`).then((r) => r.data);

// ── Reportes (M7 — report-service) ──────────────────────────
export const getSalesByPeriod = (startDate, endDate) =>
  api.get('/api/reports/sales', { params: { startDate, endDate } }).then((r) => r.data);

export const getPackageRanking = (startDate, endDate) =>
  api.get('/api/reports/ranking', { params: { startDate, endDate } }).then((r) => r.data);

// ── Descuentos (admin — package-service) ────────────────────
export const getDiscounts = () =>
  api.get('/api/discounts').then((r) => r.data);

export const createDiscount = (discount) =>
  api.post('/api/discounts', discount).then((r) => r.data);

export const getPromotions = () =>
  api.get('/api/promotions').then((r) => r.data);

export const createPromotion = (promo) =>
  api.post('/api/promotions', promo).then((r) => r.data);
