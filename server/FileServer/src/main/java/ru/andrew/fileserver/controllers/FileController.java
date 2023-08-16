package ru.andrew.fileserver.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.andrew.fileserver.dao.DatabaseFileDao;
import ru.andrew.fileserver.dao.FileUserDao;
import ru.andrew.fileserver.entities.DatabaseFile;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.util.CustomHTTPError;
import ru.andrew.fileserver.util.SessionFactoryImpl;
import ru.andrew.fileserver.util.UsefulFunctions;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

@RestController()
@RequestMapping("/files")
public class FileController {
    private SessionFactoryImpl sessionFactory;

    @Value("${JWT_PRIVATE_KEY}")
    private String jwtKey;

    @Value("${FILES_PATH}")
    private String path;

    @Autowired
    public FileController(SessionFactoryImpl sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @PostMapping(value = "/upload", produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(
            HttpServletRequest request,
            @RequestParam MultipartFile multipartFile
    ) {
        DecodedJWT decodedJWT = UsefulFunctions.isAuthenticated(request, jwtKey);
        if (decodedJWT == null) {
            String body = new CustomHTTPError(401, "Unauthorized").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(401));
        }
        // Retrieving username from jwt payload
        String payload = new String(Base64.getUrlDecoder().decode(decodedJWT.getPayload()));
        JSONObject payloadJson = new JSONObject(payload);
        String username = payloadJson.getString("username");
        int userId = payloadJson.getInt("user_id");
        // Setting up the filename
        Random random = new Random();
        int randomNumber = random.nextInt(1000, 10000);
        String filename = username + '_' + randomNumber + '.' + multipartFile.getOriginalFilename();
        // Saving file data to the database
        Session session = sessionFactory.getSessionFactory().openSession();
        FileUser fileUser = FileUserDao.getCandidate(userId, session);
        long date = new Date().getTime();
        DatabaseFile databaseFile = new DatabaseFile(fileUser, filename, date);
        DatabaseFileDao databaseFileDao = new DatabaseFileDao(databaseFile, session);
        databaseFileDao.save();
        // Creating a file
        File file = new File(path + File.separator + filename);
        try {
            multipartFile.transferTo(file);
        } catch (IOException exception) {
            exception.printStackTrace();
            String body = new CustomHTTPError(500, "IOException error").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(500));
        }
        return new ResponseEntity<>("{\"status\": 200}", HttpStatusCode.valueOf(200));
    }

    @GetMapping(value = "/getFiles", produces = "application/json")
    public ResponseEntity<String> getFilesByUsername(HttpServletRequest request) {
        DecodedJWT decodedJWT = UsefulFunctions.isAuthenticated(request, jwtKey);
        if (decodedJWT == null) {
            String body = new CustomHTTPError(401, "Unauthorized").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(401));
        }
        // Getting userId
        String payload = new String(Base64.getUrlDecoder().decode(decodedJWT.getPayload()));
        JSONObject payloadJson = new JSONObject(payload);
        int userId = payloadJson.getInt("user_id");
        // Getting all files from database
        Session session = sessionFactory.getSessionFactory().openSession();
        FileUser fileUser = FileUserDao.getCandidate(userId, session);
        List<DatabaseFile> filesList = DatabaseFileDao.getAllFilesByUser(fileUser, session);
        JSONObject resultObject = new JSONObject();
        resultObject.put("status", 200);
        // Forming original filenames of each file
        for (DatabaseFile file : filesList) {
            String[] filenameArr = file.getFilename().split("\\.");
            StringBuilder newFilename = new StringBuilder();
            for (int i = 1; i < filenameArr.length-1; i++) {
                newFilename.append(filenameArr[i] + '.');
            }
            newFilename.append(filenameArr[filenameArr.length-1]);
            file.setFilename(newFilename.toString());
        }
        resultObject.put("files", filesList);
        return new ResponseEntity<>(resultObject.toString(), HttpStatusCode.valueOf(200));
    }

    @GetMapping(value = "/getFileById/{fileId}", produces = "application/json")
    public ResponseEntity<String> getFileById(@PathVariable int fileId, HttpServletRequest request) {
        DecodedJWT decodedJWT = UsefulFunctions.isAuthenticated(request, jwtKey);
        if (decodedJWT == null) {
            String body = new CustomHTTPError(401, "Unauthorized").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(401));
        }
        // Getting file from database
        Session session = sessionFactory.getSessionFactory().openSession();
        DatabaseFile databaseFile = DatabaseFileDao.getFileById(fileId, session);
        // Checking if databaseFile == null
        if (databaseFile == null) {
            String body = new CustomHTTPError(404, "File not found").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(404));
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
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", 200);
        List<DatabaseFile> singleList = new ArrayList<>();
        singleList.add(databaseFile);
        resultJson.put("file", singleList);
        return new ResponseEntity<>(resultJson.toString(), HttpStatusCode.valueOf(200));
    }

    @DeleteMapping(value = "/deleteFileById/{fileId}", produces = "application/json")
    public ResponseEntity<String> deleteFileById(
            HttpServletRequest request,
            @PathVariable int fileId
    ) {
        DecodedJWT decodedJWT = UsefulFunctions.isAuthenticated(request, jwtKey);
        if (decodedJWT == null) {
            String body = new CustomHTTPError(401, "Unauthorized").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(401));
        }
        // Getting that file
        Session session = sessionFactory.getSessionFactory().openSession();
        DatabaseFile databaseFile = DatabaseFileDao.getFileById(fileId, session);
        // Checking if databaseFile is null
        if (databaseFile == null) {
            String body = new CustomHTTPError(404, "File not found").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(404));
        }
        // Checking if a user is an owner of this file
        String payload = new String(Base64.getUrlDecoder().decode(decodedJWT.getPayload()));
        JSONObject payloadJson = new JSONObject(payload);
        int userId = payloadJson.getInt("user_id");
        if (userId != databaseFile.getFileUser().getId()) {
            String body = new CustomHTTPError(403, "You are not an owner of this file").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(403));
        }
        // Deleting a file from database
        DatabaseFileDao.deleteFile(databaseFile, session);
        // Deleting a file from storage
        File dir = new File(path);
        File[] matches = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Objects.equals(name, databaseFile.getFilename());
            }
        });
        try {
            matches[0].delete();
        } catch (Exception exception) {
            return new ResponseEntity<>("Server error", HttpStatusCode.valueOf(500));
        }
        return new ResponseEntity<>("{\"status\": 200}", HttpStatusCode.valueOf(200));
    }
}
