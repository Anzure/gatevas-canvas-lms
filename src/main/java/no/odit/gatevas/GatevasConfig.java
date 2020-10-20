package no.odit.gatevas;

import java.util.List;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.command.CourseCommand;
import no.odit.gatevas.command.ExitCommand;

@Configuration
@EnableAsync
public class GatevasConfig implements AsyncConfigurer {

	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
		pool.setCorePoolSize(5);
		pool.setMaxPoolSize(10);
		pool.setWaitForTasksToCompleteOnShutdown(false);
		return pool;
	}

	@Autowired
	private CourseCommand courseCommand;

	@Autowired
	private ExitCommand exitCommand;

	@Bean
	public List<CommandHandler> getCommands() {
		return List.of(courseCommand, exitCommand);
	}

	@Bean
	public Scanner getCommandScanner() {
		return new Scanner(System.in);
	}
}