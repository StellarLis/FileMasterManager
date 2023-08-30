package ru.andrew.fileserver.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.andrew.fileserver.dao.FileUserDao;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.util.SessionFactoryImpl;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @MockBean
    private FileUserDao fileUserDao;
    @MockBean
    private SessionFactoryImpl sessionFactory;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${JWT_PRIVATE_KEY}")
    private String jwtKey;

    private Session session;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getSession()).thenReturn(session);
    }

    @Test
    void signUp_successful_Returns200() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername(), session))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void signUp_usernameExists_Returns400() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername(), session))
                .thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_successful_Returns200() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        String hash = BCrypt.withDefaults()
                .hashToString(6, fileUser.getPassword().toCharArray());
        FileUser candidate = new FileUser("username", hash);
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername(), session))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_userDoesNotExist_Returns400() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername(), session))
                .thenReturn(null);
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_incorrectPassword_Returns400() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        String hash = BCrypt.withDefaults()
                .hashToString(6, "other_password".toCharArray());
        FileUser candidate = new FileUser("username", hash);
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername(), session))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void authenticate_successful_Returns200() throws Exception {
        Algorithm algorithm = Algorithm.HMAC256(jwtKey);
        String token = JWT.create().withIssuer("auth0").sign(algorithm);
        mockMvc.perform(get("/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void authenticate_userDoesNotHaveAToken_Returns401() throws Exception {
        mockMvc.perform(get("/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(401));
    }

    @Test
    void authenticate_userHasInvalidToken_Returns401() throws Exception {
        String token = "random_incorrect_token";
        mockMvc.perform(get("/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token))
                .andExpect(status().is(401));
    }
}