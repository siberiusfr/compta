package tn.cyberious.compta.einvoicing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "tn.cyberious.compta.authz.client")
@SpringBootApplication
public class EinvoicingTnServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(EinvoicingTnServiceApplication.class, args);
  }
}
