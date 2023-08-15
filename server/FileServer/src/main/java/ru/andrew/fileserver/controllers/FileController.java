package ru.andrew.fileserver.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
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
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;

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
        // Setting the filename
        String filename = username + '.' + multipartFile.getOriginalFilename();
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
        List<DatabaseFile> filesList = DatabaseFileDao.getAllFilesByUserId(fileUser, session);
        JSONObject resultObject = new JSONObject();
        resultObject.put("status", 200);
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
}
