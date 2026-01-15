package tn.cyberious.compta.referentiel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "tn.cyberious.compta.authz.client")
@SpringBootApplication
public class ReferentielServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReferentielServiceApplication.class, args);
  }
}
