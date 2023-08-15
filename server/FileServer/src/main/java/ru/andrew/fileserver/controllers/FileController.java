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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
        JSONObject jsonObject = new JSONObject(payload);
        String username = jsonObject.get("username").toString();
        // Setting the filename
        String filename = username + '.' + multipartFile.getOriginalFilename();
        // Saving file data to the database
        Session session = sessionFactory.getSessionFactory().openSession();
        FileUser fileUser = FileUserDao.getCandidate(username, session);
        DatabaseFile databaseFile = new DatabaseFile(fileUser, filename);
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
        return new ResponseEntity<>("\"status\": 200", HttpStatusCode.valueOf(200));
    }
}
