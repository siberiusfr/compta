package tn.cyberious.compta.oauth2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.cyberious.compta.oauth2.dto.UserInfoResponse;
import tn.cyberious.compta.oauth2.generated.tables.Roles;
import tn.cyberious.compta.oauth2.generated.tables.UserRoles;
import tn.cyberious.compta.oauth2.generated.tables.Users;

@Tag(name = "UserInfo", description = "OIDC UserInfo endpoint (RFC 7662)")
@RestController
@RequestMapping
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserInfoController {

  private final DSLContext dsl;

  @Operation(summary = "Get user info", description = "Returns claims about the authenticated user")
  @GetMapping("/userinfo")
  public ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal Jwt jwt) {

    String username = jwt.getSubject();

    var userRecord =
        dsl.selectFrom(Users.USERS).where(Users.USERS.USERNAME.eq(username)).fetchOne();

    if (userRecord == null) {
      return ResponseEntity.notFound().build();
    }

    List<String> roles =
        dsl.select(Roles.ROLES.NAME)
            .from(UserRoles.USER_ROLES)
            .join(Roles.ROLES)
            .on(UserRoles.USER_ROLES.ROLE_ID.eq(Roles.ROLES.ID))
            .where(UserRoles.USER_ROLES.USER_ID.eq(userRecord.getId()))
            .fetch(Roles.ROLES.NAME);

    UserInfoResponse response =
        UserInfoResponse.builder()
            .sub(userRecord.getId().toString())
            .name(
                userRecord.getFirstName() != null && userRecord.getLastName() != null
                    ? userRecord.getFirstName() + " " + userRecord.getLastName()
                    : userRecord.getUsername())
            .givenName(userRecord.getFirstName())
            .familyName(userRecord.getLastName())
            .email(userRecord.getEmail())
            .emailVerified(true)
            .roles(roles)
            .build();

    return ResponseEntity.ok(response);
  }
}
