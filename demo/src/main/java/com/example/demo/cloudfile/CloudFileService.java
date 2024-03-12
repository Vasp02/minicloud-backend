package com.example.demo.cloudfile;

import com.example.demo.account.Account;
import com.example.demo.account.AccountService;
import com.example.demo.accountmanager.AccountManager;
import com.example.demo.accountmanager.AccountManagerService;
import com.example.demo.cloudfile.dto.FileDTO;
import com.example.demo.encryptionUtil.EncryptionUtil;
import com.example.demo.encryptionkey.EncryptionKeyService;
import com.example.demo.jwtUtil.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CloudFileService {

    private final JWTUtil jwtUtil;
    private final CloudFileRepository cloudFileRepository;
    private final AccountManagerService accountManagerService;
    private final AccountService accountService;
    private final EncryptionKeyService encryptionKeyService;


    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public CloudFileService(CloudFileRepository cloudFileRepository,
                            JWTUtil jwtUtil,
                            AccountManagerService accountManagerService,
                            AccountService accountService,
                            EncryptionKeyService encryptionKeyService){
        this.cloudFileRepository = cloudFileRepository;
        this.jwtUtil = jwtUtil;
        this.accountManagerService = accountManagerService;
        this.accountService = accountService;
        this.encryptionKeyService = encryptionKeyService;
    }

    public String uploadFiles(MultipartFile[] files, String userPath, String token) {
        try {
            if (!jwtUtil.isTokenValid(token)) {
                throw new SecurityException("Invalid or expired JWT token.");
            }
            String username = jwtUtil.extractSubject(token);
            Account userAccount = accountService.getAccountByUsername(username).orElse(null);

            if (userAccount == null) {
                throw new IllegalStateException("User account does not exist.");
            }
            AccountManager accountManager = accountManagerService.getManagerOfAccount(userAccount);

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                double fileSizeInMB = bytesToMegabytes(file.getSize());
                if (!accountManagerService.canPerformOperation(userAccount, fileSizeInMB)) {
                    System.out.println("operation too large : " + fileSizeInMB);
                    return "Upload failed: Not enough space available for file " + file.getOriginalFilename();
                }

                SecretKey secretKey = EncryptionUtil.generateKey();
                byte[] encryptedData = EncryptionUtil.encrypt(file.getBytes(), secretKey);

                String fileID = UUID.randomUUID().toString();
                String modifiedFileName = fileID + "_" + file.getOriginalFilename();

                Path filePath = Paths.get(uploadDir, accountManager.getRootdir(), userPath, modifiedFileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, encryptedData);

                encryptionKeyService.saveKey(fileID, secretKey);
            }
            String pathDebug = uploadDir+accountManager.getRootdir();
            double sizeofdir = bytesToMegabytes(getFileSizeInBytes(Paths.get(pathDebug)));
            accountManagerService.updateUsedMemoryOfAccount(userAccount, sizeofdir);
            return "Files uploaded successfully.";
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("File upload failed due to an IO error.", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("File upload failed due to an unexpected error.", e);
        }
    }



    public List<FileDTO> getFilesAtPath(String directoryPath, String jwtToken) {
        directoryPath = sanitizePath(directoryPath);
        String userPath = getUserPathFromJWT(jwtToken);
        String fullPathName =uploadDir+ userPath + directoryPath;
        File directory = new File(fullPathName);

        File[] listOfFiles = directory.listFiles();
        List<FileDTO> fileDTOS = new ArrayList<>();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                try {
                    if (file.isFile()) {
                        String fileID = file.getName().split("_")[0];
                        SecretKey key = encryptionKeyService.getKeyForFile(fileID);

                        byte[] encryptedData = Files.readAllBytes(file.toPath());
                        byte[] decryptedData = EncryptionUtil.decrypt(encryptedData, key);

                        String base64Image = java.util.Base64.getEncoder().encodeToString(decryptedData);
                        fileDTOS.add(new FileDTO(file.getName(), FileDTO.FileType.FILE, base64Image, userPath + directoryPath));
                    } else {
                        fileDTOS.add(new FileDTO(file.getName(), FileDTO.FileType.DIRECTORY, userPath + directoryPath));
                    }
                } catch (IOException e) {
                    System.err.println("Error encoding file: " + file.getName());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("The specified path does not exist or is not a directory");
        }
        return fileDTOS;
    }

    public boolean deleteFile(String filePath, String jwtToken) {
        filePath = sanitizePath(filePath);
        if (!jwtUtil.isTokenValid(jwtToken)) {
            return false;
        }
        Path pathToFile = Paths.get(uploadDir, filePath);
        try {
            if (!Files.isDirectory(pathToFile)) {
                String fileName = pathToFile.getFileName().toString();
                String fileID = fileName.contains("_") ? fileName.split("_")[0] : "";

                boolean keyDeleted = encryptionKeyService.deleteKeyByFileID(fileID);
                if (keyDeleted) {
                    System.out.println("Encryption key deleted successfully.");
                } else {
                    System.out.println("No encryption key found or failed to delete.");
                }

                Files.delete(pathToFile);
                String username = jwtUtil.extractSubject(jwtToken);
                Account userAccount = accountService.getAccountByUsername(username).orElse(null);
                if (userAccount == null) {
                    throw new IllegalStateException("User account does not exist.");
                }

                AccountManager accountManager = accountManagerService.getManagerOfAccount(userAccount);
                String pathToScan = uploadDir + accountManager.getRootdir();
                double sizeofdir = bytesToMegabytes(getFileSizeInBytes(Paths.get(pathToScan)));
                accountManagerService.updateUsedMemoryOfAccount(userAccount, sizeofdir);
                return true;
            } else {
                try {
                    Files.delete(pathToFile);
                    System.out.println("Directory deleted successfully.");
                    return true;
                } catch (DirectoryNotEmptyException e) {
                    System.err.println("Directory is not empty.");
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to delete the file or directory.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean createFolder(String path, String jwtToken){
        path = sanitizePath(path);
        String userDir = getUserPathFromJWT(jwtToken);

        String fullpath = uploadDir + userDir + path;
        try {
            Path folderPath = Paths.get(fullpath);

            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Resource getDownloadResource(String filePath, String jwtToken) {
        String userPath = getUserPathFromJWT(jwtToken);
        String targetFilePath = uploadDir + userPath + filePath;
        Path path = Paths.get(targetFilePath).normalize();

        String fileName = path.getFileName().toString();
        String fileID = fileName.substring(0, fileName.indexOf('_'));

        try {
            SecretKey secretKey = encryptionKeyService.getKeyForFile(fileID);
            byte[] encryptedData = Files.readAllBytes(path);
            byte[] decryptedData = EncryptionUtil.decrypt(encryptedData, secretKey);

            return new ByteArrayResource(decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private boolean doesDirectoryExist(String directoryPath){
        Path path = Paths.get(directoryPath);
        return Files.exists(path) && Files.isDirectory(path);
    }

    private boolean createDirectory(String directoryPath) {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("Directory created: " + path);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to create directory: " + path);
                return false;
            }
        } else {
            System.out.println("Directory already exists: " + path);
            return true;
        }
    }

    private String getUserPathFromJWT(String jwtToken){
        if (!jwtUtil.isTokenValid(jwtToken)) {
            throw new SecurityException("Invalid or expired JWT token.");
        }

        String username = jwtUtil.extractSubject(jwtToken);
        Account userAccount = accountService.getAccountByUsername(username).orElse(null);
        if(userAccount != null) {
            AccountManager accountManager = accountManagerService.getManagerOfAccount(userAccount);
            System.out.println(accountManager.getRootdir());
            return accountManager.getRootdir();
        }
        return null;
    }

    private String sanitizePath(String inputPath) {
        String normalizedPath = Paths.get(inputPath).normalize().toString();
        return normalizedPath;
    }


    private long getFileSizeInBytes(Path path) {
        final AtomicLong size = new AtomicLong(0);

        try {
            Files.walk(path)
                    .filter(p -> p.toFile().isFile())
                    .forEach(p -> size.addAndGet(p.toFile().length()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return size.get();
    }

    private double bytesToMegabytes(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }

}


