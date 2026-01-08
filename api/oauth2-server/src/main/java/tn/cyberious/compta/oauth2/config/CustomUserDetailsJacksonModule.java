package tn.cyberious.compta.oauth2.config;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.security.jackson2.SecurityJackson2Modules;

import tn.cyberious.compta.oauth2.security.CustomUserDetails;

/**
 * Jackson module to register CustomUserDetails for serialization/deserialization
 * in Spring Authorization Server's JdbcOAuth2AuthorizationService.
 */
public class CustomUserDetailsJacksonModule extends SimpleModule {

  public CustomUserDetailsJacksonModule() {
    super(
        CustomUserDetailsJacksonModule.class.getName(),
        new Version(1, 0, 0, null, null, null));
  }

  @Override
  public void setupModule(SetupContext context) {
    // Register CustomUserDetails in the allowlist
    SecurityJackson2Modules.enableDefaultTyping(
        (com.fasterxml.jackson.databind.ObjectMapper) context.getOwner());
    context.setMixInAnnotations(CustomUserDetails.class, CustomUserDetailsMixin.class);
  }
}
