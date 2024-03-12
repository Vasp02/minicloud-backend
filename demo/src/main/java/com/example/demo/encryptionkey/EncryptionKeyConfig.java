package com.example.demo.encryptionkey;

import com.example.demo.cloudfile.CloudFile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionKeyConfig {
    CommandLineRunner commandLineRunnerKey (EncryptionKeyRepository repository){
        return args -> {
            new EncryptionKey();
        };
    }
}
