package no.odit.gatevas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import no.odit.gatevas.misc.GoogleSheetIntegration;

@SpringBootApplication
@Configurable
@EnableJpaRepositories
@ComponentScan("no.odit.gatevas")
public class GatevasApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(GatevasApplication.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(GatevasApplication.class, args);
	}

	@Autowired
	private GoogleSheetIntegration sheetIntegration;
	
	@Override
	public void run(String... args) throws Exception {
		// TODO
		log.error("DEBUG 1");
		sheetIntegration.processSheet("Vedlikehold-BP5-H20", "10efYpyYXj5oX3UO4eEEux4zmjY1udM-vEe07ESL_cPE");
		log.error("DEBUG 2");
	}

}