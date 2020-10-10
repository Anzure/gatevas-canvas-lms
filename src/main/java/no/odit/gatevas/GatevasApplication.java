package no.odit.gatevas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Configurable
@EnableAsync
@EnableJpaRepositories
@ComponentScan("no.odit.gatevas")
public class GatevasApplication {

	private static final Logger log = LoggerFactory.getLogger(GatevasApplication.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(GatevasApplication.class, args);
	}
}