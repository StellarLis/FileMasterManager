package ru.andrew.fileserver.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
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

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileUserDao fileUserDao;
    private final DatabaseFileDao databaseFileDao;

    @Value("${JWT_PRIVATE_KEY}")
    private String jwtKey;

    @Value("${FILES_PATH}")
    private String path;

    @Autowired
    public FileController(
            FileUserDao fileUserDao,
            DatabaseFileDao databaseFileDao
    ) {
        this.fileUserDao = fileUserDao;
        this.databaseFileDao = databaseFileDao;
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
        FileUser fileUser = fileUserDao.getCandidateByUserId(userId);
        long date = new Date().getTime();
        DatabaseFile databaseFile = new DatabaseFile(fileUser, filename, date, true);
        databaseFileDao.save(databaseFile);
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
        FileUser fileUser = fileUserDao.getCandidateByUserId(userId);
        List<DatabaseFile> filesList = databaseFileDao.getAllFilesByUser(fileUser);
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
            // Generating new FileUser without hashed password
            FileUser newFileUser = file.getFileUser();
            newFileUser.setPassword(null);
            file.setFileUser(newFileUser);
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
        DatabaseFile databaseFile = databaseFileDao.getFileById(fileId);
        // Checking if databaseFile == null
        if (databaseFile == null) {
            String body = new CustomHTTPError(404, "File not found").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(404));
        }
        // Get user_id from JWT
        String payload = new String(Base64.getUrlDecoder().decode(decodedJWT.getPayload()));
        JSONObject payloadJson = new JSONObject(payload);
        int userId = payloadJson.getInt("user_id");
        // Abort if you are not an owner and it's a private file
        if (databaseFile.isPrivate() && databaseFile.getFileUser().getId() != userId) {
            String body = new CustomHTTPError(403, "This file is private").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(403));
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
        // Modifying the json object that it is not having a fileuser data
        resultJson.put("id", databaseFile.getId());
        resultJson.put("filename", databaseFile.getFilename());
        resultJson.put("date", databaseFile.getDate());
        resultJson.put("owner", databaseFile.getFileUser().getUsername());
        resultJson.put("isPrivate", databaseFile.isPrivate());
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
        DatabaseFile databaseFile = databaseFileDao.getFileById(fileId);
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
        databaseFileDao.deleteFile(databaseFile);
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
            exception.printStackTrace();
            return new ResponseEntity<>("Server error", HttpStatusCode.valueOf(500));
        }
        return new ResponseEntity<>("{\"status\": 200}", HttpStatusCode.valueOf(200));
    }

    @GetMapping(value = "/download/{fileId}")
    @ResponseBody
    public ResponseEntity<String> download(
            @PathVariable int fileId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        DecodedJWT decodedJWT = UsefulFunctions.isAuthenticated(request, jwtKey);
        if (decodedJWT == null) {
            return ResponseEntity.status(401).body(null);
        }
        // Getting that file
        DatabaseFile databaseFile = databaseFileDao.getFileById(fileId);
        // Checking if database file is null
        if (databaseFile == null) {
            return ResponseEntity.status(404).body(null);
        }
        // Checking if a user is an owner of this file
        String payload = new String(Base64.getUrlDecoder().decode(decodedJWT.getPayload()));
        JSONObject payloadJson = new JSONObject(payload);
        int userId = payloadJson.getInt("user_id");
        if (databaseFile.isPrivate() && userId != databaseFile.getFileUser().getId()) {
            return ResponseEntity.status(403).body(null);
        }
        // Getting that file from storage
        File dir = new File(path);
        File[] matches = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Objects.equals(name, databaseFile.getFilename());
            }
        });
        // Creating InputStream and returning it
        try {
            File resultFile = matches[0];
            if (resultFile == null) throw new Exception();
            InputStream in = new FileInputStream(resultFile);
            response.setCharacterEncoding("UTF-8");
            IOUtils.copy(in, response.getOutputStream());
            return ResponseEntity.ok().contentType(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(null);
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping(value = "/changePrivacy/{fileId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> changePrivacy(
            @PathVariable int fileId,
            @RequestParam boolean newPrivacyOption,
            HttpServletRequest request
    ) {
        DecodedJWT decodedJWT = UsefulFunctions.isAuthenticated(request, jwtKey);
        if (decodedJWT == null) {
            String body = new CustomHTTPError(401, "Unauthorized").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(401));
        }
        // Getting that file
        DatabaseFile databaseFile = databaseFileDao.getFileById(fileId);
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
        // Change privacy
        databaseFile.setPrivate(newPrivacyOption);
        databaseFileDao.updateFile(databaseFile);
        return new ResponseEntity<>("{\"status\": 200}", HttpStatusCode.valueOf(200));
    }

    @GetMapping(value = "/search/{textInput}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> search(
            @PathVariable String textInput,
            @RequestParam int origin,
            HttpServletRequest request
    ) {
        DecodedJWT decodedJWT = UsefulFunctions.isAuthenticated(request, jwtKey);
        if (decodedJWT == null) {
            String body = new CustomHTTPError(401, "Unauthorized").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(401));
        }
        //
        // Searching for files
        List<DatabaseFile> filesList = databaseFileDao.searchFiles(origin, textInput);
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", 200);
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
        resultJson.put("files", filesList);
        return new ResponseEntity<>(resultJson.toString(), HttpStatusCode.valueOf(200));
    }
}
