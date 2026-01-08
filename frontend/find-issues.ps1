# Script PowerShell pour corriger automatiquement les vues avec problèmes de données undefined

$modulesDir = "src/modules"

# Fonction pour ajouter une vérification de sécurité aux tableaux
function Add-SafeArrayCheck {
    param(
        [string]$Content,
        [string]$StoreVariable
    )
    
    # Pattern: const { items } = useStore()
    # Remplacer par: const { items } = useStore()
    #            const safeItems = computed(() => {
    #              if (!items.value || !Array.isArray(items.value)) { return [] }
    #              return items.value
    #            })
    
    $content -replace "($StoreVariable = useStore\(\))", "`$1`n`nconst safe$($StoreVariable.Substring(1)) = computed(() => {`n  if (!$($StoreVariable).value || !Array.isArray($($StoreVariable).value)) {`n    return []`n  }`n  return $($StoreVariable).value`n})"
}

Write-Host "Script de correction des vues" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Trouver tous les fichiers .vue dans les modules
$vueFiles = Get-ChildItem -Path $modulesDir -Recurse -Filter "*.vue"

$fixedFiles = @()
$skippedFiles = @()

foreach ($file in $vueFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    
    # Vérifier si le fichier a déjà un safe check
    if ($content -match "safe\w+\s*=") {
        Write-Host "⏭️  Skipping $($file.Name) (déjà corrigé)" -ForegroundColor Gray
        $skippedFiles += $file.FullName
        continue
    }
    
    # Vérifier si le fichier utilise .value directement dans les v-for
    if ($content -match 'v-for="\w+\s+in\s+\w+\.value"') {
        Write-Host "⚠️  Found issue in: $($file.Name)" -ForegroundColor Yellow
        
        # Extraire le nom du store
        if ($content -match "const \{\s*(\w+)\s*\}.*=.*use\w+\(\)") {
            $storeVar = $Matches[1]
            Write-Host "   Store variable: $storeVar" -ForegroundColor Cyan
        }
        
        $fixedFiles += $file.FullName
    }
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Résultat:" -ForegroundColor Cyan
Write-Host "  Fichiers nécessitant une correction: $($fixedFiles.Count)" -ForegroundColor Yellow
Write-Host "  Fichiers déjà corrigés: $($skippedFiles.Count)" -ForegroundColor Green
Write-Host ""

if ($fixedFiles.Count -gt 0) {
    Write-Host "Fichiers à corriger:" -ForegroundColor Yellow
    foreach ($file in $fixedFiles) {
        Write-Host "  - $($file)" -ForegroundColor White
    }
    
    Write-Host ""
    Write-Host "Veuillez corriger manuellement les fichiers ci-dessus en suivant le guide dans VUES_FIX.md" -ForegroundColor Cyan
} else {
    Write-Host "✅ Tous les fichiers ont déjà été corrigés!" -ForegroundColor Green
}
