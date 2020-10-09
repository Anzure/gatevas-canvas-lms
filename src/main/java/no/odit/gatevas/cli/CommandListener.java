package no.odit.gatevas.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.odit.gatevas.GatevasApplication;

public class CommandListener {

	private static final Logger log = LoggerFactory.getLogger(GatevasApplication.class);

	private Map<String, CommandHandler> commands;

	protected Scanner scanner;

	public CommandListener() {
		this.commands = new HashMap<String, CommandHandler>();
		this.scanner = new Scanner(System.in);
	}

	public void registerCommand(String cmd, CommandHandler handler) {
		commands.put(cmd, handler);
	}

	public void start() {
		do {
			System.out.print("=> ");
			Command cmd = new Command(scanner.nextLine());
			String key = cmd.getCmd().toLowerCase();
			log.debug("Received command: " + cmd.getCmd());
			if (commands.containsKey(key)) {
				log.debug("Handling command: " + cmd.getCmd());
				commands.get(key).handleCommand(cmd);
				log.debug("Handled command: " + cmd.getCmd());
			}
		} while(scanner != null && scanner.hasNextLine());
	}

	public void stop() {
		scanner.close();
		scanner = null;
	}
}