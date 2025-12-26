# 🚀 Redis pour COMPTA Gateway - Guide de Démarrage

## 📋 Prérequis

- Docker installé
- Docker Compose installé
- Port 6379 disponible

## 🎯 Démarrage Rapide

### Option 1 : Redis Seul (Recommandé)

```bash
# Démarrer Redis
docker-compose -f docker-compose-redis.yml up -d

# Vérifier que Redis est démarré
docker-compose -f docker-compose-redis.yml ps

# Tester la connexion
docker exec -it compta-redis redis-cli ping
# Doit retourner: PONG
```

### Option 2 : Redis + Interface Web

```bash
# Démarrer Redis avec Redis Commander (interface web)
docker-compose -f docker-compose-redis.yml --profile tools up -d

# Accéder à l'interface web
open http://localhost:8081
```

## 📊 Commandes Utiles

### Vérifier l'État

```bash
# Status des containers
docker-compose -f docker-compose-redis.yml ps

# Logs Redis
docker-compose -f docker-compose-redis.yml logs -f redis

# Health check
docker exec compta-redis redis-cli ping
```

### Inspecter Redis

```bash
# Ouvrir le CLI Redis
docker exec -it compta-redis redis-cli

# Dans le CLI Redis:
> PING                    # Test de connexion
> INFO                    # Informations sur Redis
> DBSIZE                  # Nombre de clés
> KEYS *                  # Lister toutes les clés
> MONITOR                 # Voir les commandes en temps réel
> QUIT                    # Quitter
```

### Vérifier le Rate Limiting

```bash
# Après avoir démarré la gateway, faire plusieurs requêtes
for i in {1..10}; do
  curl -s http://localhost:8080/actuator/health > /dev/null
  echo "Request $i completed"
done

# Vérifier les clés de rate limiting dans Redis
docker exec compta-redis redis-cli KEYS "request_rate_limiter*"

# Voir la valeur d'une clé
docker exec compta-redis redis-cli GET "request_rate_limiter.{username}.tokens"
```

## 🛑 Arrêt et Nettoyage

### Arrêter Redis

```bash
# Arrêter sans supprimer les données
docker-compose -f docker-compose-redis.yml stop

# Redémarrer
docker-compose -f docker-compose-redis.yml start
```

### Nettoyer Complètement

```bash
# Arrêter et supprimer les containers
docker-compose -f docker-compose-redis.yml down

# Arrêter et supprimer les containers + volumes (⚠️ perte de données)
docker-compose -f docker-compose-redis.yml down -v
```

## 🔧 Configuration de la Gateway

### Variables d'Environnement

Dans votre `.env` ou lors du lancement de la gateway :

```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

### Si la Gateway est aussi dans Docker

Si vous lancez la gateway avec Docker, utilisez le nom du service :

```bash
REDIS_HOST=redis  # Au lieu de localhost
REDIS_PORT=6379
```

## 📈 Monitoring Redis

### Via Redis Commander (Interface Web)

1. Démarrer avec le profil `tools` :
   ```bash
   docker-compose -f docker-compose-redis.yml --profile tools up -d
   ```

2. Accéder à http://localhost:8081

3. Vous pouvez :
   - Voir toutes les clés
   - Inspecter les valeurs
   - Voir les statistiques
   - Exécuter des commandes

### Via CLI

```bash
# Statistiques en temps réel
docker exec compta-redis redis-cli --stat

# Information détaillée
docker exec compta-redis redis-cli INFO

# Utilisation mémoire
docker exec compta-redis redis-cli INFO memory
```

## 🐛 Troubleshooting

### Redis ne démarre pas

```bash
# Vérifier si le port est déjà utilisé
lsof -i :6379
# ou
netstat -an | grep 6379

# Stopper l'ancien container si existe
docker stop compta-redis
docker rm compta-redis

# Relancer
docker-compose -f docker-compose-redis.yml up -d
```

### Erreur de connexion depuis la Gateway

```bash
# 1. Vérifier que Redis est bien démarré
docker ps | grep redis

# 2. Vérifier que le port est exposé
docker port compta-redis

# 3. Tester la connexion
docker exec compta-redis redis-cli ping

# 4. Si la gateway est en Docker, utiliser le nom du service
REDIS_HOST=redis  # pas localhost
```

### Nettoyer Redis (vider toutes les données)

```bash
# Via CLI
docker exec compta-redis redis-cli FLUSHALL

# Via Redis Commander
# http://localhost:8081 > Server > Flush DB
```

## 📊 Tests de Rate Limiting

### Test Simple

```bash
# Faire 150 requêtes rapidement
for i in {1..150}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
  echo "Request $i: HTTP $STATUS"
done

# Les dernières requêtes doivent retourner 429 (Too Many Requests)
```

### Test avec Authentification

```bash
# 1. Obtenir un token
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}' \
  | jq -r .token)

# 2. Faire des requêtes avec le token
for i in {1..150}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/api/invoices)
  echo "Request $i: HTTP $STATUS"
done
```

### Vérifier les Compteurs dans Redis

```bash
# Voir les clés de rate limiting
docker exec compta-redis redis-cli KEYS "*rate*"

# Voir la valeur d'un compteur
docker exec compta-redis redis-cli GET "request_rate_limiter.{test}.tokens"

# TTL (Time To Live) d'une clé
docker exec compta-redis redis-cli TTL "request_rate_limiter.{test}.tokens"
```

## 🔒 Sécurité (Production)

Pour la production, ajoutez un mot de passe :

```yaml
# docker-compose-redis.yml
services:
  redis:
    command: >
      redis-server 
      --requirepass your-secure-password
      --appendonly yes
```

Et dans la gateway :
```bash
REDIS_PASSWORD=your-secure-password
```

## 💡 Astuces

### Persistance des Données

Les données Redis sont persistées dans un volume Docker :
```bash
# Voir les volumes
docker volume ls | grep redis

# Inspecter le volume
docker volume inspect gateway-service_redis-data
```

### Performance

```bash
# Voir les statistiques de performance
docker exec compta-redis redis-cli --latency

# Benchmark
docker exec compta-redis redis-cli --latency-history
```

### Backup/Restore (si nécessaire)

```bash
# Créer un backup
docker exec compta-redis redis-cli SAVE
docker cp compta-redis:/data/dump.rdb ./backup-redis.rdb

# Restaurer
docker cp ./backup-redis.rdb compta-redis:/data/dump.rdb
docker-compose -f docker-compose-redis.yml restart redis
```

## 📞 Liens Utiles

- Documentation Redis : https://redis.io/docs/
- Redis Commander : https://github.com/joeferner/redis-commander
- Rate Limiting avec Redis : https://redis.io/docs/manual/patterns/rate-limiter/

---

**C'est tout ! Redis est prêt pour votre gateway.** 🎉

Pour démarrer :
```bash
docker-compose -f docker-compose-redis.yml up -d
```

Pour vérifier :
```bash
docker exec -it compta-redis redis-cli ping
```

Puis lancez votre gateway ! 🚀
