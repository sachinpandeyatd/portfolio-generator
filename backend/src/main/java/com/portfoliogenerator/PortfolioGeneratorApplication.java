package com.portfoliogenerator;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.tika.Tika;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PortfolioGeneratorApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().filename(".env").load();
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
        SpringApplication.run(PortfolioGeneratorApplication.class, args);
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }
}