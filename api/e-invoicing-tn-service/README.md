# El Fatoora - Module de Facturation Électronique Tunisienne

Ce module permet la génération de factures électroniques conformes au système tunisien **El Fatoora** (TEIF - Tunisian Electronic Invoice Format).

## Fonctionnalités

- **Génération XML** : Création de documents XML conformes au schéma XSD El Fatoora v1.8.8
- **Signature XAdES-EPES** : Signature électronique conforme à la politique tunisienne
- **Validation XSD** : Validation automatique des documents générés
- **Calculs automatiques** : Calcul des totaux, TVA et montants des lignes
- **API REST** : Endpoints pour la génération et validation des factures

## Architecture

```
elfatoora/
├── config/
│   ├── ElFatooraConfiguration.java    # Configuration Spring
│   └── ElFatooraProperties.java       # Propriétés externalisées
├── controller/
│   └── ElFatooraController.java       # API REST
├── exception/
│   ├── ElFatooraException.java        # Exception personnalisée
│   ├── ElFatooraExceptionHandler.java # Gestionnaire d'erreurs
│   └── ErrorCode.java                 # Codes d'erreur
├── model/
│   ├── dto/                           # DTOs pour l'API
│   │   ├── ElFatooraInvoiceDTO.java
│   │   ├── SupplierDTO.java
│   │   ├── CustomerDTO.java
│   │   ├── InvoiceLineDTO.java
│   │   └── ...
│   └── generated/                     # Classes JAXB (auto-générées)
└── service/
    ├── ElFatooraService.java          # Service orchestrateur (facade)
    ├── ElFatooraXmlGeneratorService.java  # Génération XML
    └── XadesSignatureService.java     # Signature XAdES
```

## Configuration

### application.yml

```yaml
elfatoora:
  # Validation XSD
  xsd:
    path: classpath:schema/facture_INVOIC_V1.8.8_withSig.xsd
    validation-enabled: true

  # Certificat de signature (PKCS#12)
  certificate:
    path: ${ELFATOORA_CERT_PATH}
    password: ${ELFATOORA_CERT_PASSWORD}
    alias: 1
    type: PKCS12
    required: true

  # Configuration signature XAdES
  signature:
    signer-role: CEO
    policy-oid: urn:2.16.788.1.2.1
```

### Variables d'environnement

| Variable | Description | Requis |
|----------|-------------|--------|
| `ELFATOORA_CERT_PATH` | Chemin vers le certificat PKCS#12 | Oui (si signature requise) |
| `ELFATOORA_CERT_PASSWORD` | Mot de passe du certificat | Oui (si signature requise) |
| `ELFATOORA_CERT_ALIAS` | Alias dans le keystore | Non (défaut: 1) |

## Utilisation

### API REST

#### Générer une facture signée

```http
POST /api/invoices/elfatoora/generate
Content-Type: application/json

{
  "invoiceNumber": "FAC_2024_001",
  "invoiceDate": "2024-06-07",
  "dueDate": "2024-07-07",
  "documentType": "INVOICE",
  "supplier": {
    "taxIdentifier": "0736202XAM000",
    "companyName": "Ma Société SA",
    "address": {
      "city": "Tunis",
      "postalCode": "1000",
      "country": "TN"
    }
  },
  "customer": {
    "taxIdentifier": "0914089JAM000",
    "companyName": "Client SARL",
    "customerType": "SMTP",
    "taxRegime": "P",
    "address": {
      "city": "Sfax",
      "postalCode": "3000",
      "country": "TN"
    }
  },
  "lines": [
    {
      "lineNumber": 1,
      "itemCode": "PROD001",
      "itemDescription": "Produit A",
      "unitType": "UNIT",
      "quantity": 10,
      "unitPrice": 50.000,
      "taxRate": 19
    }
  ],
  "currency": "TND"
}
```

#### Valider une facture

```http
POST /api/invoices/elfatoora/validate
Content-Type: application/json

{
  // ... données facture
}
```

#### Vérifier une signature

```http
POST /api/invoices/elfatoora/verify-signature
Content-Type: application/xml

<?xml version="1.0" encoding="UTF-8"?>
<TEIF ...>
  <!-- XML signé -->
</TEIF>
```

### Code Java

```java
@Autowired
private ElFatooraService elFatooraService;

// Générer une facture
ElFatooraInvoiceDTO invoice = ElFatooraInvoiceDTO.builder()
    .invoiceNumber("FAC_2024_001")
    .invoiceDate(LocalDate.now())
    .documentType(DocumentType.INVOICE)
    .supplier(supplierDTO)
    .customer(customerDTO)
    .lines(List.of(lineDTO))
    .build();

ElFatooraResult result = elFatooraService.generateInvoice(invoice);

// Accéder au XML signé
String signedXml = result.getSignedXml();
```

## Codes El Fatoora

### Types de documents (DocumentType)

| Code | Description |
|------|-------------|
| I-11 | Facture |
| I-12 | Avoir |
| I-13 | Note de débit |
| I-14 | Facture proforma |
| I-15 | Auto-facturation |
| I-16 | Autre |

### Types d'identifiants (IdentifierType)

| Code | Description | Format |
|------|-------------|--------|
| I-01 | Matricule Fiscal | 7 chiffres + 1 lettre + AM + 3 chiffres (ex: 0736202XAM000) |
| I-02 | CIN | 8 chiffres |
| I-03 | Carte de Séjour | 9 chiffres |
| I-04 | Autre | Libre |

### Taux de TVA Tunisie

| Taux | Application |
|------|-------------|
| 0% | Exonéré |
| 7% | Taux réduit |
| 13% | Taux intermédiaire |
| 19% | Taux normal |

### Types de paiement (PaymentMethod)

| Code | Description |
|------|-------------|
| I-111 | Espèces |
| I-112 | Chèque |
| I-113 | Carte de crédit |
| I-114 | Virement bancaire |
| I-115 | Mandat postal / CCP |
| I-116 | Prélèvement |

### Codes de taxe (TaxTypeCode)

| Code | Description |
|------|-------------|
| I-1601 | Droit de timbre |
| I-1602 | TVA |
| I-1603 | FODEC |

## Obtention du Certificat TnTrust

Pour signer les factures électroniques, vous devez obtenir un certificat qualifié auprès de l'ANCE (Agence Nationale de Certification Électronique) :

1. **Contactez l'ANCE** : [www.tuntrust.tn](https://www.tuntrust.tn)
2. **Type de certificat** : Certificat qualifié de signature (Qualified Certificate)
3. **Format** : PKCS#12 (.p12)

### Installation du certificat

1. Placez le fichier `.p12` dans un emplacement sécurisé
2. Configurez le chemin via la variable `ELFATOORA_CERT_PATH`
3. Configurez le mot de passe via la variable `ELFATOORA_CERT_PASSWORD`

**⚠️ NE JAMAIS commiter les certificats dans Git !**

## Formats de données

### Montants
- Précision : 3 décimales
- Arrondi : HALF_UP
- Exemple : `1234.567`

### Dates
- Format facture : `ddMMyy` (ex: 070624 pour 07/06/2024)
- Format période : `ddMMyy-ddMMyy` (ex: 010524-310524)

### Identifiant fiscal (Matricule Fiscal)
```
Format: NNNNNNNXAMZZZ
- NNNNNNN : 7 chiffres
- X : 1 lettre (clé)
- A : Type (A, B, D, N, P)
- M : Catégorie (C, M, N, P)
- ZZZ : 3 chiffres (établissement)

Exemple: 0736202XAM000
```

## Tests

```bash
# Exécuter tous les tests
mvn test -pl e-invoicing-tn-service

# Exécuter les tests d'un service spécifique
mvn test -pl e-invoicing-tn-service -Dtest=ElFatooraServiceTest
```

## Génération des classes JAXB

Les classes JAXB sont générées automatiquement à partir du XSD lors du build :

```bash
mvn generate-sources -pl e-invoicing-tn-service
```

Les classes sont générées dans : `target/generated-sources/jaxb/`

## Dépannage

### Erreur de validation XSD

Si vous obtenez une erreur de validation XSD, vérifiez :
1. La structure XML générée correspond au schéma
2. Tous les champs obligatoires sont renseignés
3. Les formats de données sont corrects (dates, montants, identifiants)

### Erreur de signature

Si la signature échoue, vérifiez :
1. Le certificat est valide et non expiré
2. Le mot de passe est correct
3. L'alias correspond à une clé existante dans le keystore

### Certificate not found

Si le certificat n'est pas trouvé :
1. Vérifiez le chemin du fichier
2. Utilisez `file:` pour les chemins absolus
3. Vérifiez les permissions de lecture

## Références

- [Spécifications El Fatoora](https://www.tradenet.com.tn)
- [ANCE - Certificats TnTrust](https://www.tuntrust.tn)
- [XAdES (XML Advanced Electronic Signatures)](https://www.etsi.org/technologies/electronic-signatures)

## Support

Pour toute question ou problème, contactez l'équipe de développement ou ouvrez une issue sur le repository.
