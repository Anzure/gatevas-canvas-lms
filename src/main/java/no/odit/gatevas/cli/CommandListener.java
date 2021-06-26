package no.odit.gatevas.cli;

import java.util.List;
import java.util.Scanner;
import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import io.netty.util.concurrent.Future;

@Component
@Slf4j
public class CommandListener {

    @Autowired
    private List<CommandHandler> commands;

    @Autowired
    private Scanner scanner;

    @PostConstruct
    @Async
    public Future<Void> start() {

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

        } while (scanner != null && scanner.hasNextLine());

        log.info("Exiting console application...");
        return null;
    }

}