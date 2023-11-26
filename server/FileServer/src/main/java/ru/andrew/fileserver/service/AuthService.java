package ru.andrew.fileserver.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.andrew.fileserver.configuration.JwtUtils;
import ru.andrew.fileserver.dto.AuthResponse;
import ru.andrew.fileserver.dto.AuthResponseWithToken;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.repository.FileUserRepository;

import java.util.ArrayList;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final Validator validator;
    private final JwtUtils jwtUtils;
    private final FileUserRepository fileUserRepository;

    public ResponseEntity<AuthResponse> signUp(FileUser fileUser) {
        // Validating fileUser
        Set<ConstraintViolation<FileUser>> constraintViolations = validator.validate(fileUser);
        if (!constraintViolations.isEmpty()) {
            AuthResponse authResponse = new AuthResponse("Bad Request. Try again");
            return ResponseEntity.status(400).body(authResponse);
        }
        // Checking database for existing username
        FileUser candidate = fileUserRepository.findUserByUsername(fileUser.getUsername());
        if (candidate != null) {
            AuthResponse authResponse = new AuthResponse("This user with that username already exists");
            return ResponseEntity.status(400).body(authResponse);
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String hash = bCryptPasswordEncoder.encode(fileUser.getPassword());
        fileUser.setPassword(hash);
        FileUser savedUser = fileUserRepository.saveAndFlush(fileUser);
        // Generating a JWT token
        UserDetails userDetails = new User(savedUser.getUsername(),
                savedUser.getPassword(),
                new ArrayList<>()
        );
        String token = jwtUtils.generateToken(userDetails);
        AuthResponseWithToken response = new AuthResponseWithToken(
                "Signed up successfully", token);
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<AuthResponse> login(FileUser fileUser) {
        // Validating fileUser
        Set<ConstraintViolation<FileUser>> constraintViolations = validator.validate(fileUser);
        if (!constraintViolations.isEmpty()) {
            AuthResponse authResponse = new AuthResponse("Bad Request. Try again");
            return ResponseEntity.status(400).body(authResponse);
        }
        // Getting candidate
        FileUser candidate = fileUserRepository.findUserByUsername(fileUser.getUsername());
        if (candidate == null) {
            AuthResponse authResponse = new AuthResponse("Invalid username or password");
            return ResponseEntity.status(400).body(authResponse);
        }
        // A candidate has hashed password
        boolean isPasswordCorrect = BCrypt.checkpw(
                fileUser.getPassword(),
                candidate.getPassword()
        );
        if (!isPasswordCorrect) {
            AuthResponse authResponse = new AuthResponse("Invalid username or password");
            return ResponseEntity.status(400).body(authResponse);
        }
        // If username and password are valid, then we give a JWT token
        UserDetails userDetails = new User(candidate.getUsername(),
                candidate.getPassword(),
                new ArrayList<>());
        String token = jwtUtils.generateToken(userDetails);
        AuthResponseWithToken authResponse = new AuthResponseWithToken(
                "Logged in successfully", token
        );
        return ResponseEntity.ok().body(authResponse);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        FileUser loadedUser = fileUserRepository.findUserByUsername(username);
        if (loadedUser == null) return null;
        return new User(
                loadedUser.getUsername(),
                loadedUser.getPassword(),
                new ArrayList<>()
        );
    }
}
