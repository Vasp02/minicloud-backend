package com.example.demo.accountmanager;

import com.example.demo.account.Account;
import com.example.demo.account.AccountService;
import com.example.demo.jwtUtil.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountManagerService {

    private final AccountManagerRepository repository;
    private final JWTUtil jwtUtil;

    @Autowired
    public AccountManagerService(AccountManagerRepository repository, JWTUtil jwtUtil){
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }

    public AccountManager createNewManager(Account account){
        AccountManager accountManager = new AccountManager(account);
        return repository.save(accountManager);
    }

    public AccountManager getManagerOfAccount(Account account){
        Long accountID = account.getId();
        AccountManager accountManager = repository.findByAccountID(accountID);
        return accountManager;
    }

    public AccountManager updateUsedMemoryOfAccount(Account account, double newUsedMemory) {
        AccountManager accountManager = getManagerOfAccount(account);
        if (accountManager != null) {
            accountManager.setUsedMemory(newUsedMemory);
            return repository.save(accountManager);
        } else {
            System.err.println("No AccountManager found for the provided account.");
            return null;
        }
    }

    public boolean canPerformOperation(Account account, double operationSize) {
        AccountManager accountManager = getManagerOfAccount(account);
        if (accountManager == null) {
            System.err.println("No AccountManager found for the provided account.");
            return false;
        }
        double newUsedMemory = accountManager.getUsedMemory() + operationSize;
        return newUsedMemory <= accountManager.getTotalMemory();
    }

    public AccountManager getAccountManagerFromToken(String jwtToken){
        String username = jwtUtil.extractSubject(jwtToken);
        AccountManager accountManager = repository.findByUsername(username);
        return accountManager;
    }

    public void deleteAccountManager(AccountManager accountManager){
        repository.delete(accountManager);
    }

    public AccountManager updateTotalMemory(String username, double newTotalMemory){
        AccountManager accountManager = repository.findByUsername(username);
        if(newTotalMemory < accountManager.getUsedMemory()){
            throw new IllegalArgumentException("Total memory cannot be less than used memory.");
        }
        accountManager.setTotalMemory(newTotalMemory);
        return repository.save(accountManager);
    }

}
