package org.ftc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@EnableCaching
@SpringBootApplication
public class FtcServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(FtcServerApplication.class, args);
	}
}
