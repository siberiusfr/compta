# Script PowerShell de correction automatique des vues

Write-Host "=== Script de correction automatique des vues ===" -ForegroundColor Cyan
Write-Host ""

# Dictionnaire des stores et leurs données
$storeData = @{
    'permissions' = @{
        store = 'usePermissions'
        dataItems = @('roles', 'users', 'auditLogs')
        isCollection = @('roles', 'users', 'auditLogs')
    }
    'companies' = @{
        store = 'useCompanies'
        dataItems = @('companies')
        isCollection = @('companies')
    }
    'hr' = @{
        store = 'useHr'
        dataItems = @('employees', 'contracts', 'leaveRequests', 'payroll')
        isCollection = @('employees', 'contracts', 'leaveRequests', 'payroll')
    }
    'accounting' = @{
        store = 'useAccounting'
        dataItems = @('journalEntries', 'ledgerAccounts', 'invoices', 'expenses')
        isCollection = @('journalEntries', 'ledgerAccounts', 'invoices', 'expenses')
    }
    'documents' = @{
        store = 'useDocuments'
        dataItems = @('documents', 'categories')
        isCollection = @('documents', 'categories')
    }
}

function Add-SafeCheckScript {
    param(
        [string]$FilePath,
        [hashtable]$ModuleData
    )
    
    Write-Host "Traitement: $FilePath" -ForegroundColor Yellow
    
    $content = Get-Content -Path $FilePath -Raw
    $moduleName = [System.IO.Path]::GetFileName([System.IO.Path]::GetDirectoryName($FilePath))
    
    # Vérifier si déjà corrigé
    if ($content -match "const safe") {
        Write-Host "  Déjà corrigé, ignoré" -ForegroundColor Gray
        return $false
    }
    
    $scriptSection = @"
import { computed } from 'vue'
import { $($ModuleData.store) } from '../composables/use$($moduleName)'
"@
    
    # Trouver les données utilisées dans le store
    foreach ($item in $ModuleData.dataItems) {
        if ($content -match "const \{\s*$item,") {
            $scriptSection += @"
const safe$($item.Substring(0, 1).ToUpper() + $item.Substring(1)) = computed(() => {
  if (!$($item).value || !Array.isArray($($item).value)) {
    return []
  }
  return $($item).value
})
"@
        }
    }
    
    # Insérer après les imports et avant le reste du script
    if ($content -match "<script setup lang=`"ts`">") {
        $newContent = $content -replace "(<script setup lang=`"ts`">)", "`$1`n`n$scriptSection`n"
        
        Set-Content -Path $FilePath -Value $newContent
        Write-Host "  ✅ Safe checks ajoutés" -ForegroundColor Green
        return $true
    }
    
    Write-Host "  ❌ Impossible de modifier" -ForegroundColor Red
    return $false
}

# Parcourir tous les modules
$fixedFiles = 0
$totalFiles = 0

foreach ($module in $storeData.Keys) {
    $moduleDir = "src/modules/$module/views"
    
    if (Test-Path $moduleDir) {
        Write-Host "`n=== Module: $module ===" -ForegroundColor Cyan
        
        $vueFiles = Get-ChildItem -Path $moduleDir -Filter "*.vue"
        
        foreach ($file in $vueFiles) {
            $totalFiles++
            
            # Ne pas corriger les vues déjà corrigées
            if ($file.Name -match "NotificationsInbox|NotificationsSent|NotificationsSettings") {
                continue
            }
            
            if (Add-SafeCheckScript -FilePath $file.FullName -ModuleData $storeData[$module]) {
                $fixedFiles++
            }
        }
    }
}

Write-Host ""
Write-Host "=== Résultat ===" -ForegroundColor Cyan
Write-Host "Fichiers analysés: $totalFiles" -ForegroundColor White
Write-Host "Fichiers corrigés: $fixedFiles" -ForegroundColor Green
Write-Host ""

if ($fixedFiles -gt 0) {
    Write-Host "✅ Correction automatique terminée!" -ForegroundColor Green
    Write-Host "Veuillez tester l'application maintenant." -ForegroundColor Cyan
} else {
    Write-Host "ℹ️  Aucun fichier nouveau à corriger" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Note: Certains vues pourraient nécessiter une correction manuelle" -ForegroundColor Cyan
Write-Host "Voir CORRECTIONS.md pour le guide détaillé." -ForegroundColor Cyan
