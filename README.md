# COMPTA ERP

Application ERP de comptabilité complète basée sur une architecture microservices.

## Architecture

### Services Backend

- **API Gateway** (Spring Cloud Gateway) - Port 8080
- **Auth Service** (Spring Boot) - Gestion utilisateurs, entreprises, rôles et permissions
- **Accounting Service** (Spring Boot) - Plan comptable, journaux, écritures, TVA
- **Document Service** (Spring Boot) - GED avec MinIO, OCR, versioning
- **HR Service** (Spring Boot) - Employés, contrats, paies, congés
- **Notification Service** (NestJS) - Emails, SMS, push notifications
- **Migration Service** (Spring Boot + Flyway) - Gestion centralisée des migrations DB

### Stack Technique

- **Backend**: Java 21, Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **Frontend**: React 18, TypeScript, Vite, TailwindCSS, shadcn/ui
- **Base de données**: PostgreSQL 16
- **Message Broker**: RabbitMQ 4.2
- **Cache**: Redis 8.4
- **Stockage**: MinIO
- **API Documentation**: OpenAPI 3.0 (Swagger)

## Structure du Projet

```
compta/
├── gateway/                      # API Gateway (Spring Cloud Gateway)
├── compta-commons/              # Bibliothèque partagée
├── services/
│   ├── migration-service/       # Service de migration Flyway
│   ├── auth-service/            # Service d'authentification
│   ├── accounting-service/      # Service de comptabilité
│   ├── document-service/        # Service de gestion documentaire
│   ├── hr-service/              # Service RH
│   └── notification-service/    # Service de notifications (NestJS)
├── frontend/                    # Application React
├── docker/                      # Configuration Docker
├── docker-compose.yml
└── README.md
```

## Prérequis

- Java 21 (JDK)
- Node.js 20+ & npm
- Docker & Docker Compose
- Maven 3.9+

## Installation & Démarrage

### 1. Configuration de l'environnement

Copier le fichier `.env.example` vers `.env` et configurer les variables:

```bash
cp .env.example .env
```

### 2. Build des services

#### Build de compta-commons (requis en premier)

```bash
cd compta-commons
mvn clean install
cd ..
```

#### Build de tous les services Spring Boot

```bash
# Gateway
cd gateway && mvn clean package && cd ..

# Migration Service
cd services/migration-service && mvn clean package && cd ../..

# Auth Service
cd services/auth-service && mvn clean package && cd ../..

# Accounting Service
cd services/accounting-service && mvn clean package && cd ../..

# Document Service
cd services/document-service && mvn clean package && cd ../..

# HR Service
cd services/hr-service && mvn clean package && cd ../..
```

#### Build du Notification Service (NestJS)

```bash
cd services/notification-service
npm install
npm run build
cd ../..
```

#### Build du Frontend

```bash
cd frontend
npm install
npm run build
cd ..
```

### 3. Démarrage avec Docker Compose

```bash
docker-compose up -d
```

Les services démarrent dans l'ordre suivant:
1. PostgreSQL, RabbitMQ, MinIO, Redis
2. Migration Service (exécute les migrations puis s'arrête)
3. Tous les autres services backend
4. Frontend

### 4. Accès aux interfaces

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)

## Endpoints Principaux

### Auth Service (via Gateway)

- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion (retourne JWT)
- `GET /api/auth/me` - Profil utilisateur
- `GET /api/auth/companies` - Liste des entreprises de l'utilisateur

### Accounting Service

- `GET /api/accounting/accounts` - Plan comptable
- `GET /api/accounting/journals` - Journaux
- `POST /api/accounting/entries` - Créer une écriture
- `GET /api/accounting/balance` - Balance comptable
- `GET /api/accounting/ledger` - Grand livre

### Document Service

- `POST /api/documents/upload` - Upload document
- `GET /api/documents/{id}` - Télécharger document
- `GET /api/documents` - Liste documents
- `POST /api/documents/{id}/share` - Partager document

### HR Service

- `GET /api/hr/employees` - Liste employés
- `POST /api/hr/employees` - Créer employé
- `GET /api/hr/payslips` - Fiches de paie
- `POST /api/hr/leave-requests` - Demande de congé

## Base de Données

### Connexion

```
Host: localhost
Port: 5432
Database: compta_db
User: compta_user
Password: (voir .env)
```

### Migrations

Les migrations Flyway sont gérées par le `migration-service`. Pour exécuter manuellement:

```bash
# Voir le statut des migrations
curl http://localhost:8081/api/migration/status

# Exécuter les migrations
curl -X POST http://localhost:8081/api/migration/migrate

# Historique des migrations
curl http://localhost:8081/api/migration/history
```

## Développement

### Lancer un service en mode développement

#### Services Spring Boot

```bash
cd services/auth-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Notification Service (NestJS)

```bash
cd services/notification-service
npm run start:dev
```

#### Frontend

```bash
cd frontend
npm run dev
```

### Tests

#### Tests unitaires (Spring Boot)

```bash
cd services/auth-service
mvn test
```

#### Tests E2E (Frontend)

```bash
cd frontend
npm run test
```

## Multi-Tenancy

L'application supporte le multi-tenant au niveau entreprise:
- Un utilisateur peut appartenir à plusieurs entreprises
- Chaque utilisateur a des rôles différents par entreprise
- Les données sont isolées par `company_id`
- Le header `X-Company-Id` est requis pour toutes les requêtes authentifiées

## Sécurité

### JWT

- Expiration: 24h (access token)
- Algorithme: HS256
- Secret partagé entre auth-service et gateway

### Permissions

Le système utilise des permissions granulaires:
- Format: `RESOURCE:ACTION` (ex: `ACCOUNTING:READ`, `EMPLOYEES:WRITE`)
- Les rôles sont associés à des permissions
- Vérification au niveau du gateway et des services

## Monitoring

### Logs

Les logs sont disponibles via Docker:

```bash
# Voir les logs d'un service
docker-compose logs -f auth-service

# Voir tous les logs
docker-compose logs -f
```

### Health Checks

Tous les services exposent un endpoint `/actuator/health`:

```bash
curl http://localhost:8080/api/auth/actuator/health
```

## Troubleshooting

### Le migration-service ne démarre pas

Vérifier que PostgreSQL est bien démarré:
```bash
docker-compose ps postgres
```

### Erreur "Connection refused" sur les services

Attendre que tous les services soient démarrés (peut prendre 1-2 minutes):
```bash
docker-compose ps
```

### Problème de connexion JWT

Vérifier que le secret JWT est identique dans `.env` pour auth-service et gateway.

## Contribution

1. Créer une branche: `git checkout -b feature/ma-fonctionnalite`
2. Commit: `git commit -am 'Ajout de ma fonctionnalité'`
3. Push: `git push origin feature/ma-fonctionnalite`
4. Créer une Pull Request

## License

Propriétaire - Tous droits réservés

## Support

Pour toute question ou problème, créer une issue sur le repository.
