package com.example.demo.accountmanager;

import com.example.demo.account.Account;
import jakarta.persistence.*;

@Entity
public class AccountManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;
    @Column(name = "rootdir")
    private String rootdir;
    @Column(name = "usedmem")
    private double usedMemory;
    @Column(name = "totalmem")
    private double totalMemory;

    public AccountManager(){

    }

    public AccountManager(String rootdir, double usedMemory, double totalMemory) {
        this.rootdir = rootdir;
        this.usedMemory = usedMemory;
        this.totalMemory = totalMemory;
    }

    public AccountManager(Account account){
        this.rootdir = "./".concat(account.getUsername());
        this.usedMemory = 0;
        this.totalMemory = 4096;
        this.account = account;
    }

    public Long getId() {
        return id;
    }


    public String getRootdir() {
        return rootdir;
    }

    public void setRootdir(String rootdir) {
        this.rootdir = rootdir;
    }

    public double getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(double usedMemory) {
        this.usedMemory = usedMemory;
    }

    public double getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(double totalMemory) {
        this.totalMemory = totalMemory;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "AccountManager{" +
                "id=" + id +
                ", rootdir='" + rootdir + '\'' +
                ", usedMemory=" + usedMemory +
                ", totalMemory=" + totalMemory +
                '}';
    }
}
