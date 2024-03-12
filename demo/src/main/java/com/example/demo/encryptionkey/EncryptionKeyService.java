package com.example.demo.encryptionkey;

import com.example.demo.cloudfile.CloudFile;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionKeyService {

    private EncryptionKeyRepository repository;

    @Autowired
    public EncryptionKeyService(EncryptionKeyRepository repository){
        this.repository = repository;
    }


    public void saveKey(String fileID, SecretKey secretKey) {
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        EncryptionKey encryptionKey = new EncryptionKey(fileID, encodedKey);
        repository.save(encryptionKey);
    }

    public SecretKey getKeyForFile(String fileID) {
        EncryptionKey encryptionKeyEntity = repository.findByFileID(fileID);
        if (encryptionKeyEntity == null) {
            throw new RuntimeException("No encryption key found for file ID: " + fileID);
        }

        byte[] decodedKey = Base64.getDecoder().decode(encryptionKeyEntity.getKey());
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    @Transactional
    public boolean deleteKeyByFileID(String fileID) {
        try {
            int affectedRows = repository.deleteByFileID(fileID);
            return affectedRows > 0;
        } catch (Exception e) {
            return false;
        }
    }

}
