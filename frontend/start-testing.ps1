# Script de test rapide de l'application

Write-Host "=== Test de l'application ===" -ForegroundColor Cyan
Write-Host ""

# Vérifier que le serveur fonctionne
Write-Host "1. Vérification du serveur..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -Method GET -UseBasicParsing
    Write-Host "   ✅ Serveur fonctionne (Statut: $($response.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Serveur ne répond pas. Veuillez lancer 'npm run dev'" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. URLs à tester:" -ForegroundColor Cyan
Write-Host ""
Write-Host "   📋 Test de base:" -ForegroundColor Yellow
Write-Host "      http://localhost:3000/test" -ForegroundColor White
Write-Host ""
Write-Host "   🏠 Dashboard:" -ForegroundColor Yellow
Write-Host "      http://localhost:3000/dashboard" -ForegroundColor White
Write-Host ""
Write-Host "   🔔 Notifications:" -ForegroundColor Yellow
Write-Host "      http://localhost:3000/notifications/inbox" -ForegroundColor White
Write-Host "      http://localhost:3000/notifications/sent" -ForegroundColor White
Write-Host "      http://localhost:3000/notifications/settings" -ForegroundColor White
Write-Host ""
Write-Host "   🔐 OAuth:" -ForegroundColor Yellow
Write-Host "      http://localhost:3000/oauth/applications" -ForegroundColor White
Write-Host "      http://localhost:3000/oauth/tokens" -ForegroundColor White
Write-Host "      http://localhost:3000/oauth/consents" -ForegroundColor White
Write-Host ""
Write-Host "   📄 Documents:" -ForegroundColor Yellow
Write-Host "      http://localhost:3000/documents/all" -ForegroundColor White
Write-Host ""

Write-Host "3. Actions à faire:" -ForegroundColor Cyan
Write-Host ""
Write-Host "   a. Testez chaque URL ci-dessus" -ForegroundColor White
Write-Host "   b. Si vous voyez une erreur, ErrorBoundary l'affichera" -ForegroundColor White
Write-Host "   c. Notez les erreurs et la pile d'appels" -ForegroundColor White
Write-Host "   d. Rafraîchissez la page (F5) pour tester le rechargement" -ForegroundColor White
Write-Host "   e. Arrêtez et redémarrez le serveur pour tester avec les tokens" -ForegroundColor White
Write-Host ""
Write-Host "4. Vérifications importantes:" -ForegroundColor Cyan
Write-Host ""
Write-Host "   ✅ Authentification OAuth2: http://localhost:3000/login" -ForegroundColor Green
Write-Host "   ✅ Page de test: http://localhost:3000/test" -ForegroundColor Green
Write-Host "   ✅ Dashboard: http://localhost:3000/dashboard" -ForegroundColor Green
Write-Host "   ✅ Notifications: http://localhost:3000/notifications/inbox" -ForegroundColor Green
Write-Host "   ✅ OAuth: http://localhost:3000/oauth/applications" -ForegroundColor Green
Write-Host "   ✅ Documents: http://localhost:3000/documents/all" -ForegroundColor Green
Write-Host ""
Write-Host "5. Outils de débogage:" -ForegroundColor Cyan
Write-Host ""
Write-Host "   🖥️  Ouvrez les outils de développement (F12)" -ForegroundColor Yellow
Write-Host "   📊  Onglet 'Console' pour voir les erreurs JavaScript" -ForegroundColor Yellow
Write-Host "   📝  ErrorBoundary affichera les erreurs avec détails" -ForegroundColor Yellow
Write-Host "   🔍  Voir DEBUG.md pour plus de détails sur le débogage" -ForegroundColor Yellow
Write-Host ""
Write-Host "6. Si tout fonctionne:" -ForegroundColor Green
Write-Host "   ✅ Toutes les pages s'affichent correctement" -ForegroundColor Green
Write-Host "   ✅ Pas d'erreurs dans la console" -ForegroundColor Green
Write-Host "   ✅ Les données mock s'affichent correctement" -ForegroundColor Green
Write-Host "   ✅ La navigation fonctionne" -ForegroundColor Green
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan

Read-Host "Appuyez sur ENTREE pour ouvrir l'application dans le navigateur..."
