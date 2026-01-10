# AGENTS.md

Instructions pour les agents IA travaillant sur le backend Java/Spring Boot.

## Stack Technique

- Java 21, Spring Boot 3.5.9, Spring Cloud 2025.0.1
- PostgreSQL 16 avec isolation par schéma
- jOOQ 3.19.28 pour les requêtes type-safe
- Flyway pour les migrations
- MapStruct 1.6.3 pour le mapping DTO
- Lombok 1.18.42

## Structure des Services

```
api/
├── compta-commons/          # Configuration partagée Spring Boot
├── compta-security-commons/ # Utilitaires JWT, encryption
├── notification-contracts/  # DTOs partagés avec notification-service
├── gateway/                 # API Gateway (port 8080)
├── auth-service/            # Authentification (port 8081)
├── accounting-service/      # Comptabilité (port 8082)
├── document-service/        # GED MinIO (port 8083)
├── hr-service/              # RH/Paie (port 8084)
├── authz-service/           # Permissions
├── migration-service/       # Migrations Flyway centralisées
├── notification-service/    # NestJS (port 3001)
└── oauth2-server/           # OAuth2/OIDC
```

## Commandes Essentielles

```bash
# Build complet
mvn clean install

# Build un service spécifique
mvn clean install -pl auth-service -am

# Lancer en dev
cd auth-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Tests
mvn test
mvn test -Dtest=NomDuTest

# Formater le code (obligatoire avant commit)
mvn spotless:apply

# Régénérer les classes jOOQ après changement de schéma
mvn clean generate-sources
```

## Conventions de Code

### Repositories (jOOQ)

```java
@Repository
@RequiredArgsConstructor
public class ExampleRepository {
    private final DSLContext dsl;

    public Optional<Example> findById(Long id) {
        return dsl.selectFrom(EXAMPLE)
            .where(EXAMPLE.ID.eq(id))
            .fetchOptional()
            .map(r -> r.into(Example.class));
    }

    public List<Example> findAll() {
        return dsl.selectFrom(EXAMPLE)
            .fetch()
            .into(Example.class);
    }
}
```

### Services

```java
@Service
@RequiredArgsConstructor
@Transactional
public class ExampleService {
    private final ExampleRepository exampleRepository;
    private final ExampleMapper mapper;

    public ExampleResponse create(CreateExampleRequest request) {
        Example entity = mapper.toEntity(request);
        Example saved = exampleRepository.save(entity);
        return mapper.toResponse(saved);
    }
}
```

### Controllers

```java
@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
@Tag(name = "Examples")
public class ExampleController {
    private final ExampleService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un exemple")
    public ResponseEntity<ExampleResponse> create(@Valid @RequestBody CreateExampleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
}
```

### DTOs

- Utiliser des records Java pour les DTOs immutables
- Annoter avec `@Schema` pour OpenAPI
- Valider avec Jakarta Validation (`@NotNull`, `@Size`, etc.)

```java
public record CreateExampleRequest(
    @NotBlank @Size(max = 100) @Schema(description = "Nom") String name,
    @Schema(description = "Description optionnelle") String description
) {}
```

## Migrations de Base de Données

Les migrations sont centralisées dans `migration-service/src/main/resources/db/migration/`.

```sql
-- V10__add_example_table.sql
CREATE TABLE IF NOT EXISTS accounting.example (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_example_company ON accounting.example(company_id);
```

**Règles:**
- Toujours préfixer les tables avec le schéma (`auth.`, `accounting.`, `hr.`, `document.`)
- Utiliser `IF NOT EXISTS` / `IF EXISTS` pour l'idempotence
- Ne jamais modifier une migration déjà appliquée

## Multi-Tenancy

- Header `X-Company-Id` requis sur toutes les requêtes authentifiées
- Toutes les entités métier ont une colonne `company_id`
- Filtrer systématiquement par `company_id` dans les requêtes

## Sécurité

- Rôles: `ADMIN`, `COMPTABLE`, `SOCIETE`, `EMPLOYEE`
- Utiliser `@PreAuthorize` sur les endpoints
- JWT validé au niveau du gateway

## Tests

```java
@SpringBootTest
@Testcontainers
class ExampleServiceTest extends AbstractIntegrationTest {
    @Autowired
    private ExampleService service;

    @Test
    void shouldCreateExample() {
        // given
        var request = new CreateExampleRequest("Test", "Description");

        // when
        var response = service.create(request);

        // then
        assertThat(response.name()).isEqualTo("Test");
    }
}
```

## Points d'Attention

1. **Ne pas éditer le code généré** dans `src/generated/jooq/`
2. **Toujours formater** avec `mvn spotless:apply` avant de commiter
3. **Préfixer les tables** avec le nom du schéma dans les migrations
4. **Utiliser MapStruct** pour les conversions DTO ↔ Entity
5. **Documenter les endpoints** avec les annotations OpenAPI
