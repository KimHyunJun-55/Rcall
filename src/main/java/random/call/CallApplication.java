package random.call;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;

@SpringBootApplication
@EnableJpaAuditing

public class CallApplication {

	public static void main(String[] args) {
		SpringApplication.run(CallApplication.class, args);
	}

}
