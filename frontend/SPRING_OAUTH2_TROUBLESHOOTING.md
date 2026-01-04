# Dépannage Spring Authorization Server - Erreur 500

## Problème : Internal Server Error (500) sur /oauth2/authorize

Cette erreur indique que le serveur Spring Authorization Server a rencontré un problème lors du traitement de la demande d'autorisation.

## Causes courantes et solutions

### 1. Client OAuth2 non configuré

**Symptôme** :
```
Client 'public-client' not found
```

**Solution** :
Créez le client dans votre configuration Spring Authorization Server :

```java
@Bean
public CommandLineRunner initializer(RegisteredClientRepository repository) {
    return args -> {
        RegisteredClient.withId("public-client")
            .clientId("public-client")
            .clientSecret(null) // Public client - pas de secret
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://localhost:3000/authorized")
            .scope(OidcScopes.OPENID)
            .scope("read")
            .scope("write")
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .requireProofKey(true) // PKCE requis
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .refreshTokenTimeToLive(Duration.ofHours(24))
                .build())
            .build()
            .apply(repository::save);
    };
}
```

### 2. PKCE non activé

**Symptôme** :
```
PKCE is required for public clients
```

**Solution** :
Assurez-vous que `requireProofKey(true)` est activé dans les client settings :

```java
.clientSettings(ClientSettings.builder()
    .requireProofKey(true) // PKCE requis
    .build())
```

### 3. Redirect URI non configuré

**Symptôme** :
```
Redirect URI 'http://localhost:3000/authorized' not registered for client 'public-client'
```

**Solution** :
Ajoutez le redirect URI dans la configuration du client :

```java
.redirectUri("http://localhost:3000/authorized")
```

### 4. Scopes non définis

**Symptôme** :
```
Scope 'read' not found
```

**Solution** :
Définissez les scopes personnalisés dans votre configuration OAuth2 :

```java
@Bean
public OAuth2AuthorizationService authorizationService(
        JdbcTemplate jdbcTemplate,
        RegisteredClientRepository registeredClientRepository) {
    JdbcOAuth2AuthorizationService service = new JdbcOAuth2AuthorizationService(
        jdbcTemplate, registeredClientRepository);

    // Définir les scopes personnalisés
    OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer = context -> {
        JwtClaimsSet.Builder claims = context.getClaims();
        // Ajouter les scopes personnalisés
        claims.claim("scope", context.getAuthorizedScopes());
    };

    service.setTokenCustomizer(jwtCustomizer);
    return service;
}
```

### 5. Page de login non configurée

**Symptôme** :
```
No explicit mapping for /login
```

**Solution** :
Créez un contrôleur de login :

```java
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // Retourne la page login.html
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password) {
        // Logique d'authentification
        return "redirect:/oauth2/authorize";
    }
}
```

### 6. Problème de base de données

**Symptôme** :
```
Connection refused
```

**Solution** :
Vérifiez que la base de données est démarrée et accessible :

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/oauth2
    username: oauth2
    password: oauth2
  jpa:
    hibernate:
      ddl-auto: update
```

### 7. Problème de configuration JWT

**Symptôme** :
```
JWT signing key not configured
```

**Solution** :
Configurez la clé de signature JWT :

```java
@Bean
public JWKSource<SecurityContext> jwkSource() {
    KeyPair keyPair = generateRsaKey();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

    RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();

    JWKSet jwkSet = new JWKSet(rsaKey);
    return new ImmutableJWKSet<>(jwkSet);
}

private static KeyPair generateRsaKey() {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    return keyPairGenerator.generateKeyPair();
}
```

## Vérification de la configuration

### 1. Vérifier que le client existe

```sql
SELECT * FROM oauth2_registered_client WHERE client_id = 'public-client';
```

### 2. Vérifier les redirect URIs

```sql
SELECT * FROM oauth2_registered_client_redirect_uri
WHERE registered_client_id = 'public-client';
```

### 3. Vérifier les scopes

```sql
SELECT * FROM oauth2_authorization WHERE scopes LIKE '%read%';
```

## Test avec curl

Testez directement le endpoint d'autorisation :

```bash
curl -X GET "http://localhost:9000/oauth2/authorize?client_id=public-client&redirect_uri=http://localhost:3000/authorized&response_type=code&scope=openid+read+write&code_challenge=TEST&code_challenge_method=S256"
```

## Activer les logs détaillés

Ajoutez ceci à votre `application.yml` pour voir les erreurs détaillées :

```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
    org.springframework.jdbc: DEBUG
```

## Configuration complète de référence

Voici une configuration complète de Spring Authorization Server :

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/.well-known/jwks.json").permitAll()
                .requestMatchers("/login").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(server -> server
                .jwt(Customizer.withDefaults())
            );

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:9000")
                .build();
    }
}
```

## Prochaines étapes

1. **Consultez les logs du serveur** avec le niveau DEBUG activé
2. **Identifiez l'erreur exacte** dans les logs
3. **Appliquez la solution correspondante** ci-dessus
4. **Redémarrez le serveur** après les modifications
5. **Testez à nouveau** la connexion depuis votre application Vue

## Support

Si après avoir essayé toutes ces solutions vous avez toujours une erreur 500 :

1. Vérifiez que vous utilisez une version compatible de Spring Authorization Server
2. Vérifiez que toutes les dépendances sont à jour
3. Consultez la documentation officielle : https://docs.spring.io/spring-authorization-server/reference/
