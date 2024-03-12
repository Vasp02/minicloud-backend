package com.example.demo.account;

import com.example.demo.account.dto.AccountConfigDTO;
import com.example.demo.account.dto.AuthDTO;
import com.example.demo.accountmanager.AccountManager;
import com.example.demo.accountmanager.AccountManagerService;
import com.example.demo.jwtUtil.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Stream;

@Service
public class AccountService{
    @Value("${file.upload-dir}")
    private String uploadDir;

    private final AccountRepository repository;
    private final AccountManagerService accountManagerService;
    private final JWTUtil jwtUtil;

    @Autowired
    public AccountService(AccountRepository repository, AccountManagerService accountManagerService, JWTUtil jwtUtil){
        this.repository = repository;
        this.accountManagerService = accountManagerService;
        this.jwtUtil = jwtUtil;
    }


    public Account addNewAccount(Account account) {
        if (!isAccountFormValid(account)) {
            throw new IllegalArgumentException("Account form data is invalid.");
        }

        if (repository.findByUsername(account.getUsername()).isPresent()) {
            throw new IllegalStateException("[ERROR]: Account username already exists!");
        }

        String password = account.getPassword();
        byte[] salt = generateSalt();
        String saltString = Base64.getEncoder().encodeToString(salt);
        String hashedPassword = getHashedPassword(password, salt);
        account.setPassword(hashedPassword);
        account.setPasswordSalt(saltString);
        Account savedAccount = repository.save(account);

        accountManagerService.createNewManager(savedAccount);

        return savedAccount;
    }

    public AuthDTO login(Account account){

        Account existingAccount = repository.findByUsername(account.getUsername())
                .orElse(null);

        if (existingAccount != null) {
            if (isPasswordMatching(account.getPassword(),existingAccount.getPasswordSalt(),existingAccount.getPassword())) {
                String token = jwtUtil.createToken(account);
                AuthDTO authDTO = new AuthDTO();
                authDTO.setToken(token);
                authDTO.setUsername(account.getUsername());
                return authDTO;
            } else {
                throw new IllegalStateException("Invalid credentials");
            }
        } else {
            throw new IllegalStateException("Account does not exist");
        }
    }

    public void delete(Account account) {
        AccountManager manager = accountManagerService.getManagerOfAccount(account);
        if (manager != null) {

            String userRootDir = uploadDir + manager.getRootdir();
            Path directoryPath = Paths.get(userRootDir);
            if (Files.exists(directoryPath)) {
                if (Files.isDirectory(directoryPath)) {
                    try (Stream<Path> paths = Files.walk(directoryPath)) {
                        paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(directoryPath + " is not a directory");
                }
            } else {
                System.out.println(directoryPath + " does not exist");
            }
            accountManagerService.deleteAccountManager(manager);
            repository.delete(account);
        }
    }

    public Optional<Account> getAccountByUsername(String username){
        return repository.findByUsername(username);
    }


    public List<AccountConfigDTO> getAccountsConfigs() {
        List<Account> accounts = repository.findAll();
        List<AccountConfigDTO> accountConfigDTOS = new ArrayList<>();

        for (Account account : accounts) {
            AccountManager accountManager = accountManagerService.getManagerOfAccount(account);

            account.setPassword(null);
            account.setPasswordSalt(null);

            AccountConfigDTO dto = new AccountConfigDTO(account, accountManager);

            accountConfigDTOS.add(dto);
        }
        return accountConfigDTOS;
    }


    private byte[] generateSalt(){
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private String getHashedPassword(String password, byte[] salt){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        }
        catch(Exception e){
            System.out.println();
            e.printStackTrace();
            return null;
        }
    }

    private boolean isPasswordMatching(String enteredPassword, String storedSalt, String storedHashedPassword) {
        try {
            byte[] salt = Base64.getDecoder().decode(storedSalt);
            String hashedPassword = getHashedPassword(enteredPassword, salt);
            return hashedPassword.equals(storedHashedPassword);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isAccountFormValid(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }
        if (isUsernameValid(account.getUsername()) && isPasswordValid(account.getPassword()) && isEmailValid(account.getEmail())){
            return true;
        }
        return false;
    }


    private boolean isUsernameValid(String username){
        if (username == null || username.trim().isEmpty()) {
            System.out.println("[Validation Error] : Username cannot be empty.");
            return false;
        } else if (username.length() < 5) {
            System.out.println("[Validation Error] : Username must be at least 5 characters long.");
            return false;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            System.out.println("[Validation Error] : Username can only contain letters, numbers, and underscores.");
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password){
        if (password == null || password.trim().isEmpty()) {
            System.out.println("[Validation Error] : Password cannot be empty.");
            return false;
        } else if (password.length() < 8) {
            System.out.println("[Validation Error] : Password must be at least 8 characters long.");
            return false;
        } else if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$")) {
            System.out.println("[Validation Error] : Password must contain at least one uppercase letter, one lowercase letter and one digit");
            return false;
        }
        return true;
    }

    private boolean isEmailValid(String email){
        if (email == null || email.trim().isEmpty()) {
            System.out.println("[Validation Error] : Email cannot be empty.");
            return false;
        } else if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            System.out.println("[Validation Error] : Invalid email format.");
            return false;
        }
        return true;
    }

    public Account addNewAdminAccount(Account account) {

        if (repository.findByUsername(account.getUsername()).isPresent()) {
            throw new IllegalStateException("[ERROR]: Admin already exists!");
        }
        String password = account.getPassword();
        byte[] salt = generateSalt();
        String saltString = Base64.getEncoder().encodeToString(salt);
        String hashedPassword = getHashedPassword(password, salt);
        account.setPassword(hashedPassword);
        account.setPasswordSalt(saltString);
        Account savedAccount = repository.save(account);

        accountManagerService.createNewManager(savedAccount);

        return savedAccount;
    }




}
