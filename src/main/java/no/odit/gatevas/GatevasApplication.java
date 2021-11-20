package no.odit.gatevas;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Configurable
@EnableAsync
@EnableJpaRepositories
public class GatevasApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatevasApplication.class, args);
    }

}