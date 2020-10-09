package no.odit.gatevas.cli;

import java.util.List;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.odit.gatevas.GatevasApplication;

@Service
public class CommandListener {

	private static final Logger log = LoggerFactory.getLogger(GatevasApplication.class);

	@Autowired
	private List<CommandHandler> commands;

	private Scanner scanner;

	@PostConstruct
	private void start() {

		log.info("Starting console application...");
		this.scanner = new Scanner(System.in);

		do {
			System.out.print("=> ");
			Command cmd = new Command(scanner.nextLine());
			String key = cmd.getCmd().toLowerCase();
			log.debug("Received command: " + cmd.getCmd());

			commands.stream().filter(handler -> handler.getClass().getSimpleName().replace("Command", "").equalsIgnoreCase(key))
			.findAny().ifPresentOrElse(handler -> {

				log.debug("Handling command: " + cmd.getCmd());
				handler.handleCommand(cmd);
				log.debug("Handled command: " + cmd.getCmd());

			}, () -> {
				log.error("Unknown command '" + key + "'.");
			});

		} while(scanner != null && scanner.hasNextLine());

		log.info("Exiting console application...");
	}

	public void stop() {
		scanner.close();
		scanner = null;
	}

	public Scanner getScanner() {
		return scanner;
	}
}