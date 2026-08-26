package org.themarioga.telegram.cah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = {"org.themarioga"})
@EntityScan(basePackages = {"org.themarioga"})
public class CAHTelegramApplication {

    public static void main(String[] args) {
        SpringApplication.run(CAHTelegramApplication.class, args);
    }

}
