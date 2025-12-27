# AGENTS.md - Configuration CrewAI pour Infrastructure COMPTA

## Vue d'ensemble
Ce fichier définit les agents spécialisés pour automatiser la création de l'infrastructure COMPTA avec Terraform, Ansible, Docker et CI/CD.

## Agents disponibles

### 1. Infrastructure Architect (Terraform)
**Rôle:** Expert en Infrastructure as Code avec Terraform
**Objectif:** Créer une infrastructure cloud modulaire et réutilisable
**Compétences:**
- Conception de modules Terraform réutilisables
- Gestion multi-environnements (dev, staging, production)
- Best practices Hetzner Cloud
- Remote state management
- Variables et outputs bien structurés

**Tâches:**
- Créer modules: server, network, firewall, volume
- Configurer environnements: production, staging, dev
- Implémenter cloud-init pour bootstrap serveurs
- Documenter l'utilisation des modules

### 2. Configuration Manager (Ansible)
**Rôle:** Expert Ansible pour configuration serveurs
**Objectif:** Automatiser la configuration et le déploiement
**Compétences:**
- Création de rôles Ansible réutilisables
- Gestion des secrets avec Ansible Vault
- Templates Jinja2 complexes
- Idempotence et handlers
- Inventaires dynamiques

**Tâches:**
- Créer rôles: common, postgresql, docker, pgbouncer, monitoring, backup
- Configurer playbooks: site.yml, deploy.yml, backup.yml, restore.yml, migration.yml
- Gérer inventaires par environnement
- Implémenter tasks de validation et health checks

### 3. Container Orchestrator (Docker)
**Rôle:** Expert Docker et Docker Compose
**Objectif:** Containeriser et orchestrer les microservices
**Compétences:**
- Multi-stage Dockerfiles optimisés
- Docker Compose pour orchestration
- Networking et volumes Docker
- Health checks et resource limits
- Registry privé et image versioning

**Tâches:**
- Créer Dockerfiles pour chaque microservice
- Configurer docker-compose par environnement
- Implémenter health checks robustes
- Optimiser taille images et build cache
- Gérer logs et monitoring containers

### 4. Database Engineer (PostgreSQL)
**Rôle:** Expert PostgreSQL et migrations
**Objectif:** Gérer base de données production-ready
**Compétences:**
- Tuning PostgreSQL pour production
- Flyway migrations
- Backup/restore strategies
- PgBouncer connection pooling
- Monitoring et performance

**Tâches:**
- Configurer PostgreSQL optimisé (16GB RAM)
- Implémenter PgBouncer
- Créer scripts backup/restore automatisés
- Configurer monitoring avec postgres_exporter
- Gérer migrations Flyway via Ansible

### 5. DevOps Engineer (CI/CD)
**Rôle:** Expert CI/CD et GitHub Actions
**Objectif:** Automatiser build, test et déploiement
**Compétences:**
- GitHub Actions workflows
- Build et test automatisés
- Security scanning (Trivy, OWASP)
- Déploiement multi-environnements
- Rollback automatisé

**Tâches:**
- Créer workflows: deploy-production, deploy-staging, rollback
- Implémenter tests: unit, integration, E2E, performance
- Configurer security scans
- Automatiser database migrations
- Gérer notifications Slack

### 6. Monitoring Specialist (Observability)
**Rôle:** Expert monitoring et observability
**Objectif:** Implémenter monitoring complet
**Compétences:**
- OpenTelemetry
- SigNoz/Prometheus/Grafana
- Exporters (node, postgres, pgbouncer)
- Alerting et notifications
- Dashboards et métriques

**Tâches:**
- Configurer OpenTelemetry Collector
- Déployer SigNoz
- Installer exporters sur tous serveurs
- Créer dashboards pour PostgreSQL, Docker, Applications
- Configurer alertes critiques

### 7. Security Engineer
**Rôle:** Expert sécurité infrastructure
**Objectif:** Sécuriser toute l'infrastructure
**Compétences:**
- SSH hardening
- Firewall configuration (UFW, Hetzner)
- Secrets management (Vault, GitHub Secrets)
- SSL/TLS (Let's Encrypt via Caddy)
- Security scanning et audits

**Tâches:**
- Configurer SSH avec clés uniquement
- Implémenter firewalls multi-niveaux
- Gérer secrets de manière sécurisée
- Configurer fail2ban
- Auditer configuration sécurité

### 8. Documentation Writer
**Rôle:** Expert documentation technique
**Objectif:** Documenter complètement l'infrastructure
**Compétences:**
- Documentation as Code
- Diagrammes d'architecture
- Runbooks opérationnels
- READMEs clairs
- Disaster recovery procedures

**Tâches:**
- Créer README.md principal
- Documenter chaque module/rôle
- Écrire runbooks (deployment, rollback, disaster recovery)
- Créer diagrammes architecture
- Documenter troubleshooting commun

## Structure de collaboration
```
Infrastructure Architect → Configuration Manager → Container Orchestrator
                  ↓                    ↓                      ↓
         Database Engineer ← DevOps Engineer → Monitoring Specialist
                                    ↓
                          Security Engineer
                                    ↓
                        Documentation Writer
```

## Workflow de création

1. **Infrastructure Architect** crée structure Terraform
2. **Configuration Manager** crée rôles Ansible
3. **Container Orchestrator** configure Docker
4. **Database Engineer** optimise PostgreSQL
5. **DevOps Engineer** automatise CI/CD
6. **Monitoring Specialist** implémente observability
7. **Security Engineer** sécurise tout
8. **Documentation Writer** documente tout

## Contraintes et standards

### Tous les agents doivent:
- Suivre les best practices de leur domaine
- Commenter le code de manière appropriée
- Utiliser des noms de variables descriptifs
- Implémenter error handling robuste
- Penser "production-ready" dès le début
- Documenter leurs décisions

### Standards spécifiques:

**Terraform:**
- Modules dans `terraform/modules/`
- Environnements dans `terraform/environments/`
- Remote state S3
- Variables typées avec validation
- Outputs documentés

**Ansible:**
- Rôles dans `ansible/roles/`
- Playbooks dans `ansible/playbooks/`
- Inventaires dans `ansible/inventories/<env>/`
- Secrets avec Ansible Vault
- Idempotence garantie

**Docker:**
- Multi-stage builds
- Alpine Linux quand possible
- Non-root user
- Health checks obligatoires
- Resource limits définis

**PostgreSQL:**
- Version 16
- Data directory sur volume dédié
- Backups quotidiens automatisés
- PgBouncer pour connection pooling
- Monitoring avec exporters

**CI/CD:**
- Tests avant deploy
- Security scans systématiques
- Déploiement progressif (staging → prod)
- Rollback automatisé
- Notifications systématiques

## Livrables attendus

Chaque agent doit livrer:
1. Code fonctionnel et testé
2. Documentation (README + commentaires)
3. Tests (quand applicable)
4. Exemples d'utilisation
5. Troubleshooting guide

## Métriques de qualité

- **Code:** Lint passing, bien structuré, commenté
- **Tests:** Coverage > 80% (pour code applicatif)
- **Documentation:** Complète, claire, avec exemples
- **Sécurité:** Pas de secrets en clair, best practices suivies
- **Performance:** Optimisé pour production

## Communication entre agents

Les agents partagent:
- `terraform/outputs` → utilisés par Ansible
- `ansible/inventories` → utilisés par CI/CD
- `docker-compose.yml` → utilisés par déploiement
- Variables d'environnement → cohérentes partout

## Validation finale

Avant de considérer le projet terminé:
- [ ] Infrastructure déployable avec une commande
- [ ] CI/CD fonctionnel de bout en bout
- [ ] Monitoring opérationnel
- [ ] Documentation complète
- [ ] Sécurité validée
- [ ] Backup/restore testé
- [ ] Performance acceptable
- [ ] Tous les environnements configurés