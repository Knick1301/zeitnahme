package de.student.zeitnahme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZeitnahmeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZeitnahmeApplication.class, args);
    }
}
