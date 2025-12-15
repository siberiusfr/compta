# Guide d'Implémentation COMPTA ERP

## État d'avancement

### ✅ TERMINÉ
- ✅ Structure complète du projet
- ✅ Fichiers racine (README, docker-compose.yml, .env.example, .gitignore)
- ✅ Migration Service (complet avec 9 migrations SQL)
- ✅ Compta-Commons (bibliothèque partagée complète)
- ✅ Gateway (pom.xml + Dockerfile)

### 🔄 À COMPLÉTER
- ⏳ Gateway (configuration + filtres + routing)
- ⏳ Auth Service (complet)
- ⏳ Accounting Service (complet)
- ⏳ Document Service (complet)
- ⏳ HR Service (complet)
- ⏳ Notification Service (NestJS)
- ⏳ Frontend (React + TypeScript)

## Prochaines étapes

### 1. Finaliser le Gateway

Fichiers à créer dans `gateway/src/main/java/tn/compta/gateway/`:

#### GatewayApplication.java
```java
package tn.compta.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

#### application.yml
Créer dans `gateway/src/main/resources/`:
- Configuration des routes vers tous les services
- Configuration JWT
- Configuration CORS
- Configuration rate limiting avec Redis

#### JwtAuthenticationFilter.java
Dans `gateway/src/main/java/tn/compta/gateway/filter/`:
- Valider le JWT
- Extraire user_id, email, roles, permissions
- Injecter les headers X-User-Id, X-User-Email, etc.

### 2. Auth Service

Structure complète:

```
services/auth-service/
├── pom.xml (ajouter dépendance compta-commons)
├── Dockerfile
├── src/main/
│   ├── java/tn/compta/auth/
│   │   ├── AuthServiceApplication.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Company.java
│   │   │   ├── Role.java
│   │   │   ├── Permission.java
│   │   │   ├── UserCompanyRole.java
│   │   │   └── UserPermission.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── CompanyRepository.java
│   │   │   ├── RoleRepository.java
│   │   │   ├── PermissionRepository.java
│   │   │   ├── UserCompanyRoleRepository.java
│   │   │   └── UserPermissionRepository.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── UserService.java
│   │   │   ├── CompanyService.java
│   │   │   └── JwtService.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── UserController.java
│   │   │   └── CompanyController.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── RegisterRequest.java
│   │   │   └── UserDto.java
│   │   ├── security/
│   │   │   ├── SecurityConfig.java
│   │   │   └── JwtTokenProvider.java
│   │   └── config/
│   │       └── OpenApiConfig.java
│   └── resources/
│       └── application.yml
```

#### Endpoints clés:
- POST `/api/auth/register` - Inscription
- POST `/api/auth/login` - Connexion (génère JWT)
- GET `/api/auth/me` - Profil utilisateur
- GET `/api/auth/companies` - Liste entreprises de l'utilisateur
- GET `/api/auth/permissions` - Permissions de l'utilisateur pour une entreprise

### 3. Accounting Service

#### Entities principales:
- Account, Journal, FiscalYear, JournalEntry, JournalEntryLine
- BankAccount, TaxDeclaration, Partner

#### Services clés:
- AccountService (CRUD plan comptable)
- JournalEntryService (création écritures comptables)
- BalanceService (calcul balance)
- LedgerService (grand livre)
- TaxService (déclarations TVA)

#### Endpoints:
- `/api/accounting/accounts` - Plan comptable
- `/api/accounting/journals` - Journaux
- `/api/accounting/entries` - Écritures
- `/api/accounting/balance` - Balance
- `/api/accounting/ledger` - Grand livre
- `/api/accounting/tax-declarations` - TVA

### 4. Document Service

#### Configuration MinIO:
- Connection au démarrage
- Créer bucket si n'existe pas
- Upload/Download via MinIO client

#### Services clés:
- DocumentService (CRUD documents)
- MinioService (stockage fichiers)
- DocumentVersionService (versioning)
- DocumentShareService (partage)
- OcrService (extraction texte) - optionnel

### 5. HR Service

#### Entities:
- Department, Position, Employee, Contract
- Payslip, LeaveType, LeaveRequest, LeaveBalance, Attendance

#### Services métier:
- EmployeeService
- ContractService
- PayslipService (calcul paie avec CNSS, IRPP)
- LeaveService (gestion congés)
- AttendanceService (pointage)

#### Calculs importants:
- CNSS employé: 9.18%
- CNSS employeur: 16.57%
- IRPP (barème progressif Tunisie)

### 6. Notification Service (NestJS)

Structure:
```
services/notification-service/
├── package.json
├── tsconfig.json
├── Dockerfile
├── src/
│   ├── main.ts
│   ├── app.module.ts
│   ├── modules/
│   │   ├── email/
│   │   │   ├── email.module.ts
│   │   │   ├── email.service.ts
│   │   │   └── email.controller.ts
│   │   ├── sms/
│   │   │   ├── sms.module.ts
│   │   │   └── sms.service.ts
│   │   ├── notification/
│   │   │   ├── notification.module.ts
│   │   │   ├── notification.service.ts
│   │   │   └── notification.controller.ts
│   │   └── template/
│   │       ├── template.module.ts
│   │       └── template.service.ts
│   ├── config/
│   │   ├── database.config.ts
│   │   └── rabbitmq.config.ts
│   └── entities/
│       ├── notification.entity.ts
│       └── notification-template.entity.ts
```

#### Intégrations:
- TypeORM pour PostgreSQL
- RabbitMQ pour queue async
- Nodemailer pour SMTP
- Handlebars pour templating

### 7. Frontend (React + TypeScript)

Structure:
```
frontend/
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.js
├── Dockerfile
├── index.html
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── components/
│   │   ├── ui/ (shadcn/ui components)
│   │   ├── layout/
│   │   │   ├── Header.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   └── Layout.tsx
│   │   └── common/
│   ├── pages/
│   │   ├── auth/
│   │   │   ├── Login.tsx
│   │   │   └── Register.tsx
│   │   ├── dashboard/
│   │   │   └── Dashboard.tsx
│   │   ├── accounting/
│   │   ├── documents/
│   │   └── hr/
│   ├── services/
│   │   ├── api.ts (axios config)
│   │   ├── auth.service.ts
│   │   ├── accounting.service.ts
│   │   ├── document.service.ts
│   │   └── hr.service.ts
│   ├── types/
│   │   ├── user.types.ts
│   │   ├── accounting.types.ts
│   │   └── hr.types.ts
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   └── useApi.ts
│   └── utils/
│       ├── constants.ts
│       └── formatters.ts
```

#### Librairies clés:
- React Router v6 (routing)
- TanStack Query (data fetching)
- Zustand (state management)
- React Hook Form + Zod (forms + validation)
- shadcn/ui (components)
- Recharts (graphiques)

## Instructions de Build

### 1. Build compta-commons (OBLIGATOIRE EN PREMIER)
```bash
cd compta-commons
mvn clean install
```

### 2. Build tous les services Spring Boot
```bash
# Gateway
cd gateway && mvn clean package

# Migration Service
cd services/migration-service && mvn clean package

# Auth Service
cd services/auth-service && mvn clean package

# Accounting Service
cd services/accounting-service && mvn clean package

# Document Service
cd services/document-service && mvn clean package

# HR Service
cd services/hr-service && mvn clean package
```

### 3. Build Notification Service
```bash
cd services/notification-service
npm install
npm run build
```

### 4. Build Frontend
```bash
cd frontend
npm install
npm run build
```

### 5. Lancer avec Docker Compose
```bash
docker-compose up -d
```

## Configuration importante

### Variables d'environnement (.env)
Copier `.env.example` vers `.env` et remplir:
- JWT_SECRET (minimum 32 caractères)
- POSTGRES_PASSWORD
- SMTP credentials
- MinIO credentials

### Ordre de démarrage Docker
1. PostgreSQL, Redis, RabbitMQ, MinIO
2. Migration Service (s'exécute une fois puis s'arrête)
3. Tous les services backend
4. Frontend

## Tests

### Tester la migration
```bash
curl http://localhost:8085/api/migration/status
```

### Tester le gateway
```bash
curl http://localhost:8080/actuator/health
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@compta.tn","password":"admin123"}'
```

## Documentation API

Swagger UI disponible sur:
- Gateway: http://localhost:8080/swagger-ui.html
- Auth Service: http://localhost:8081/swagger-ui.html
- Accounting Service: http://localhost:8082/swagger-ui.html
- Document Service: http://localhost:8083/swagger-ui.html
- HR Service: http://localhost:8084/swagger-ui.html

## Prochaines améliorations

1. **Tests**:
   - Tests unitaires pour tous les services
   - Tests d'intégration
   - Tests E2E pour le frontend

2. **Monitoring**:
   - Prometheus + Grafana
   - ELK Stack pour les logs
   - Distributed tracing (Zipkin/Jaeger)

3. **CI/CD**:
   - GitHub Actions ou GitLab CI
   - Automated builds
   - Automated deployments

4. **Sécurité**:
   - HTTPS/TLS
   - Secrets management (Vault)
   - API rate limiting avancé
   - OWASP compliance

5. **Performance**:
   - Database indexing optimization
   - Caching strategy avec Redis
   - CDN pour le frontend

## Support

Pour toute question, créer une issue dans le repository.
