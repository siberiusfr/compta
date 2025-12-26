# COMPTA Gateway Service

API Gateway pour le système ERP COMPTA utilisant Spring Cloud Gateway avec authentification JWT.

## 📋 Table des Matières

- [Architecture](#architecture)
- [Fonctionnalités](#fonctionnalités)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Déploiement](#déploiement)
- [Monitoring](#monitoring)
- [Sécurité](#sécurité)

## 🏗️ Architecture

### Services Downstream

- **Auth Service** (port 8081) : Authentification et gestion des utilisateurs
- **Authorization Service** (port 8084) : Gestion des permissions
- **Invoice Service** (port 8082) : Gestion des factures
- **Employee Service** (port 8083) : Gestion RH

### Composants Clés

```
Gateway (8080)
├── Security Layer
│   ├── JWT Authentication
│   ├── CORS Configuration
│   └── Security Headers
├── Routing Layer
│   ├── Service Discovery
│   ├── Load Balancing
│   └── Path Rewriting
├── Resilience Layer
│   ├── Circuit Breakers
│   ├── Rate Limiting
│   └── Fallback Controllers
└── Observability Layer
    ├── Request Tracing
    ├── Health Checks
    └── Metrics
```

## ✨ Fonctionnalités

### Sécurité

- ✅ Authentification JWT (OAuth2 Resource Server)
- ✅ Validation des tokens avec signature HMAC-SHA256
- ✅ Headers de sécurité (HSTS, CSP, X-Frame-Options, etc.)
- ✅ CORS configuré par environnement
- ✅ Masquage des données sensibles dans les logs

### Résilience

- ✅ Circuit breakers avec Resilience4j
- ✅ Rate limiting par utilisateur (Redis)
- ✅ Timeouts configurables par service
- ✅ Fallback controllers pour chaque service

### Observabilité

- ✅ Request ID tracking
- ✅ Health checks agrégés des services downstream
- ✅ Métriques Prometheus
- ✅ Logs structurés avec MDC
- ✅ Documentation OpenAPI/Swagger

## 📦 Prérequis

- **Java 21** ou supérieur
- **Maven 3.8+**
- **Redis** (pour le rate limiting)
- Services downstream démarrés

## 🚀 Installation

### 1. Cloner le projet

```bash
git clone https://github.com/votre-organisation/compta-gateway.git
cd compta-gateway
```

### 2. Configurer les variables d'environnement

```bash
# Copier le fichier d'exemple
cp .env.example .env

# Éditer le fichier .env
nano .env
```

### 3. Build du projet

```bash
mvn clean install
```

### 4. Démarrer Redis (via Docker)

```bash
docker run -d -p 6379:6379 --name redis redis:7-alpine
```

### 5. Démarrer l'application

#### Mode développement

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Mode production

```bash
java -jar target/gateway-service-1.0.0.jar --spring.profiles.active=prod
```

## ⚙️ Configuration

### Variables d'environnement essentielles

#### Développement

```properties
# Profil actif
SPRING_PROFILES_ACTIVE=dev

# URLs des services (optionnel en dev, utilise localhost par défaut)
AUTH_SERVICE_URL=http://localhost:8081
AUTHZ_SERVICE_URL=http://localhost:8084
INVOICE_SERVICE_URL=http://localhost:8082
EMPLOYEE_SERVICE_URL=http://localhost:8083

# Redis (optionnel en dev)
REDIS_HOST=localhost
REDIS_PORT=6379
```

#### Production

```properties
# Profil actif
SPRING_PROFILES_ACTIVE=prod

# JWT (OBLIGATOIRE)
JWT_SECRET=your-super-secret-key-minimum-64-characters-for-hs256-algorithm
JWT_EXPIRATION=3600000

# URLs des services (OBLIGATOIRE)
AUTH_SERVICE_URL=https://auth.compta.tn
AUTHZ_SERVICE_URL=https://authz.compta.tn
INVOICE_SERVICE_URL=https://invoices.compta.tn
EMPLOYEE_SERVICE_URL=https://employees.compta.tn

# URL publique de la gateway
GATEWAY_URL=https://api.compta.tn

# Redis (OBLIGATOIRE pour rate limiting)
REDIS_HOST=redis.compta.tn
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
```

### Génération d'un JWT Secret sécurisé

```bash
# Générer un secret de 64 caractères (256 bits)
openssl rand -hex 32
```

## 🚢 Déploiement

### Docker

#### 1. Build de l'image

```bash
docker build -t compta-gateway:1.0.0 .
```

#### 2. Run du container

```bash
docker run -d \
  --name compta-gateway \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET=${JWT_SECRET} \
  -e AUTH_SERVICE_URL=${AUTH_SERVICE_URL} \
  -e REDIS_HOST=${REDIS_HOST} \
  compta-gateway:1.0.0
```

### Docker Compose

```yaml
version: '3.8'

services:
  gateway:
    image: compta-gateway:1.0.0
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JWT_SECRET: ${JWT_SECRET}
      AUTH_SERVICE_URL: ${AUTH_SERVICE_URL}
      AUTHZ_SERVICE_URL: ${AUTHZ_SERVICE_URL}
      INVOICE_SERVICE_URL: ${INVOICE_SERVICE_URL}
      EMPLOYEE_SERVICE_URL: ${EMPLOYEE_SERVICE_URL}
      REDIS_HOST: redis
      REDIS_PORT: 6379
    depends_on:
      - redis
    networks:
      - compta-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - compta-network

networks:
  compta-network:
    driver: bridge
```

### Kubernetes

Voir le dossier `k8s/` pour les manifests Kubernetes.

## 📊 Monitoring

### Endpoints Actuator

```bash
# Health check
curl http://localhost:8080/actuator/health

# Métriques Prometheus
curl http://localhost:8080/actuator/prometheus

# Circuit breakers
curl http://localhost:8080/actuator/circuitbreakers
```

### Health Check Détaillé

```json
{
  "status": "UP",
  "components": {
    "downstreamServices": {
      "status": "UP",
      "details": {
        "auth-service": {
          "name": "auth-service",
          "url": "http://localhost:8081",
          "status": "UP"
        },
        "invoice-service": {
          "name": "invoice-service",
          "url": "http://localhost:8082",
          "status": "UP"
        }
      }
    },
    "circuitBreakers": {
      "status": "UP"
    }
  }
}
```

### Grafana Dashboard

Importez le dashboard fourni dans `monitoring/grafana/gateway-dashboard.json`

## 🔒 Sécurité

### Checklist de Production

- [ ] **JWT Secret** : Minimum 64 caractères, jamais commité dans Git
- [ ] **HTTPS uniquement** : Certificats SSL valides
- [ ] **CORS restreint** : Uniquement vos domaines de production
- [ ] **Swagger désactivé** : `springdoc.swagger-ui.enabled=false`
- [ ] **Logs niveau INFO/WARN** : Pas de DEBUG en production
- [ ] **Actuator sécurisé** : Endpoints limités et authentifiés
- [ ] **Rate limiting actif** : Redis configuré
- [ ] **Security headers** : Tous activés (HSTS, CSP, etc.)
- [ ] **Timeouts configurés** : Pour éviter les blocages
- [ ] **Circuit breakers testés** : Comportement validé

### Headers de Sécurité Appliqués

```http
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
Content-Security-Policy: default-src 'none'; frame-ancestors 'none'
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

### Rate Limiting

- **Développement** : 1000 requêtes/seconde par utilisateur
- **Production** : 100 requêtes/seconde par utilisateur
- Burst capacity : 2x le taux normal

### Circuit Breakers

| Service   | Failure Threshold | Wait Duration | Timeout |
|-----------|-------------------|---------------|---------|
| Auth      | 30%               | 60s           | 10s     |
| Invoice   | 60%               | 30s           | 60s     |
| Default   | 50%               | 30s           | 30s     |

## 📝 Logs

### Format des Logs

```
2024-12-26 10:30:45 [http-nio-8080-exec-1] INFO  tn.compta.gateway.filter.RequestIdFilter - [a1b2c3d4-e5f6-7890] Request ID added
2024-12-26 10:30:45 [http-nio-8080-exec-1] INFO  tn.compta.gateway.filter.SecureLoggingGlobalFilter - [a1b2c3d4-e5f6-7890] 🔵 Request: GET /api/invoices
2024-12-26 10:30:46 [http-nio-8080-exec-1] INFO  tn.compta.gateway.filter.SecureLoggingGlobalFilter - [a1b2c3d4-e5f6-7890] ✅ Response: GET /api/invoices | Status: 200 | Duration: 125ms
```

### Données Masquées

- **JWT tokens** : Masqués dans les logs
- **Emails** : Partiellement masqués (j***@example.com)
- **Passwords** : Jamais loggés
- **API Keys** : Masqués

## 🧪 Tests

```bash
# Tests unitaires
mvn test

# Tests d'intégration
mvn verify

# Tests de charge (optionnel)
k6 run tests/load/gateway-load-test.js
```

## 📚 Documentation API

### Accès Swagger UI

- **Développement** : http://localhost:8080/swagger-ui.html
- **Production** : Désactivé (sécurité)

### OpenAPI Spec

```bash
curl http://localhost:8080/v3/api-docs
```

## 🆘 Troubleshooting

### Problème : Services downstream inaccessibles

```bash
# Vérifier le health check
curl http://localhost:8080/actuator/health

# Vérifier les circuits
curl http://localhost:8080/actuator/circuitbreakers
```

### Problème : JWT invalide

```bash
# Vérifier la configuration JWT
# Le secret doit être identique entre l'auth-service et le gateway
echo $JWT_SECRET | wc -c  # Doit être >= 64
```

### Problème : Rate limiting ne fonctionne pas

```bash
# Vérifier Redis
redis-cli ping  # Doit retourner PONG

# Vérifier la connexion
curl http://localhost:8080/actuator/health | jq .components.redis
```

## 📄 Licence

Propriétaire - COMPTA Team © 2024

## 👥 Support

- **Email** : support@compta.tn
- **Documentation** : https://docs.compta.tn
- **Issues** : https://github.com/votre-organisation/compta-gateway/issues
