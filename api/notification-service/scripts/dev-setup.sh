#!/bin/bash

# Script de configuration pour le développement local
# Usage: ./scripts/dev-setup.sh

set -e

echo "🚀 Configuration de l'environnement de développement..."

# Couleurs pour les messages
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Vérifier si Docker est installé
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker n'est pas installé${NC}"
    echo "Installez Docker Desktop: https://www.docker.com/products/docker-desktop"
    exit 1
fi

# Vérifier si docker-compose est installé
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ docker-compose n'est pas installé${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker et docker-compose sont installés${NC}"

# Créer le fichier .env s'il n'existe pas
if [ ! -f .env ]; then
    echo -e "${YELLOW}📝 Création du fichier .env...${NC}"
    cp .env.example .env
    echo -e "${GREEN}✅ Fichier .env créé${NC}"
else
    echo -e "${GREEN}✅ Fichier .env existe déjà${NC}"
fi

# Démarrer les services Docker
echo -e "${YELLOW}🐳 Démarrage de Redis et PostgreSQL...${NC}"
docker-compose up -d

# Attendre que Redis soit prêt
echo -e "${YELLOW}⏳ Attente de Redis...${NC}"
sleep 3

# Vérifier que Redis est accessible
if docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Redis est prêt${NC}"
else
    echo -e "${RED}❌ Redis n'est pas accessible${NC}"
    exit 1
fi

# Vérifier que PostgreSQL est accessible
echo -e "${YELLOW}⏳ Attente de PostgreSQL...${NC}"
sleep 3

if docker-compose exec -T postgres pg_isready -U notifuser > /dev/null 2>&1; then
    echo -e "${GREEN}✅ PostgreSQL est prêt${NC}"
else
    echo -e "${RED}❌ PostgreSQL n'est pas accessible${NC}"
    exit 1
fi

# Installer les dépendances
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}📦 Installation des dépendances...${NC}"
    pnpm install
    echo -e "${GREEN}✅ Dépendances installées${NC}"
fi

# Générer le client Prisma
echo -e "${YELLOW}🔧 Génération du client Prisma...${NC}"
pnpm prisma generate
echo -e "${GREEN}✅ Client Prisma généré${NC}"

# Appliquer les migrations (si en mode local)
read -p "Voulez-vous appliquer les migrations Prisma ? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}🔄 Application des migrations Prisma...${NC}"
    pnpm prisma migrate deploy
    echo -e "${GREEN}✅ Migrations appliquées${NC}"
fi

echo ""
echo -e "${GREEN}🎉 Configuration terminée !${NC}"
echo ""
echo "📋 Services disponibles :"
echo "  - Redis:            localhost:6379"
echo "  - PostgreSQL:       localhost:5432"
echo "  - Redis Commander:  http://localhost:8081"
echo "  - pgAdmin:          http://localhost:5050 (admin@notification.local / admin)"
echo ""
echo "🚀 Démarrer l'application :"
echo "  pnpm run start:dev"
echo ""
echo "📊 Une fois l'app démarrée :"
echo "  - API:              http://localhost:3000"
echo "  - BullBoard:        http://localhost:3000/queues"
echo "  - Health Check:     http://localhost:3000/health"
echo ""
