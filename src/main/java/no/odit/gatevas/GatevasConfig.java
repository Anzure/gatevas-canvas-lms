package no.odit.gatevas;

import java.util.List;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.command.CourseCommand;
import no.odit.gatevas.command.ExitCommand;
import no.odit.gatevas.command.StudentCommand;

@Configuration
public class GatevasConfig {

	@Autowired
	private CourseCommand courseCommand;

	@Autowired
	private ExitCommand exitCommand;

	@Autowired
	private StudentCommand studentCommand;

	@Bean
	public List<CommandHandler> getCommands() {
		return List.of(courseCommand, exitCommand, studentCommand);
	}

	@Bean
	public Scanner getCommandScanner() {
		return new Scanner(System.in);
	}
}