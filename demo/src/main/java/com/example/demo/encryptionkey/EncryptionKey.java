package com.example.demo.encryptionkey;

import jakarta.persistence.*;

@Entity
public class EncryptionKey {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "file_id")
    private String fileID;

    @Column(name = "key")
    private String key;

    public EncryptionKey() {
    }

    public EncryptionKey(String fileID, String key) {
        this.fileID = fileID;
        this.key = key;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
