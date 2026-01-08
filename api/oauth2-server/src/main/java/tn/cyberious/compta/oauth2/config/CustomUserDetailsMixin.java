package tn.cyberious.compta.oauth2.config;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Jackson Mixin for CustomUserDetails to enable proper serialization/deserialization.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CustomUserDetailsMixin {

  @JsonCreator
  CustomUserDetailsMixin(
      @JsonProperty("id") UUID id,
      @JsonProperty("username") String username,
      @JsonProperty("password") String password,
      @JsonProperty("email") String email,
      @JsonProperty("firstName") String firstName,
      @JsonProperty("lastName") String lastName,
      @JsonProperty("enabled") boolean enabled,
      @JsonProperty("accountNonExpired") boolean accountNonExpired,
      @JsonProperty("accountNonLocked") boolean accountNonLocked,
      @JsonProperty("credentialsNonExpired") boolean credentialsNonExpired,
      @JsonProperty("roleNames") List<String> roleNames) {}
}
