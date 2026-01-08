# Script PowerShell pour corriger les vues avec des erreurs de données undefined

$vueFiles = Get-ChildItem -Path "src/modules" -Recurse -Filter "*.vue"

foreach ($file in $vueFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $originalContent = $content
    
    # Vérifier si le fichier utilise déjà des .value directs sans vérification
    if ($content -match '\.value\s*\[') {
        Write-Host "Analyse de $($file.Name)..." -ForegroundColor Yellow
        
        # Pattern: identifier les computed qui utilisent directement .value
        $hasIssue = $false
        
        # Chercher les patterns dangereux
        $dangerousPatterns = @(
            '\.value\.length',
            '\.value\.slice',
            '\.value\.map',
            '\.value\.filter',
            '\.value\.reduce',
            'v-for="\w+\s+in\s+\w+\.value"'
        )
        
        foreach ($pattern in $dangerousPatterns) {
            if ($content -match $pattern) {
                Write-Host "  ⚠️  Pattern détecté: $pattern" -ForegroundColor Red
                $hasIssue = $true
            }
        }
        
        if ($hasIssue) {
            Write-Host "  ❌ Fichier nécessite une correction: $($file.FullName)" -ForegroundColor Red
        } else {
            Write-Host "  ✅ Fichier OK" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "Analyse terminée. Utilisez le manuel pour corriger les fichiers marqués." -ForegroundColor Cyan
Write-Host "Voir VUES_FIX.md pour le guide de correction." -ForegroundColor Cyan
