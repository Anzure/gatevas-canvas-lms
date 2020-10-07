package no.odit.gatevas;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@Configurable
public class GatevasApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(GatevasApplication.class, args);
	}

	@Bean
	public WebClient webClient() {
		return WebClient.create();
	}

}