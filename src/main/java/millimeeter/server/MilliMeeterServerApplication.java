package millimeeter.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
      "millimeeter.server.config",
      "millimeeter.server.controller",
      "millimeeter.server.repository",
      "millimeeter.server.service",
      "millimeeter.server.exception"
    })
public class MilliMeeterServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(MilliMeeterServerApplication.class, args);
  }
}
