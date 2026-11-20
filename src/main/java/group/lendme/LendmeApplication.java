package group.lendme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LendmeApplication {

	public static void main(String[] args) {
		SpringApplication.run(LendmeApplication.class, args);
	}

}
