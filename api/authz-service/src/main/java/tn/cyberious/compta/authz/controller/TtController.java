package tn.cyberious.compta.authz.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/authz")
public class TtController {

  @GetMapping("/tt")
  public String tt(@RequestHeader Map<String, String> headers) {
    headers.forEach((key, value) -> {
      System.out.println(key + ": " + value);
    });
    return "String";
  }
}
