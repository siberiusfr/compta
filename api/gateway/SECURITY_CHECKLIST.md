# 🔒 Security Checklist - COMPTA Gateway

## ⚠️ Avant le Déploiement en Production

Cette checklist **DOIT** être complétée et validée avant tout déploiement en production.

---

## 1. Configuration JWT

### Secrets et Clés

- [ ] **JWT Secret généré** : Minimum 64 caractères
  ```bash
  # Vérification
  echo $JWT_SECRET | wc -c  # Doit être >= 64
  ```

- [ ] **Secret unique** : Jamais utiliser le secret par défaut du repository
  ```bash
  # Le secret NE DOIT PAS être celui-ci :
  # 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  ```

- [ ] **Secret stocké de manière sécurisée** : 
  - Utiliser un secret manager (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault)
  - OU variable d'environnement (jamais hardcodé)

- [ ] **Expiration JWT appropriée** :
  - Recommandé : 1 heure maximum (`JWT_EXPIRATION=3600000`)
  - Jamais plus de 24 heures en production

### Validation

- [ ] **Validation de la signature** : Activée (par défaut)
- [ ] **Validation de l'issuer** : `compta-auth` (vérifié dans SecurityConfig)
- [ ] **Validation de l'expiration** : Activée (par défaut)

---

## 2. HTTPS et Certificats SSL

- [ ] **HTTPS obligatoire** : Toutes les URLs de services en `https://`
  ```yaml
  # ✅ Correct
  AUTH_SERVICE_URL=https://auth.compta.tn
  
  # ❌ Incorrect en production
  AUTH_SERVICE_URL=http://auth.compta.tn
  ```

- [ ] **HSTS activé** : Header `Strict-Transport-Security` configuré
  - Vérifier dans `SecurityHeadersFilter.java`

- [ ] **Certificats valides** :
  - Délivrés par une CA reconnue (Let's Encrypt, DigiCert, etc.)
  - Pas de certificats auto-signés en production
  - Date d'expiration > 30 jours

- [ ] **Redirection HTTP → HTTPS** : Configurée au niveau du load balancer/reverse proxy

---

## 3. CORS Configuration

- [ ] **Origins restreints** : Uniquement vos domaines de production
  ```yaml
  # ❌ Jamais ça en production
  cors.allowed-origins: "*"
  
  # ✅ Configuration stricte
  cors.allowed-origins:
    - https://app.compta.tn
    - https://www.compta.tn
  ```

- [ ] **Méthodes HTTP limitées** : Uniquement celles nécessaires
- [ ] **Headers exposés restreints** : Pas d'exposition de `Authorization`
- [ ] **Credentials** : `allowCredentials: true` uniquement si nécessaire

---

## 4. Rate Limiting

- [ ] **Redis configuré et accessible** :
  ```bash
  # Test de connexion
  redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD ping
  # Doit retourner : PONG
  ```

- [ ] **Limites appropriées** :
  - Production : 100 req/s par utilisateur recommandé
  - Ajuster selon votre charge attendue

- [ ] **Monitoring du rate limiting** :
  - Logs des rejets activés
  - Alertes configurées

---

## 5. Security Headers

Vérifier que tous ces headers sont présents dans les réponses :

- [ ] **X-Content-Type-Options** : `nosniff`
- [ ] **X-Frame-Options** : `DENY`
- [ ] **X-XSS-Protection** : `1; mode=block`
- [ ] **Strict-Transport-Security** : `max-age=31536000; includeSubDomains; preload`
- [ ] **Content-Security-Policy** : Configuré (strict pour API)
- [ ] **Referrer-Policy** : `strict-origin-when-cross-origin`
- [ ] **Permissions-Policy** : Fonctionnalités restreintes

```bash
# Test des headers
curl -I https://api.compta.tn/actuator/health
```

---

## 6. Endpoints Publics

- [ ] **Swagger UI désactivé** : `springdoc.swagger-ui.enabled=false`
  ```bash
  # Ne doit pas être accessible
  curl https://api.compta.tn/swagger-ui.html
  # Doit retourner 404
  ```

- [ ] **Actuator restreint** :
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,info,metrics,prometheus
  ```

- [ ] **Endpoints sensibles protégés** :
  - `/actuator/env` : NON exposé
  - `/actuator/configprops` : NON exposé
  - `/actuator/beans` : NON exposé

---

## 7. Logging et Monitoring

- [ ] **Niveau de logs approprié** :
  - Production : INFO ou WARN
  - Jamais DEBUG en production

- [ ] **Données sensibles masquées** :
  - JWT tokens : Masqués ✅
  - Emails : Partiellement masqués ✅
  - Mots de passe : Jamais loggés ✅

- [ ] **Request ID tracking** : Activé pour le tracing

- [ ] **Monitoring configuré** :
  - Prometheus metrics exposés
  - Dashboard Grafana configuré
  - Alertes configurées (circuit breaker, timeouts, errors)

---

## 8. Circuit Breakers

- [ ] **Configuration testée** :
  - Failure threshold approprié
  - Temps de récupération adapté
  - Fallbacks fonctionnels

- [ ] **Fallback controllers** :
  - Messages d'erreur informatifs
  - Pas d'exposition d'informations sensibles

- [ ] **Health checks** :
  - Services downstream monitorés
  - Alertes configurées

---

## 9. Network Security

- [ ] **Firewall configuré** :
  - Port 8080 accessible uniquement depuis le load balancer
  - Redis accessible uniquement depuis le gateway
  - Services downstream en réseau privé

- [ ] **Network policies** (Kubernetes) :
  - Ingress rules restrictives
  - Egress rules limitées

---

## 10. Dependencies et Versions

- [ ] **Versions à jour** :
  ```bash
  mvn versions:display-dependency-updates
  ```

- [ ] **Vulnérabilités scannées** :
  ```bash
  mvn dependency:tree
  mvn org.owasp:dependency-check-maven:check
  ```

- [ ] **Pas de dépendances SNAPSHOT** en production

---

## 11. Secrets Management

- [ ] **Aucun secret dans Git** :
  ```bash
  # Vérifier
  git log --all --full-history -- "*secret*" "*password*" "*key*"
  ```

- [ ] **Variables d'environnement** :
  - Jamais de valeurs par défaut pour les secrets en prod
  - Utilisation de secret manager recommandée

- [ ] **.env ajouté au .gitignore** : ✅

---

## 12. Container Security (si Docker/K8s)

- [ ] **Image basée sur Alpine** : Plus petite surface d'attaque
- [ ] **Non-root user** : Application ne tourne pas en root
- [ ] **Health checks** : Configurés dans le Dockerfile
- [ ] **Resource limits** : CPU et mémoire limités

---

## 13. Tests de Sécurité

- [ ] **Tests de pénétration** : Effectués
- [ ] **Scan de vulnérabilités** : Aucune critique
- [ ] **Tests de charge** : Système stable sous charge
- [ ] **Tests de failover** : Circuit breakers fonctionnent

---

## 14. Documentation et Runbooks

- [ ] **Runbook de déploiement** : Disponible et testé
- [ ] **Procédure de rollback** : Documentée et testée
- [ ] **Contacts d'urgence** : À jour
- [ ] **Documentation API** : À jour

---

## 15. Backup et Disaster Recovery

- [ ] **Backup Redis** : Configuré (si données critiques)
- [ ] **Plan de reprise** : Documenté et testé
- [ ] **RTO/RPO définis** : Recovery objectives clairs

---

## Validation Finale

### Commandes de vérification

```bash
# 1. Vérifier les variables d'environnement
env | grep -E "JWT|SERVICE_URL|REDIS"

# 2. Tester l'authentification
curl -X POST https://api.compta.tn/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'

# 3. Tester le rate limiting
for i in {1..150}; do
  curl -s -o /dev/null -w "%{http_code}\n" https://api.compta.tn/actuator/health
done
# Les dernières requêtes doivent retourner 429

# 4. Vérifier les security headers
curl -I https://api.compta.tn/actuator/health | grep -E "X-|Strict|Content-Security"

# 5. Vérifier le health check
curl https://api.compta.tn/actuator/health | jq

# 6. Vérifier les métriques
curl https://api.compta.tn/actuator/prometheus | grep http_server
```

---

## Sign-off

- [ ] **Développeur** : _________________ Date: _______
- [ ] **Security Lead** : _________________ Date: _______
- [ ] **DevOps Lead** : _________________ Date: _______
- [ ] **Tech Lead** : _________________ Date: _______

---

## Notes et Observations

```
<!-- Ajoutez ici toute note ou observation pertinente -->


```

---

**Date de la dernière révision** : __________________

**Prochaine révision programmée** : __________________
