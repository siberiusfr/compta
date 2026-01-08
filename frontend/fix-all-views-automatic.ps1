# Script PowerShell pour corriger toutes les vues automatiquement

$ErrorActionPreference = "Continue"

Write-Host "=== Script de correction automatique des vues ===" -ForegroundColor Cyan
Write-Host ""

$fixedCount = 0
$skippedCount = 0
$totalFiles = 0

# Fichiers à corriger (priorité haute)
$viewsToFix = @(
    "DocumentsCategories.vue",
    "DocumentsUpload.vue",
    "PermissionsRoles.vue",
    "PermissionsUsers.vue",
    "PermissionsAudit.vue",
    "CompaniesAll.vue",
    "CompaniesCreate.vue",
    "CompaniesSettings.vue",
    "HrEmployees.vue",
    "HrContracts.vue",
    "HrLeaves.vue",
    "HrPayroll.vue",
    "AccountingJournal.vue",
    "AccountingLedger.vue",
    "AccountingIncomeStatement.vue",
    "AccountingInvoices.vue",
    "AccountingReports.vue"
)

function Fix-View {
    param(
        [string]$ModuleDir,
        [string]$ViewName
    )
    
    $filePath = "$ModuleDir\$ViewName"
    
    Write-Host "Traitement: $ViewName" -ForegroundColor Yellow
    
    if (-not (Test-Path $filePath)) {
        Write-Host "  ⚠️  Fichier non trouvé" -ForegroundColor Red
        return
    }
    
    $content = Get-Content -Path $filePath -Raw
    
    # Vérifier si déjà corrigé
    if ($content -match "const safe") {
        Write-Host "  Déjà corrigé, ignoré" -ForegroundColor Gray
        $skippedCount++
        return
    }
    
    # Identifier les données du store
    $storeMatches = [regex]::Matches($content, "const \{\s*(\w+)\s*\}.*=.*use\w+\(\)")
    
    if ($storeMatches.Count -eq 0) {
        Write-Host "  ⚠️  Aucun store trouvé" -ForegroundColor Yellow
        return
    }
    
    $storeName = $storeMatches[0].Groups[1].Value
    Write-Host "  Store trouvé: $storeName" -ForegroundColor Cyan
    
    # Trouver toutes les variables utilisées depuis le store
    $variables = [regex]::Matches($content, "const \{\s*(\w+)\s*,.*use.*\}.*=.*use$($storeName)\(\)[\s*(\w+)[^}]*")
    
    Write-Host "  Variables trouvées: $($variables.Count)" -ForegroundColor Cyan
    
    $importSection = @"

import { computed } from 'vue'
import { $storeName } from '../composables/use$($storeName.Substring(1).ToUpper())'
"@

    $safeComputeds = @()

    foreach ($var in $variables) {
        $varName = $var.Groups[1].Value
        $safeVarName = "safe" + $varName.Substring(0, 1).ToUpper() + $varName.Substring(1)
        
        $safeComputeds += @"

const $($safeVarName) = computed(() => {
  if (!$($varName).value || !Array.isArray($($varName).value)) {
    return []
  }
  return $($varName).value
})
"@
    }
    
    # Trouver l'endroit où insérer (après les imports)
    if ($content -match "(?s*)(<script.*setup|<script>)(?=.*$storeMatches[0].Groups[2].Value)") {
        $insertPos = $content.LastIndexOf($matches[0].Groups[0].Value) + $matches[0].Groups[0].Length
        
        $newContent = $content.Insert($insertPos + 1, "`n$($safeComputeds)`n`n")
        
        Set-Content -Path $filePath -Value $newContent
        Write-Host "  ✅ Safe checks ajoutés" -ForegroundColor Green
        $fixedCount++
    } else {
        Write-Host "  ❌ Impossible de trouver l'emplacement d'insertion" -ForegroundColor Red
    }
}

# Parcourir tous les modules
$moduleDirs = @(
    "documents",
    "permissions",
    "companies",
    "hr",
    "accounting"
)

foreach ($module in $moduleDirs) {
    $modulePath = "src/modules/$module/views"
    
    if (-not (Test-Path $modulePath)) {
        Write-Host "Module non trouvé: $module" -ForegroundColor Yellow
        continue
    }
    
    Write-Host "`n=== Module: $module ===" -ForegroundColor Cyan
    
    foreach ($view in $viewsToFix) {
        Fix-View -ModuleDir $modulePath -ViewName $view
    }
}

Write-Host ""
Write-Host "=== Résultat ===" -ForegroundColor Cyan
Write-Host "Total fichiers analysés: $($totalFiles)" -ForegroundColor White
Write-Host "Fichiers corrigés: $fixedCount" -ForegroundColor Green
Write-Host "Fichiers ignorés: $skippedCount" -ForegroundColor Yellow
Write-Host ""

if ($fixedCount -gt 0) {
    Write-Host "✅ Correction terminée! $fixedCount fichier(s) corrigé(s)" -ForegroundColor Green
} else {
    Write-Host "ℹ️  Aucun fichier corrigé. Certains ont été déjà traités." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Veuillez tester l'application maintenant." -ForegroundColor Cyan
