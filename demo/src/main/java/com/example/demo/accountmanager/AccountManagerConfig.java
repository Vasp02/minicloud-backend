package com.example.demo.accountmanager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountManagerConfig {
    @Bean
    CommandLineRunner commandLineRunnerAccountManager(AccountManagerRepository repository){
        return args -> {
            new AccountManager();
        };
    }
}
