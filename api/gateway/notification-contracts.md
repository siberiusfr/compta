Crée un système complet de vérification d'email avec communication asynchrone entre Spring Boot et NestJS via Redis/BullMQ.
Contexte du projet

Projet principal : COMPTA (système de gestion comptable)
Backend : Module Maven Spring Boot (oauth2-server)
Service notifications : Application NestJS (notification-service)
Base de données : PostgreSQL
Communication : Redis + BullMQ
Table existante : email_verification_tokens

oauth2-server ne doit plus envoyer de mail via smtp mais doit poster la queue bullMQ (Redis)

Proposition mais adapte toi au code existant

Structure à créer
compta/
├── notification-contracts/          # NOUVEAU MODULE
│   ├── asyncapi.yaml
│   ├── pom.xml
│   ├── package.json
│   ├── tsconfig.json
│   ├── .gitignore
│   └── README.md
├── oauth2-server/                   # MODULE EXISTANT - À MODIFIER
│   ├── pom.xml                      # Ajouter dépendances
│   ├── src/main/resources/
│   │   └── application.yml          # Ajouter config Redis
│   └── src/main/java/tn/compta/oauth2/
│       ├── config/
│       │   └── RedisConfig.java     # NOUVEAU
│       ├── ...
└── notification-service/            # APPLICATION EXISTANTE - À MODIFIER
    ├── package.json                 # Ajouter dépendances
    ├── .env                         # Ajouter variables
    ├── src/
    │   ├── mail/
    │   │   └── mail.module.ts       # NOUVEAU
    │   ├── processors/
    │   │   ├── email-verification-requested.processor.ts  # NOUVEAU
    │   │   ├── email-verification-completed.processor.ts  # NOUVEAU
    │   │   └── email-verification-failed.processor.ts     # NOUVEAU
    │   └── notification/
    │       └── notification.module.ts  # MODIFIER
    └── templates/
        ├── email-verification-requested-fr.hbs  # NOUVEAU
        ├── email-verification-requested-ar.hbs  # NOUVEAU
        ├── email-verification-completed.hbs     # NOUVEAU
        └── email-verification-failed.hbs        # NOUVEAU

PARTIE 1 : Module notification-contracts
1.1 Créer asyncapi.yaml
Spécification AsyncAPI 3.0.0 avec :
Serveur :

Host: localhost:6379
Protocol: redis
