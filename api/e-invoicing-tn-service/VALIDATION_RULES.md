# Règles de Validation El Fatoora

Ce document décrit toutes les règles de validation métier implémentées pour la génération de factures électroniques tunisiennes (TEIF v1.8.8).

## Table des matières

- [Identifiants Fiscaux](#identifiants-fiscaux)
- [Types de Documents](#types-de-documents)
- [Dates](#dates)
- [Montants et Calculs](#montants-et-calculs)
- [Taux de TVA](#taux-de-tva)
- [Partenaires](#partenaires)
- [Codes et Énumérations](#codes-et-énumérations)

---

## Identifiants Fiscaux

### Matricule Fiscal Tunisien (Type I-01)

**Format**: `NNNNNNNLPCE99` (13 caractères exactement)

| Position | Description | Valeurs autorisées |
|----------|-------------|-------------------|
| 1-7 | Numéro d'identification | Chiffres 0-9 |
| 8 | Lettre de contrôle | A-Z sauf I et O |
| 9 | Position fiscale | A, B, D, N, P |
| 10 | Catégorie | C, M, N, P, E |
| 11-13 | Code établissement | 000-999 |

**Positions fiscales**:
- `A` : Personne physique
- `B` : Société
- `D` : Établissement stable
- `N` : Non résident
- `P` : Professionnel libéral

**Catégories**:
- `C` : Commercial
- `M` : Manufacturier
- `N` : Non commercial
- `P` : Prestataire de services
- `E` : Exportateur

**Exemples valides**:
- `0736202XAM000` (Exemple officiel TTN)
- `1234567ABC123`
- `9999999ZPE999`

**Exemples invalides**:
- `0736202IAM000` ❌ (lettre I interdite)
- `0736202OAM000` ❌ (lettre O interdite)
- `0736202XXM000` ❌ (position X invalide)
- `0736202XAM00` ❌ (trop court)

### CIN - Carte d'Identité Nationale (Type I-02)

**Format**: 8 chiffres exactement

**Exemples valides**:
- `12345678`
- `00000001`

**Exemples invalides**:
- `1234567` ❌ (7 chiffres)
- `123456789` ❌ (9 chiffres)
- `1234567A` ❌ (contient une lettre)

### Carte de Séjour / Passeport (Type I-03)

**Format**: 9 chiffres exactement

**Exemples valides**:
- `123456789`
- `000000001`

### Autre Identifiant (Type I-04)

**Format**: Chaîne libre, maximum 35 caractères

---

## Types de Documents

| Code | Type | Description |
|------|------|-------------|
| I-11 | INVOICE | Facture standard |
| I-12 | CREDIT_NOTE | Avoir (note de crédit) |
| I-13 | DEBIT_NOTE | Facture de débit |
| I-14 | SIMPLIFIED_INVOICE | Facture simplifiée |
| I-15 | SELF_BILLING_INVOICE | Facture d'auto-facturation |
| I-16 | CORRECTIVE_INVOICE | Facture rectificative |

**Règles spéciales**:
- `I-12` (Avoir) : Doit référencer la facture d'origine
- `I-16` (Rectificative) : Doit référencer la facture d'origine

---

## Dates

### Formats supportés

| Format | Description | Exemple | Longueur |
|--------|-------------|---------|----------|
| `ddMMyy` | Date simple | 150625 | 6 |
| `ddMMyyHHmm` | Date avec heure | 1506251230 | 10 |
| `ddMMyy-ddMMyy` | Période | 010625-300625 | 13 |

### Codes fonctionnels de date

| Code | Description | Format | Obligatoire |
|------|-------------|--------|-------------|
| I-31 | Date de facture | ddMMyy | ✅ Oui |
| I-32 | Date d'échéance | ddMMyy | Non |
| I-33 | Date de livraison | ddMMyy | Non |
| I-34 | Date de commande | ddMMyy | Non |
| I-35 | Date de contrat | ddMMyy | Non |
| I-36 | Période de service | ddMMyy-ddMMyy | Non |
| I-37 | Date validation TTN | ddMMyyHHmm | Non |
| I-38 | Date personnalisée | ddMMyy | Non |

### Règles de cohérence

1. **Date d'échéance** ≥ Date de facture
2. **Période**: Date début ≤ Date fin
3. **Date de facture**: Ne devrait pas être dans le futur (avertissement)

---

## Montants et Calculs

### Précision

- **Toutes les valeurs monétaires**: 3 décimales (millimes)
- **Mode d'arrondi**: HALF_UP
- **Tolérance d'arrondi**: ±0.001 TND

### Calculs de ligne

```
lineAmountExclTax = quantity × unitPrice
taxAmount = lineAmountExclTax × (taxRate / 100)
lineAmountInclTax = lineAmountExclTax + taxAmount
```

### Calculs de totaux

```
totalExcludingTax = Σ(lineAmountExclTax)
totalTax = Σ(taxAmount)
totalIncludingTax = totalExcludingTax + totalTax
```

### Règles de validation

1. Tous les montants doivent être ≥ 0 (sauf avoirs)
2. `totalIncludingTax` = `totalExcludingTax` + `totalTax`
3. La somme des lignes doit correspondre aux totaux

---

## Taux de TVA

### Taux autorisés en Tunisie

| Taux | Code | Description |
|------|------|-------------|
| 19% | I-91 | Taux normal |
| 13% | I-92 | Taux réduit |
| 7% | I-93 | Taux super-réduit |
| 0% | I-94 | Exonéré / Export |

### Codes de type de taxe

| Code | Description |
|------|-------------|
| I-161 | TVA |
| I-162 | Droit de timbre |
| I-163 | TCL |
| I-164 | FODEC |
| I-165 | FOPROLOS |
| I-166 | Droit de consommation |
| I-167 | Retenue à la source |
| I-168 | Taxe additionnelle |
| I-169 | Autres taxes |
| I-160 | Exonéré de TVA |
| I-1601 | Suspension TVA |
| I-1602 | Régime forfaitaire |
| I-1603 | Exportation |

---

## Partenaires

### Codes fonctionnels

| Code | Description | Obligatoire |
|------|-------------|-------------|
| I-62 | Fournisseur (vendeur) | ✅ Oui |
| I-64 | Client (acheteur) | ✅ Oui |
| I-61 | Débiteur | Non |
| I-63 | Créditeur | Non |
| I-65 | Destinataire final | Non |
| I-66 | Payeur | Non |
| I-67 | Agent | Non |
| I-68 | Autre partenaire | Non |

### Type de client

| Code | Description |
|------|-------------|
| SMTP | Sujet Moral Tunisien Passible (entreprise assujettie) |
| SMPP | Sujet Moral ou Physique Particulier (non assujetti) |

### Régime fiscal

| Code | Description |
|------|-------------|
| P | Passible (assujetti à la TVA) |
| NP | Non Passible (non assujetti) |

---

## Codes et Énumérations

### Moyens de communication (I-10x)

| Code | Description |
|------|-------------|
| I-101 | Téléphone |
| I-102 | Fax |
| I-103 | Email |
| I-104 | Site web |

### Comptes financiers (I-14x)

| Code | Description |
|------|-------------|
| I-141 | Compte bancaire payeur |
| I-142 | Compte bancaire bénéficiaire |
| I-143 | Compte postal (CCP) |

### Codes de référence (I-8x)

| Code | Description |
|------|-------------|
| I-81 | Bon de commande |
| I-82 | Contrat |
| I-83 | Bon de livraison |
| I-84 | Référence client |
| I-85 | Référence fournisseur |
| I-86 | Projet |
| I-87 | Facture d'origine |
| I-88 | Numéro de lot |
| I-89 | Numéro de série |
| I-80 | Autre référence |

### Types de montants (I-17x)

| Code | Description | Niveau |
|------|-------------|--------|
| I-171 | Montant ligne HT | Ligne |
| I-172 | Montant ligne TTC | Ligne |
| I-173 | Prix unitaire | Ligne |
| I-174 | Montant taxe ligne | Ligne |
| I-177 | Total HT | Facture |
| I-178 | Total TTC | Facture |
| I-179 | Total TVA | Facture |
| I-181 | Base imposable TVA | Facture |
| I-182 | Montant TVA | Facture |
| I-183 | Montant à payer | Facture |

### Langues

| Code | Description |
|------|-------------|
| fr | Français |
| en | Anglais |
| ar | Arabe |
| or | Langue originale |

### Codes pays

Format ISO 3166-1 alpha-2 (2 lettres)

Exemples:
- `TN` : Tunisie
- `FR` : France
- `DZ` : Algérie

---

## Codes d'erreur

### Erreurs bloquantes

| Code | Description |
|------|-------------|
| ELF_NULL_INVOICE | Facture nulle |
| ELF_MISSING_SUPPLIER_TAX_ID | Matricule fiscal fournisseur manquant |
| ELF_INVALID_TAX_ID_FORMAT | Format matricule fiscal invalide |
| ELF_INVALID_TAX_ID_LENGTH | Longueur matricule fiscal incorrecte |
| ELF_INVALID_TAX_ID_LETTER | Lettre invalide (I ou O) |
| ELF_INVALID_TAX_RATE | Taux TVA non autorisé |
| ELF_MISSING_INVOICE_DATE | Date de facture manquante |
| ELF_INVALID_DATE_FORMAT | Format de date invalide |
| ELF_INVALID_PERIOD | Période invalide |
| ELF_INCORRECT_LINE_AMOUNT | Calcul montant ligne incorrect |
| ELF_INCORRECT_TOTAL_EXCL | Total HT incorrect |
| ELF_INCONSISTENT_TOTALS | Incohérence des totaux |
| ELF_NO_LINES | Aucune ligne de facture |
| ELF_CREDIT_NOTE_MISSING_REFERENCE | Avoir sans référence facture |

### Avertissements

| Code | Description |
|------|-------------|
| ELF_WARN_MISSING_CUSTOMER_TAX_ID | Client sans identifiant fiscal |
| ELF_WARN_FUTURE_DATE | Date de facture dans le futur |
| ELF_WARN_INCOMPLETE_PERIOD | Période partiellement définie |
| ELF_WARN_NON_TND_CURRENCY | Devise autre que TND |
| ELF_WARN_NEGATIVE_QUANTITY | Quantité négative |

---

## Utilisation

### Service de validation

```java
@Autowired
private ElFatooraValidationService validationService;

// Validation complète
ValidationResult result = validationService.validate(invoice);

if (!result.isValid()) {
    // Afficher les erreurs
    System.out.println(result.getFormattedErrorMessage());
}

// Validation partielle
ValidationResult taxResult = validationService.validateTaxIdentifiers(invoice);
ValidationResult dateResult = validationService.validateDates(invoice);
ValidationResult calcResult = validationService.validateCalculations(invoice);
```

### Annotations de validation

```java
public class SupplierDTO {
    @NotBlank
    @ValidTaxIdentifier(type = IdentifierType.I_01)
    private String taxIdentifier;
}

public class InvoiceLineDTO {
    @NotNull
    @ValidTaxRate
    private BigDecimal taxRate;
}

public class AddressDTO {
    @NotBlank
    @ValidCountryCode
    private String country;
}
```
