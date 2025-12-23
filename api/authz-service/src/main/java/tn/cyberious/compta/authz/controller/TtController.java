package tn.cyberious.compta.authz.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authz")
public class TtController {

  @GetMapping("/tt")
  public String tt() {
    return "String";
  }
}
