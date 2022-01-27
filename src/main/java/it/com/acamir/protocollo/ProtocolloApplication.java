package it.com.acamir.protocollo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProtocolloApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProtocolloApplication.class, args);
	}

}
 