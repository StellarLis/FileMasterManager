package ru.andrew.fileserver.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.Session;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.andrew.fileserver.dao.FileUserDao;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.util.CustomHTTPError;
import ru.andrew.fileserver.util.SessionFactoryImpl;
import ru.andrew.fileserver.util.UsefulFunctions;

import java.security.SecureRandom;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final SessionFactoryImpl sessionFactoryImpl;
    private final FileUserDao fileUserDao;
    private Validator validator;

    @Value("${JWT_PRIVATE_KEY}")
    private String jwtKey;

    @Autowired
    public AuthController(
            SessionFactoryImpl sessionFactoryImpl,
            FileUserDao fileUserDao
    ) {
        this.sessionFactoryImpl = sessionFactoryImpl;
        this.fileUserDao = fileUserDao;
        // Getting validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @PostMapping(value = "/signup", produces = "application/json")
    public ResponseEntity<String> signUp(@RequestBody FileUser fileUser) {
        Session session = sessionFactoryImpl.getSession();
        // Validating fileUser
        Set<ConstraintViolation<FileUser>> constraintViolations = validator.validate(fileUser);
        if (!constraintViolations.isEmpty()) {
            String body = new CustomHTTPError(400, "Bad Request. Try again").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(400));
        }
        // Checking database for existing username
        FileUser candidate = fileUserDao.getCandidateByUsername(fileUser.getUsername(), session);
        if (candidate != null) {
            String body = new CustomHTTPError(400, "This user with that username" +
                    " already exists").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(400));
        }
        // Generating hashed password and putting it in FileUser object.
        String hash = BCrypt.withDefaults()
                .hashToString(6, fileUser.getPassword().toCharArray());
        fileUser.setPassword(hash);
        // Saving a user in database
        fileUserDao.save(fileUser, session);
        // Getting user id and saving it in the payload
        candidate = fileUserDao.getCandidateByUsername(fileUser.getUsername(), session);
        JSONObject payloadJson = new JSONObject();
        payloadJson.put("username", fileUser.getUsername());
        payloadJson.put("user_id", candidate.getId());
        String payload = payloadJson.toString();
        // Generating a JWT token
        Algorithm algorithm = Algorithm.HMAC256(jwtKey);
        String token = JWT.create().withPayload(payload).withIssuer("auth0").sign(algorithm);
        return new ResponseEntity<>(
                "{\"status\": 200, \"token\": \"" + token + "\"}",
                HttpStatusCode.valueOf(200)
        );
    }

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<String> login(@RequestBody FileUser fileUser) {
        Session session = sessionFactoryImpl.getSession();
        // Validating fileUser
        Set<ConstraintViolation<FileUser>> constraintViolations = validator.validate(fileUser);
        if (!constraintViolations.isEmpty()) {
            String body = new CustomHTTPError(400, "Bad Request. Try again").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(400));
        }
        // Getting candidate
        FileUser candidate = fileUserDao.getCandidateByUsername(fileUser.getUsername(), session);
        if (candidate == null) {
            String body = new CustomHTTPError(400, "Invalid username or password").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(400));
        }
        // A candidate has hashed password
        BCrypt.Result bcryptResult = BCrypt.verifyer()
                .verify(fileUser.getPassword().toCharArray(), candidate.getPassword());
        if (!bcryptResult.verified) {
            String body = new CustomHTTPError(400, "Invalid username or password").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(400));
        }
        // If username and password are valid, then we give a JWT token
        Algorithm algorithm = Algorithm.HMAC256(jwtKey);
        JSONObject payloadJson = new JSONObject();
        payloadJson.put("username", fileUser.getUsername());
        payloadJson.put("user_id", candidate.getId());
        String payload = payloadJson.toString();
        String token = JWT.create().withPayload(payload).withIssuer("auth0").sign(algorithm);
        return new ResponseEntity<>(
                "{\"status\": 200, \"token\": \"" + token + "\"}",
                HttpStatusCode.valueOf(200)
        );
    }

    @GetMapping(value = "/authenticate", produces = "application/json")
    public ResponseEntity<String> authenticate(HttpServletRequest request) {
        DecodedJWT decodedJWT = UsefulFunctions.isAuthenticated(request, jwtKey);
        if (decodedJWT == null) {
            String body = new CustomHTTPError(401, "Unauthorized").toString();
            return new ResponseEntity<>(body, HttpStatusCode.valueOf(401));
        }
        String body = "{\"status\": 200}";
        return new ResponseEntity<>(body, HttpStatusCode.valueOf(200));
    }
}
