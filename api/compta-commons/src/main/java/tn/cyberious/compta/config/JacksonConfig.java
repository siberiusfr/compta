package tn.cyberious.compta.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration Jackson pour la sérialisation/désérialisation JSON.
 * Configure le support pour :
 * - java.time (LocalDate, LocalDateTime, etc.)
 * - Optional, OptionalInt, etc.
 * - Noms de paramètres des constructeurs
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Module pour java.time (LocalDate, LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Module pour Optional, OptionalInt, etc.
        mapper.registerModule(new Jdk8Module());

        // Module pour les noms de paramètres
        mapper.registerModule(new ParameterNamesModule());

        // Désactiver l'écriture des dates en timestamp
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Formater le JSON de manière lisible (enlever pour la prod si besoin)
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper;
    }
}
