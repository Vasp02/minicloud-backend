package com.example.demo.account;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountConfig {
    @Bean
    CommandLineRunner commandLineRunnerAccount(AccountRepository repository){
        return args -> {
            new Account();
        };
    }
}
