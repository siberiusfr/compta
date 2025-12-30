package tn.cyberious.compta.authz.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// import tn.compta.commons.security.annotation.AuthenticatedUser;
// import tn.compta.commons.security.model.AuthenticatedUserDetails;

@RestController
@RequestMapping("/api/authz")
public class TtController {

  @GetMapping("/tt")
  public String tt(
      // @AuthenticatedUser AuthenticatedUserDetails user
      ) {
    // return "You are authenticated: %s %s %s %s".formatted(user.getUsername(), user.getEmail(),
    // user.getUserId(), user.getRoles());
    return "You are authenticated";
  }
}
