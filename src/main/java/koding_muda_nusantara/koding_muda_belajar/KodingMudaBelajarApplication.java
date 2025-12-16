package koding_muda_nusantara.koding_muda_belajar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KodingMudaBelajarApplication {

	public static void main(String[] args) {
		SpringApplication.run(KodingMudaBelajarApplication.class, args);
	}

}
