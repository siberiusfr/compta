package tn.cyberious.compta.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "tn.cyberious.compta.authz.client")
@SpringBootApplication
public class HrServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(HrServiceApplication.class, args);
  }
}
