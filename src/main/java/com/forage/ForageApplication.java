package com.forage;

import com.forage.entity.*;
import com.forage.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ForageApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForageApplication.class, args);
    }
    
}