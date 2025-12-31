package tn.cyberious.compta.oauth2.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntrospectionResponse {

  private Boolean active;

  private String clientId;

  private String tokenType;

  private Instant exp;

  private Instant iat;

  private String sub;

  private String aud;

  private String iss;

  private List<String> scope;

  private String username;

  private String email;

  private List<String> roles;
}
