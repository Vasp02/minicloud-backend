package com.example.demo.account.dto;

import com.example.demo.account.Account;
import com.example.demo.accountmanager.AccountManager;

public class AccountConfigDTO {
    private Account account;
    private AccountManager accountManager;

    public AccountConfigDTO(){

    }

    public AccountConfigDTO(Account account, AccountManager accountManager) {
        this.account = account;
        this.accountManager = accountManager;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public String toString() {
        return "AccountConfigDTO{" +
                "account=" + account +
                ", accountManager=" + accountManager +
                '}';
    }
}
