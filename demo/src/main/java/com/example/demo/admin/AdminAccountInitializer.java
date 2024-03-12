package com.example.demo.admin;

import com.example.demo.account.Account;
import com.example.demo.account.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class AdminAccountInitializer {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initAdminAccount(AccountService accountService) {
        return args -> {

            boolean adminExists = accountService.getAccountByUsername(adminUsername).isPresent();

            if (!adminExists) {
                Account adminAccount = new Account();
                adminAccount.setUsername(adminUsername);
                adminAccount.setEmail(adminEmail);
                adminAccount.setPassword(adminPassword);

                accountService.addNewAdminAccount(adminAccount);
                System.out.println("Admin account created");
            }
        };
    }
}
