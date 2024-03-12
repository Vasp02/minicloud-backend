package com.example.demo.accountmanager;

import com.example.demo.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/manage")
public class AccountManagerController {

    private final AccountManagerService accountManagerService;

    @Autowired
    public AccountManagerController(AccountManagerService accountManagerService){
        this.accountManagerService = accountManagerService;
    }


    @PostMapping("/updateTotalMemory")
    public ResponseEntity<?> updateTotalMemory(@RequestParam("username") String username,
                                               @RequestParam("newTotalMemory") double newTotalMemory,
                                               @RequestHeader("Authorization") String jwtToken){
        try {
            AccountManager updatedAccountManager = accountManagerService.updateTotalMemory(username,newTotalMemory);
            return ResponseEntity.ok(updatedAccountManager);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating total memory.");
        }
    }



}
