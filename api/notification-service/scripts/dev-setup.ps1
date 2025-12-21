# Script de configuration pour le développement local (Windows)
# Usage: .\scripts\dev-setup.ps1

Write-Host "🚀 Configuration de l'environnement de développement..." -ForegroundColor Cyan

# Vérifier si Docker est installé
try {
    docker --version | Out-Null
    Write-Host "✅ Docker est installé" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker n'est pas installé" -ForegroundColor Red
    Write-Host "Installez Docker Desktop: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# Vérifier si docker-compose est installé
try {
    docker-compose --version | Out-Null
    Write-Host "✅ docker-compose est installé" -ForegroundColor Green
} catch {
    Write-Host "❌ docker-compose n'est pas installé" -ForegroundColor Red
    exit 1
}

# Créer le fichier .env s'il n'existe pas
if (-not (Test-Path .env)) {
    Write-Host "📝 Création du fichier .env..." -ForegroundColor Yellow
    Copy-Item .env.example .env
    Write-Host "✅ Fichier .env créé" -ForegroundColor Green
} else {
    Write-Host "✅ Fichier .env existe déjà" -ForegroundColor Green
}

# Démarrer les services Docker
Write-Host "🐳 Démarrage de Redis et PostgreSQL..." -ForegroundColor Yellow
docker-compose up -d

# Attendre que les services soient prêts
Write-Host "⏳ Attente des services..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Vérifier que Redis est accessible
try {
    docker-compose exec -T redis redis-cli ping | Out-Null
    Write-Host "✅ Redis est prêt" -ForegroundColor Green
} catch {
    Write-Host "❌ Redis n'est pas accessible" -ForegroundColor Red
}

# Vérifier que PostgreSQL est accessible
try {
    docker-compose exec -T postgres pg_isready -U notifuser | Out-Null
    Write-Host "✅ PostgreSQL est prêt" -ForegroundColor Green
} catch {
    Write-Host "❌ PostgreSQL n'est pas accessible" -ForegroundColor Red
}

# Installer les dépendances
if (-not (Test-Path node_modules)) {
    Write-Host "📦 Installation des dépendances..." -ForegroundColor Yellow
    pnpm install
    Write-Host "✅ Dépendances installées" -ForegroundColor Green
}

# Générer le client Prisma
Write-Host "🔧 Génération du client Prisma..." -ForegroundColor Yellow
pnpm prisma generate
Write-Host "✅ Client Prisma généré" -ForegroundColor Green

# Demander si on applique les migrations
$response = Read-Host "Voulez-vous appliquer les migrations Prisma ? (y/n)"
if ($response -eq "y" -or $response -eq "Y") {
    Write-Host "🔄 Application des migrations Prisma..." -ForegroundColor Yellow
    pnpm prisma migrate deploy
    Write-Host "✅ Migrations appliquées" -ForegroundColor Green
}

Write-Host ""
Write-Host "🎉 Configuration terminée !" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Services disponibles :" -ForegroundColor Cyan
Write-Host "  - Redis:            localhost:6379"
Write-Host "  - PostgreSQL:       localhost:5432"
Write-Host "  - Redis Commander:  http://localhost:8081"
Write-Host "  - pgAdmin:          http://localhost:5050 (admin@notification.local / admin)"
Write-Host ""
Write-Host "🚀 Démarrer l'application :" -ForegroundColor Cyan
Write-Host "  pnpm run start:dev"
Write-Host ""
Write-Host "📊 Une fois l'app démarrée :" -ForegroundColor Cyan
Write-Host "  - API:              http://localhost:3000"
Write-Host "  - BullBoard:        http://localhost:3000/queues"
Write-Host "  - Health Check:     http://localhost:3000/health"
Write-Host ""
