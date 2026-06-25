package io.mite88.mite88shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Mite88shopApplication {

    public static void main(String[] args) {
        SpringApplication.run(Mite88shopApplication.class, args);
    }

}
