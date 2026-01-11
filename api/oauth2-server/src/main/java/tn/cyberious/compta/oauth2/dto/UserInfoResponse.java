package tn.cyberious.compta.oauth2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoResponse {

  private String sub;

  private String name;

  private String givenName;

  private String familyName;

  private String email;

  private Boolean emailVerified;

  private List<String> roles;

  private String tenantId;
}
