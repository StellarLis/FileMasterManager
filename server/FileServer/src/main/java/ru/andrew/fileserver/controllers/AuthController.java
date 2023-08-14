package ru.andrew.fileserver.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.andrew.fileserver.dao.FileUserDao;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.util.CustomHTTPError;
import ru.andrew.fileserver.util.SessionFactoryImpl;

import java.security.SecureRandom;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final SessionFactoryImpl sessionFactoryImpl;
    @Value("${JWT_PRIVATE_KEY}")
    private String jwtKey;

    @Autowired
    public AuthController(SessionFactoryImpl sessionFactoryImpl) {
        this.sessionFactoryImpl = sessionFactoryImpl;
    }

    @PostMapping(value = "/signup", produces = "application/json")
    public ResponseEntity<String> signUp(@RequestBody FileUser fileUser) {
        Session session = sessionFactoryImpl.getSessionFactory().openSession();
        // Checking database for existing username
        FileUserDao fileUserDao = new FileUserDao(fileUser, session);
        FileUser candidate = fileUserDao.getCandidate();
        if (candidate != null) {
            String body = new CustomHTTPError(400, "This user with that username" +
                    " already exists").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(400));
        }
        // Generating hashed password and putting it in FileUserDao object.
        String hash = BCrypt.withDefaults()
                .hashToString(6, fileUserDao.getPassword().toCharArray());
        fileUserDao.setPassword(hash);
        // Saving a user in database
        fileUserDao.save();
        // Creating a JWT key
        Algorithm algorithm = Algorithm.HMAC256(jwtKey);
        String token = JWT.create().withIssuer("auth0").sign(algorithm);
        return new ResponseEntity<>(
                "{\"status\": 200, \"token\": \"" + token + "\"}",
                HttpStatusCode.valueOf(200)
        );
    }

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<String> login(@RequestBody FileUser fileUser) {
        Session session = sessionFactoryImpl.getSessionFactory().openSession();
        FileUserDao fileUserDao = new FileUserDao(fileUser, session);
        FileUser candidate = fileUserDao.getCandidate();
        if (candidate == null) {
            String body = new CustomHTTPError(400, "Invalid username or password").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(400));
        }
        // A candidate has hashed password
        BCrypt.Result bcryptResult = BCrypt.verifyer()
                .verify(fileUserDao.getPassword().toCharArray(), candidate.getPassword());
        if (!bcryptResult.verified) {
            String body = new CustomHTTPError(400, "Invalid username or password").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(400));
        }
        // If username and password are valid, then we give a JWT token
        Algorithm algorithm = Algorithm.HMAC256(jwtKey);
        String token = JWT.create().withIssuer("auth0").sign(algorithm);
        return new ResponseEntity<>(
                "{\"status\": 200, \"token\": \"" + token + "\"}",
                HttpStatusCode.valueOf(200)
        );
        // SHOULD UPDATE HASHING ALGORITHM FOR JWT TOKENS!!!!!!!!!!!!!!!!!!!!!
    }
}
