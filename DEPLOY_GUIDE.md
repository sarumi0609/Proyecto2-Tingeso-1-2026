# Guía de Despliegue — TravelAgency Microservicios

## Arquitectura

```
Browser → Frontend (NodePort 30000)
Browser → Keycloak (NodePort 30090)   ← login OIDC
Browser → Gateway  (NodePort 30080)   ← todas las llamadas API

Gateway → [Eureka LB] → package-service   (ClusterIP)
                       → search-service    (ClusterIP)
                       → reservation-service (ClusterIP)
                       → payment-service   (ClusterIP)
                       → tracking-service  (ClusterIP)
                       → report-service    (ClusterIP)

Microservicios con DB → PostgreSQL dedicado (ClusterIP headless)
```

---

## PASO 0 — Prerequisitos

- Docker Desktop instalado y corriendo
- Minikube instalado en la VM (VirtualBox)
- kubectl instalado
- Cuenta Docker Hub: `sarumimope`
- Repositorio GitHub con los pipelines configurados

---

## PASO 1 — Subir imágenes a Docker Hub

### Opción A: GitHub Actions (automático)
1. En tu repo GitHub ir a **Settings → Secrets and variables → Actions**
2. Crear secretos:
   - `DOCKERHUB_USERNAME` = `sarumimope`
   - `DOCKERHUB_TOKEN` = (token de Docker Hub)
   - `VITE_GATEWAY_URL` = `http://MINIKUBE_IP:30080`
   - `VITE_KEYCLOAK_URL` = `http://MINIKUBE_IP:30090`
3. Hacer push a `main` → los workflows construyen y publican automáticamente.

### Opción B: Manual (local)
```bash
cd Evaluacion2_microservicios
docker login -u sarumimope

# Construir y publicar cada servicio
for svc in config-service eureka-service gateway-service package-service \
           reservation-service payment-service search-service tracking-service report-service; do
  docker build -t sarumimope/$svc:latest ./$svc
  docker push sarumimope/$svc:latest
done

# Frontend (reemplaza MINIKUBE_IP antes de construir)
cd frontend
docker build -t sarumimope/frontend:latest .
docker push sarumimope/frontend:latest
```

---

## PASO 2 — Push del config-data a GitHub

Los archivos en `config-data/` deben estar en la rama `main` del repo `sarumi0609/Proyecto2-Tingeso-1-2026`:

```bash
git add config-data/
git commit -m "feat: agregar config-data para todos los microservicios"
git push origin main
```

---

## PASO 3 — Levantar Minikube en la VM

```bash
# Dentro de la VM (VirtualBox)
minikube start --driver=virtualbox --memory=6144 --cpus=4

# Verificar
minikube status
kubectl get nodes

# Obtener la IP de minikube (guárdala — la usarás en el frontend y Keycloak)
minikube ip
# Ejemplo: 192.168.49.2
```

---

## PASO 4 — Desplegar en Kubernetes

```bash
# Desde el directorio Evaluacion2_microservicios/k8s/
cd k8s

# Aplicar en orden (respetar el orden de los archivos)
kubectl apply -f 00-secrets.yaml
kubectl apply -f 01-postgres-package.yaml
kubectl apply -f 02-postgres-reservation.yaml
kubectl apply -f 03-postgres-payment.yaml
kubectl apply -f 04-keycloak.yaml
kubectl apply -f 05-config-service.yaml

# Esperar a que config-service esté listo antes de continuar
kubectl wait --for=condition=ready pod -l app=config-service --timeout=120s

kubectl apply -f 06-eureka-service.yaml

# Esperar a Eureka
kubectl wait --for=condition=ready pod -l app=eureka-service --timeout=120s

kubectl apply -f 07-gateway-service.yaml
kubectl apply -f 08-package-service.yaml
kubectl apply -f 09-reservation-service.yaml
kubectl apply -f 10-payment-service.yaml
kubectl apply -f 11-search-service.yaml
kubectl apply -f 12-tracking-service.yaml
kubectl apply -f 13-report-service.yaml
kubectl apply -f 14-frontend.yaml

# Verificar todo
kubectl get pods
kubectl get services
```

---

## PASO 5 — Configurar Keycloak (M1)

Esperar a que Keycloak esté listo (~2 min), luego abrir en el browser:

```
http://MINIKUBE_IP:30090
```

### 5.1 Crear Realm
1. Ir a **Administration Console** → login con `admin` / `admin`
2. Click en **Create Realm**
3. Nombre: `travelagency`
4. **Create**

### 5.2 Crear Client (frontend)
1. En realm `travelagency` → **Clients** → **Create client**
2. Client ID: `travelagency-frontend`
3. Client type: `OpenID Connect`
4. **Next** → habilitar **Standard flow** y **Direct access grants**
5. **Next** → configurar:
   - Valid redirect URIs: `http://MINIKUBE_IP:30000/*`
   - Web origins: `http://MINIKUBE_IP:30000`
6. **Save**

### 5.3 Crear Roles
1. **Realm roles** → **Create role**
2. Crear rol: `ADMIN`
3. Crear rol: `USER`

### 5.4 Crear Usuarios
**Usuario admin:**
1. **Users** → **Create user**
   - Username: `admin`
   - Email: `admin@travelagency.com`
2. **Credentials** → Set password: `admin123` (desactivar "Temporary")
3. **Role mappings** → asignar rol `ADMIN`

**Usuario cliente:**
1. **Users** → **Create user**
   - Username: `cliente1`
   - Email: `cliente1@travelagency.com`
2. **Credentials** → Set password: `cliente123`
3. **Role mappings** → asignar rol `USER`

---

## PASO 6 — Actualizar Frontend con IP de Minikube

**Antes de hacer push del frontend**, actualizar `.env.production`:

```bash
VITE_GATEWAY_URL=http://192.168.49.2:30080
VITE_KEYCLOAK_URL=http://192.168.49.2:30090
VITE_KEYCLOAK_REALM=travelagency
VITE_KEYCLOAK_CLIENT_ID=travelagency-frontend
```

Reconstruir y subir:
```bash
cd frontend
docker build -t sarumimope/frontend:latest .
docker push sarumimope/frontend:latest
kubectl rollout restart deployment frontend
```

---

## PASO 7 — Verificar el despliegue

```bash
# Ver todos los pods
kubectl get pods -o wide

# Ver servicios y puertos
kubectl get services

# Logs de un pod específico (útil para debugging)
kubectl logs deployment/config-service
kubectl logs deployment/eureka-service
kubectl logs deployment/gateway-service

# Ver Eureka dashboard (dentro del cluster)
kubectl port-forward svc/eureka-service 8761:8761
# Abrir: http://localhost:8761
```

**URLs de acceso:**
- **Frontend**: `http://MINIKUBE_IP:30000`
- **Keycloak**: `http://MINIKUBE_IP:30090`
- **Gateway** (API): `http://MINIKUBE_IP:30080`

---

## PASO 8 — Comandos útiles para debugging

```bash
# Ver eventos de un pod que no inicia
kubectl describe pod <pod-name>

# Reiniciar un deployment
kubectl rollout restart deployment <nombre>

# Ver todos los recursos
kubectl get all

# Eliminar todo y volver a empezar
kubectl delete -f k8s/

# Escalar un servicio
kubectl scale deployment package-service --replicas=2
```

---

## Verificación de comunicación entre servicios

El gateway usa Eureka para resolver los microservicios. Los microservicios se comunican
directamente por IP de pod (Eureka con `prefer-ip-address: true`), NO por el API Gateway.
Solo el frontend habla con el gateway.

```
Frontend (NodePort:30000) 
  ↓ peticiones API
Gateway (NodePort:30080)
  ↓ Eureka load balancing
Microservicios (ClusterIP) ←→ PostgreSQL (Headless ClusterIP)
```

---

## Secretos de GitHub Actions a configurar

| Secret | Valor |
|--------|-------|
| `DOCKERHUB_USERNAME` | `sarumimope` |
| `DOCKERHUB_TOKEN` | Token generado en Docker Hub |
| `VITE_GATEWAY_URL` | `http://MINIKUBE_IP:30080` |
| `VITE_KEYCLOAK_URL` | `http://MINIKUBE_IP:30090` |
