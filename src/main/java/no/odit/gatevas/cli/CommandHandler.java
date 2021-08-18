package no.odit.gatevas.cli;

import org.springframework.stereotype.Component;

@Component
public interface CommandHandler {

    void handleCommand(Command command);

}