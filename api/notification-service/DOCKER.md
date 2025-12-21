# Docker Setup pour Développement

Ce projet inclut des configurations Docker pour faciliter le développement local.

## 🚀 Démarrage Rapide

### Option 1: Stack complète (Redis + PostgreSQL + Outils UI)

```bash
# Démarrer tous les services
docker-compose up -d

# Vérifier que tout fonctionne
docker-compose ps

# Voir les logs
docker-compose logs -f
```

**Services disponibles :**
- Redis: `localhost:6379`
- PostgreSQL: `localhost:5432`
- Redis Commander (UI): http://localhost:8081
- pgAdmin (UI): http://localhost:5050
- BullBoard (via app): http://localhost:3000/queues

### Option 2: Redis uniquement (minimal)

```bash
# Utiliser la version dev (Redis seulement)
docker-compose -f docker-compose.dev.yml up -d
```

## 📋 Services Inclus

### Redis (Port 6379)
- **Usage**: Queue BullMQ
- **Interface**: Redis Commander sur http://localhost:8081
- **Données**: Persistées dans volume `redis-data`

### PostgreSQL (Port 5432)
- **Database**: `notification_db`
- **User**: `notifuser`
- **Password**: `notifpass`
- **Interface**: pgAdmin sur http://localhost:5050
  - Email: `admin@notification.local`
  - Password: `admin`

## 🔧 Configuration

### 1. Copier le fichier d'environnement

```bash
cp .env.example .env
```

### 2. Mettre à jour le DATABASE_URL dans .env

```env
DATABASE_URL="postgresql://notifuser:notifpass@localhost:5432/notification_db?schema=public"
```

### 3. Générer le client Prisma et appliquer les migrations

```bash
# Générer le client Prisma
pnpm prisma generate

# Créer et appliquer les migrations
pnpm prisma migrate dev --name init

# Ou appliquer des migrations existantes
pnpm prisma migrate deploy
```

### 4. Démarrer l'application

```bash
pnpm run start:dev
```

## 📦 Commandes Utiles

### Docker Compose

```bash
# Démarrer les services
docker-compose up -d

# Arrêter les services
docker-compose down

# Arrêter et supprimer les volumes (⚠️ perte de données)
docker-compose down -v

# Redémarrer un service spécifique
docker-compose restart redis

# Voir les logs d'un service
docker-compose logs -f redis

# Entrer dans un conteneur
docker-compose exec redis redis-cli
docker-compose exec postgres psql -U notifuser -d notification_db
```

### Redis CLI

```bash
# Se connecter à Redis
docker-compose exec redis redis-cli

# Commandes Redis utiles
PING                    # Tester la connexion
KEYS *                  # Lister toutes les clés
GET key_name            # Obtenir une valeur
FLUSHALL                # ⚠️ Supprimer toutes les données
```

### PostgreSQL

```bash
# Se connecter à PostgreSQL
docker-compose exec postgres psql -U notifuser -d notification_db

# Commandes SQL utiles
\dt                     # Lister les tables
\d table_name           # Décrire une table
SELECT * FROM notifications LIMIT 10;
```

## 🔍 Interfaces Web

### Redis Commander (http://localhost:8081)
- Naviguer dans les clés Redis
- Voir les queues BullMQ
- Inspecter les données en temps réel

### pgAdmin (http://localhost:5050)
1. Se connecter avec `admin@notification.local` / `admin`
2. Ajouter un serveur :
   - Name: `Notification DB`
   - Host: `postgres` (nom du service Docker)
   - Port: `5432`
   - Database: `notification_db`
   - Username: `notifuser`
   - Password: `notifpass`

### BullBoard (http://localhost:3000/queues)
- Interface intégrée à l'application NestJS
- Nécessite que l'app soit démarrée
- Surveiller les jobs de la queue `mail_queue`

## 🧪 Tests

### Tester la connexion Redis

```bash
# Depuis l'hôte
redis-cli -h localhost -p 6379 ping

# Depuis Docker
docker-compose exec redis redis-cli ping
```

### Tester la connexion PostgreSQL

```bash
# Depuis l'hôte (si psql installé)
psql -h localhost -U notifuser -d notification_db

# Depuis Docker
docker-compose exec postgres psql -U notifuser -d notification_db -c "SELECT version();"
```

## 🛠️ Troubleshooting

### Port déjà utilisé

Si un port est déjà utilisé, modifier `docker-compose.yml`:

```yaml
services:
  redis:
    ports:
      - "6380:6379"  # Utiliser 6380 à la place
```

Puis mettre à jour `.env`:
```env
REDIS_PORT=6380
```

### Réinitialiser complètement

```bash
# Arrêter et supprimer tout
docker-compose down -v

# Supprimer les données locales
rm -rf data/

# Redémarrer
docker-compose up -d
```

### Voir l'utilisation mémoire

```bash
docker stats
```

## 📊 Monitoring

### Logs en temps réel

```bash
# Tous les services
docker-compose logs -f

# Service spécifique
docker-compose logs -f redis
docker-compose logs -f postgres
```

### Health Checks

```bash
# Vérifier le statut des services
docker-compose ps

# Health check de l'application
curl http://localhost:3000/health
```

## 🔒 Production

⚠️ **Important**: Ces configurations sont pour le développement uniquement !

Pour la production:
- Utiliser des mots de passe sécurisés
- Configurer SSL/TLS
- Activer l'authentification Redis
- Utiliser des services managés (AWS RDS, ElastiCache, etc.)
- Implémenter des backups automatiques
