package com.example.demo.accountmanager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountManagerRepository extends JpaRepository<AccountManager, Long> {


    @Query(value = "SELECT * FROM account_manager WHERE account_id = :account_id", nativeQuery = true)
    public AccountManager findByAccountID(@Param("account_id") Long accountID);


    @Query(value = "SELECT am.id, rootdir, totalmem, usedmem, account_id FROM account_manager am JOIN account a ON am.account_id = a.id WHERE username = :username", nativeQuery = true)
    public AccountManager findByUsername(@Param("username") String username);
}
