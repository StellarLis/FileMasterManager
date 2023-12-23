package ru.andrew.fileserver.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.andrew.fileserver.dto.FileResponse;
import ru.andrew.fileserver.dto.FileResponseWithFiles;
import ru.andrew.fileserver.dto.FileResponseWithOneFile;
import ru.andrew.fileserver.entities.DatabaseFile;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.repository.DatabaseFileRepository;
import ru.andrew.fileserver.repository.FileUserRepository;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final FileUserRepository fileUserRepository;
    private final DatabaseFileRepository databaseFileRepository;
    @Value("${JWT_PRIVATE_KEY}")
    private String jwtKey;
    @Value("${FILES_PATH}")
    private String path;

    public ResponseEntity<FileResponse> upload(
            MultipartFile multipartFile
    ) {
        String username = this.getUsername();
        // Setting up the filename
        Random random = new Random();
        int randomNumber = random.nextInt(1000, 10000);
        String filename = username + '_' + randomNumber + '.' + multipartFile.getOriginalFilename();
        // Saving file data to the database
        FileUser fileUser = fileUserRepository.findUserByUsername(username);
        long date = new Date().getTime();
        DatabaseFile databaseFile = new DatabaseFile(null, fileUser, filename, true, date);
        databaseFileRepository.save(databaseFile);
        // Creating a file
        File file = new File(path + File.separator + filename);
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            log.error("Error occurred while creating a file: " + e.getMessage());
            FileResponse fileResponse = new FileResponse("Some error occurred");
            return ResponseEntity.status(500).body(fileResponse);
        }
        FileResponse fileResponse = new FileResponse("Uploaded file successfully!");
        return ResponseEntity.ok().body(fileResponse);
    }

    public ResponseEntity<FileResponseWithFiles> getFilesByUsername() {
        String username = this.getUsername();
        // Getting all files from database
        FileUser fileUser = fileUserRepository.findUserByUsername(username);
        List<DatabaseFile> filesList = fileUser.getDbFiles();
        // Forming original filenames of each file
        for (DatabaseFile file : filesList) {
            String[] filenameArr = file.getFilename().split("\\.");
            StringBuilder newFilename = new StringBuilder();
            for (int i = 1; i < filenameArr.length-1; i++) {
                newFilename.append(filenameArr[i] + '.');
            }
            newFilename.append(filenameArr[filenameArr.length-1]);
            file.setFilename(newFilename.toString());
            // Generating new FileUser without hashed password
            FileUser newFileUser = file.getFileUser();
            newFileUser.setPassword(null);
            file.setFileUser(newFileUser);
        }
        FileResponseWithFiles fileResponseWithFiles = new FileResponseWithFiles(filesList);
        return ResponseEntity.ok().body(fileResponseWithFiles);
    }

    public ResponseEntity<FileResponse> getFileById(long fileId) {
        // Getting file from database
        Optional<DatabaseFile> databaseFileOpt = databaseFileRepository.findById(fileId);
        // Checking if databaseFile == null
        if (databaseFileOpt.isEmpty()) {
            FileResponse fileResponse = new FileResponse("File not found");
            return ResponseEntity.status(404).body(fileResponse);
        }
        DatabaseFile databaseFile = databaseFileOpt.get();
        String username = this.getUsername();
        // Abort if you are not an owner and it's a private file
        if (databaseFile.isPrivate() &&
                !databaseFile.getFileUser().getUsername().equals(username)) {
            FileResponse fileResponse = new FileResponse("This file is private");
            return ResponseEntity.status(403).body(fileResponse);
        }
        // Forming original filename
        String[] filenameArr = databaseFile.getFilename().split("\\.");
        StringBuilder newFilename = new StringBuilder();
        for (int i = 1; i < filenameArr.length-1; i++) {
            newFilename.append(filenameArr[i] + '.');
        }
        newFilename.append(filenameArr[filenameArr.length-1]);
        databaseFile.setFilename(newFilename.toString());
        // Creating response
        FileResponseWithOneFile fileResponse = new FileResponseWithOneFile("OK", databaseFile);
        return ResponseEntity.ok().body(fileResponse);
    }

    public ResponseEntity<FileResponse> deleteFileById(long fileId) {
        Optional<DatabaseFile> databaseFileOpt = databaseFileRepository.findById(fileId);
        // Checking if databaseFile is null
        if (databaseFileOpt.isEmpty()) {
            FileResponse fileResponse = new FileResponse("File not found");
            return ResponseEntity.status(404).body(fileResponse);
        }
        DatabaseFile databaseFile = databaseFileOpt.get();
        // Checking if a user is an owner of this file
        String username = this.getUsername();
        if (!databaseFile.getFileUser().getUsername().equals(username)) {
            FileResponse fileResponse = new FileResponse("You are not an owner of this file");
            return ResponseEntity.status(403).body(fileResponse);
        }
        // Deleting a file from database
        databaseFileRepository.delete(databaseFile);
        // Deleting a file from storage
        File dir = new File(path);
        File[] matches = dir.listFiles((dir1, name) -> Objects.equals(name, databaseFile.getFilename()));
        try {
            matches[0].delete();
        } catch (Exception e) {
            log.error("Error occurred while deleting a file: " + e.getMessage());
            FileResponse fileResponse = new FileResponse("Some error occurred");
            return ResponseEntity.status(500).body(fileResponse);
        }
        FileResponse fileResponse = new FileResponse("Successfully deleted!");
        return ResponseEntity.ok().body(fileResponse);
    }

    public ResponseEntity<String> download(
            long fileId,
            HttpServletResponse response
    ) {
        // Getting that file
        Optional<DatabaseFile> databaseFileOpt = databaseFileRepository.findById(fileId);
        // Checking if database file is null
        if (databaseFileOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }
        DatabaseFile databaseFile = databaseFileOpt.get();
        // Checking if a user is an owner of this file
        String username = this.getUsername();
        if (databaseFile.isPrivate() &&
                !databaseFile.getFileUser().getUsername().equals(username)) {
            log.debug("File is private and you are not an owner.");
            return ResponseEntity.status(405).body(null);
        }
        // Getting that file from storage
        File dir = new File(path);
        File[] matches = dir.listFiles((dir1, name) -> Objects.equals(name, databaseFile.getFilename()));
        // Creating InputStream and returning it
        try {
            File resultFile = matches[0];
            if (resultFile == null) throw new Exception();
            InputStream in = new FileInputStream(resultFile);
            response.setCharacterEncoding("UTF-8");
            IOUtils.copy(in, response.getOutputStream());
            return ResponseEntity.ok().contentType(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(null);
        } catch (Exception e) {
            log.error("Error occurred while downloading: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    public ResponseEntity<FileResponse> changePrivacy(
            long fileId,
            boolean newPrivacyOption
    ) {
        // Getting that file
        Optional<DatabaseFile> databaseFileOpt = databaseFileRepository.findById(fileId);
        // Checking if databaseFile is null
        if (databaseFileOpt.isEmpty()) {
            FileResponse fileResponse = new FileResponse("File not found");
            return ResponseEntity.status(404).body(fileResponse);
        }
        DatabaseFile databaseFile = databaseFileOpt.get();
        // Checking if a user is an owner of this file
        String username = this.getUsername();
        if (!databaseFile.getFileUser().getUsername().equals(username)) {
            FileResponse fileResponse = new FileResponse("You are not an owner of this file");
            return ResponseEntity.status(403).body(fileResponse);
        }
        // Change privacy
        databaseFile.setPrivate(newPrivacyOption);
        databaseFileRepository.save(databaseFile);
        FileResponse fileResponse = new FileResponse("Successfully changed privacy");
        return ResponseEntity.ok().body(fileResponse);
    }

    public ResponseEntity<FileResponseWithFiles> search(String textInput) {
        List<DatabaseFile> filesList = databaseFileRepository.searchForFilesByName(textInput);
        for (DatabaseFile file : filesList) {
            String[] filenameArr = file.getFilename().split("\\.");
            StringBuilder newFilename = new StringBuilder();
            for (int i = 1; i < filenameArr.length-1; i++) {
                newFilename.append(filenameArr[i] + '.');
            }
            newFilename.append(filenameArr[filenameArr.length-1]);
            file.setFilename(newFilename.toString());
            // Generating new FileUser without hashed password
            FileUser newFileUser = file.getFileUser();
            newFileUser.setPassword(null);
            file.setFileUser(newFileUser);
        }
        FileResponseWithFiles fileResponse = new FileResponseWithFiles(filesList);
        return ResponseEntity.ok().body(fileResponse);
    }

    private String getUsername() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getUsername();
    }
}
