package com.example.demo.cloudfile;


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudFileConfig {
    @Bean
    CommandLineRunner commandLineRunnerCloudFile(CloudFileRepository repository){
        return args -> {
            new CloudFile();
        };
    }
}
