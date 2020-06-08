package dataproducerjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DataProducerJavaApplication {

  public static void main(String[] args) {
    SpringApplication.run(DataProducerJavaApplication.class, args);
  }
}
