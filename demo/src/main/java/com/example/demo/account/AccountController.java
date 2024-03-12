package com.example.demo.account;

import com.example.demo.account.dto.AccountDTO;
import com.example.demo.account.dto.AuthDTO;
import com.example.demo.jwtUtil.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;
    private final JWTUtil jwtUtil;

    @Autowired
    public AccountController(AccountService accountService, JWTUtil jwtUtil){
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping(path = "/signup")
    public ResponseEntity<?> signup(@RequestBody AccountDTO accountDTO) {
        try {
            Account account = new Account();
            account.setUsername(accountDTO.getUsername());
            account.setEmail(accountDTO.getEmail());
            account.setPassword(accountDTO.getPassword());

            Account savedAccount = accountService.addNewAccount(account);

            return new ResponseEntity<>(savedAccount, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/login")
    public ResponseEntity<?> login(@RequestBody AccountDTO accountDTO) {
        try {
            Account account = new Account();
            account.setUsername(accountDTO.getUsername());
            account.setPassword(accountDTO.getPassword());

            AuthDTO authDTO = accountService.login(account);

            return ResponseEntity.ok(authDTO);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping(path = "/delete")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String jwtToken){
        if(jwtUtil.isTokenValid(jwtToken)){
            String username = jwtUtil.extractSubject(jwtToken);
            Account account = accountService.getAccountByUsername(username).get();
            accountService.delete(account);

            return ResponseEntity.ok(null);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @GetMapping("/getAccounts")
    public ResponseEntity<?> getAccounts(@RequestHeader("Authorization") String jwtToken){
        if(jwtUtil.isTokenValid(jwtToken)){
            String username = jwtUtil.extractSubject(jwtToken);
            if(username.equals("admin")){
                return ResponseEntity.ok(accountService.getAccountsConfigs());
            }
            else return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }
        else return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
    }


}
