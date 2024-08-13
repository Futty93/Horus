package jp.ac.tohoku.qse.takahashi.AtcSimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AtcSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtcSimulatorApplication.class, args);
	}

}
