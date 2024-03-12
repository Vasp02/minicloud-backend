package com.example.demo.encryptionkey;

import com.example.demo.accountmanager.AccountManager;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EncryptionKeyRepository extends JpaRepository<EncryptionKey, Long> {

    @Query(value = "SELECT id, filepath, key from encryption_key WHERE filepath = :path",nativeQuery = true)
    public EncryptionKey getKeyForFile(@Param("path") String path);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM encryption_key WHERE file_id = :fileid", nativeQuery = true)
    public int deleteByFileID(@Param("fileid") String fileid);

    @Query(value = "SELECT * FROM encryption_key WHERE file_id = :fileid", nativeQuery = true)
    public EncryptionKey findByFileID(@Param("fileid") String fileid);


}
