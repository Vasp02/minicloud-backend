package com.example.demo.cloudfile;

import com.example.demo.cloudfile.dto.CreateFolderDTO;
import com.example.demo.cloudfile.dto.FileDTO;
import com.example.demo.jwtUtil.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;


import java.util.List;

@RestController
@RequestMapping(path = "/api/cloud")
public class CloudFileController {

    private final CloudFileService cloudFileService;
    private final JWTUtil jwtUtil;

    @Autowired
    public CloudFileController(CloudFileService cloudFileService, JWTUtil jwtUtil){
        this.cloudFileService = cloudFileService;
        this.jwtUtil = jwtUtil;
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                         @RequestParam("userPath") String userPath,
                                         @RequestHeader("Authorization") String jwtToken) {
        if (jwtToken != null && !jwtToken.isEmpty()) {
            try {
                cloudFileService.uploadFiles(files, userPath, jwtToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @GetMapping("/getfiles")
    public ResponseEntity<?> getFiles(@RequestParam("path") String directoryPath,
                                      @RequestHeader("Authorization") String jwtToken){
        List<FileDTO> fileList = cloudFileService.getFilesAtPath(directoryPath,jwtToken);
        return ResponseEntity.ok(fileList);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestParam("path") String path,
                                        @RequestHeader("Authorization") String jwtToken) {
        try {
            boolean isDeleted = cloudFileService.deleteFile(path,jwtToken);
            if (isDeleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting file: " + e.getMessage());
        }
    }

    @PostMapping("/createFolder")
    public ResponseEntity<String> createFolder(@RequestBody CreateFolderDTO createFolderDTO,
                                               @RequestHeader("Authorization") String jwtToken) {
        String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired JWT token.");
        }
        String userDir = jwtUtil.extractSubject(jwtToken);
        String path = createFolderDTO.getPath() + "/" + createFolderDTO.getFolderName();
        cloudFileService.createFolder(path,jwtToken);
        return null;
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("path") String filePath,
                                                 @RequestHeader("Authorization") String jwtToken) {
        if (!jwtUtil.isTokenValid(jwtToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Resource resource = cloudFileService.getDownloadResource(filePath,jwtToken);
        if(resource.exists()) {
            String contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }

    }
}
