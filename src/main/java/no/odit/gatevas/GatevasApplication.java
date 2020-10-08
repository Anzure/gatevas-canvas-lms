package no.odit.gatevas;

import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@Configurable
@EnableJpaRepositories
@ComponentScan("no.odit.gatevas")
public class GatevasApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(GatevasApplication.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(GatevasApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Hello world!");

		Scanner scanner = new Scanner(System.in);

		System.out.print("Enter your name: ");
		String name = scanner.next();

		log.info("Hello " + name);

		scanner.close();
	}

}