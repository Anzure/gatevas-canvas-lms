package no.odit.gatevas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import no.odit.gatevas.command.CourseCommand;
import no.odit.gatevas.command.ExitCommand;
import no.odit.gatevas.command.StudentCommand;
import no.odit.gatevas.type.CommandListener;

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

		log.info("Starting console application...");

		CommandListener cmdListener = new CommandListener();
		cmdListener.registerCommand("exit", new ExitCommand());
		cmdListener.registerCommand("course", new CourseCommand());
		cmdListener.registerCommand("student", new StudentCommand());
		cmdListener.start();

		log.info("Exiting console application...");
	}

}