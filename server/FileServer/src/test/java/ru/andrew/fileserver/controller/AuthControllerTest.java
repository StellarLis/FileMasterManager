package ru.andrew.fileserver.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.andrew.fileserver.entities.FileUser;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @MockBean
    private FileUserDao fileUserDao;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${JWT_PRIVATE_KEY}")
    private String jwtKey;

    @Test
    void signUp_successful_Returns200() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void signUp_usernameExists_Returns400() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void signUp_usernameIs7LettersSize_Returns400() throws Exception {
        FileUser fileUser = new FileUser("abcabcc", "password");
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void signUp_usernameIs8LettersSize_Returns200() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void signUp_usernameIs29LettersSize_Returns400() throws Exception {
        String username = "12345678912345678912345678912";
        FileUser fileUser = new FileUser(username, "password");
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void signUp_usernameIs28LettersSize_Returns200() throws Exception {
        String username = "1234567891234567891234567891";
        FileUser fileUser = new FileUser(username, "password");
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void signUp_passwordIs5LettersSize_Returns400() throws Exception {
        FileUser fileUser = new FileUser("username", "passw");
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void signUp_passwordIs6LettersSize_Returns200() throws Exception {
        FileUser fileUser = new FileUser("username", "passwo");
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void signUp_passwordIs28LettersSize_Returns200() throws Exception {
        String password = "1234567891234567891234567891";
        FileUser fileUser = new FileUser("username", password);
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_successful_Returns200() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        String hash = BCrypt.withDefaults()
                .hashToString(6, fileUser.getPassword().toCharArray());
        FileUser candidate = new FileUser("username", hash);
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_userDoesNotExist_Returns400() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
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
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_usernameIs7LettersSize_Returns400() throws Exception {
        FileUser fileUser = new FileUser("abcabcc", "password");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_usernameIs8LettersSize_Returns200() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        String hash = BCrypt.withDefaults()
                .hashToString(6, fileUser.getPassword().toCharArray());
        FileUser candidate = new FileUser("username", hash);
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_usernameIs29LettersSize_Returns400() throws Exception {
        String username = "12345678912345678912345678912";
        FileUser fileUser = new FileUser(username, "password");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_usernameIs28LettersSize_Returns200() throws Exception {
        String username = "1234567891234567891234567891";
        FileUser fileUser = new FileUser(username, "password");
        String hash = BCrypt.withDefaults()
                .hashToString(6, fileUser.getPassword().toCharArray());
        FileUser candidate = new FileUser("username", hash);
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_passwordIs5LettersSize_Returns400() throws Exception {
        FileUser fileUser = new FileUser("username", "passw");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_passwordIs6LettersSize_Returns200() throws Exception {
        FileUser fileUser = new FileUser("username", "passwo");
        String hash = BCrypt.withDefaults()
                .hashToString(6, fileUser.getPassword().toCharArray());
        FileUser candidate = new FileUser("username", hash);
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_passwordIs29LettersSize_Returns400() throws Exception {
        String password = "12345678912345678912345678912";
        FileUser fileUser = new FileUser("username", password);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_passwordIs28LettersSize_Returns200() throws Exception {
        String password = "1234567891234567891234567891";
        FileUser fileUser = new FileUser("username", password);
        String hash = BCrypt.withDefaults()
                .hashToString(6, fileUser.getPassword().toCharArray());
        FileUser candidate = new FileUser("username", hash);
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
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
