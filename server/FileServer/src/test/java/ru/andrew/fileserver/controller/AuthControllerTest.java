package ru.andrew.fileserver.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.andrew.fileserver.Application;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.repository.DatabaseFileRepository;
import ru.andrew.fileserver.repository.FileUserRepository;

import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ExtendWith(SpringExtension.class)
//@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Application.class})
@WebMvcTest(controllers = AuthController.class)
//@DataJpaTest
class AuthControllerTest {

    @MockBean
    @Autowired
    private DatabaseFileRepository databaseFileRepository;
    @MockBean
    @Autowired
    private FileUserRepository fileUserRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signUp_successful_Returns200() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "password",
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void signUp_usernameExists_Returns400() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "password",
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void signUp_usernameIs7LettersSize_Returns400() throws Exception {
        FileUser fileUser = new FileUser(1L, "abcabcc", "password",
                new ArrayList<>());
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void signUp_usernameIs8LettersSize_Returns200() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "password",
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void signUp_usernameIs29LettersSize_Returns400() throws Exception {
        String username = "12345678912345678912345678912";
        FileUser fileUser = new FileUser(1L, username, "password",
                new ArrayList<>());
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void signUp_usernameIs28LettersSize_Returns200() throws Exception {
        String username = "1234567891234567891234567891";
        FileUser fileUser = new FileUser(1L, username, "password",
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void signUp_passwordIs5LettersSize_Returns400() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "passw",
                new ArrayList<>());
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void signUp_passwordIs6LettersSize_Returns200() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "passwo",
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void signUp_passwordIs28LettersSize_Returns200() throws Exception {
        String password = "1234567891234567891234567891";
        FileUser fileUser = new FileUser(1L, "username", password,
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_successful_Returns200() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "password",
                new ArrayList<>());
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String hash = bCryptPasswordEncoder.encode(fileUser.getPassword());
        FileUser candidate = new FileUser(1L, "username", hash,
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_userDoesNotExist_Returns400() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "password",
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(null);
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_incorrectPassword_Returns400() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "password",
                new ArrayList<>());
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String hash = bCryptPasswordEncoder.encode("other_password");
        FileUser candidate = new FileUser(1L, "username", hash,
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_usernameIs7LettersSize_Returns400() throws Exception {
        FileUser fileUser = new FileUser(1L, "abcabcc", "password",
                new ArrayList<>());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_usernameIs8LettersSize_Returns200() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "password",
                new ArrayList<>());
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String hash = bCryptPasswordEncoder.encode(fileUser.getPassword());
        FileUser candidate = new FileUser(1L, "username", hash,
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_usernameIs29LettersSize_Returns400() throws Exception {
        String username = "12345678912345678912345678912";
        FileUser fileUser = new FileUser(1L, username, "password",
                new ArrayList<>());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_usernameIs28LettersSize_Returns200() throws Exception {
        String username = "1234567891234567891234567891";
        FileUser fileUser = new FileUser(1L, username, "password",
                new ArrayList<>());
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String hash = bCryptPasswordEncoder.encode(fileUser.getPassword());
        FileUser candidate = new FileUser(1L, "username", hash,
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_passwordIs5LettersSize_Returns400() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "passw",
                new ArrayList<>());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_passwordIs6LettersSize_Returns200() throws Exception {
        FileUser fileUser = new FileUser(1L, "username", "passwo",
                new ArrayList<>());
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String hash = bCryptPasswordEncoder.encode(fileUser.getPassword());
        FileUser candidate = new FileUser(1L, "username", hash,
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }

    @Test
    void login_passwordIs29LettersSize_Returns400() throws Exception {
        String password = "12345678912345678912345678912";
        FileUser fileUser = new FileUser(1L, "username", password,
                new ArrayList<>());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().is(400));
    }

    @Test
    void login_passwordIs28LettersSize_Returns200() throws Exception {
        String password = "1234567891234567891234567891";
        FileUser fileUser = new FileUser(1L, "username", password,
                new ArrayList<>());
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String hash = bCryptPasswordEncoder.encode(fileUser.getPassword());
        FileUser candidate = new FileUser(1L, "username", hash,
                new ArrayList<>());
        when(fileUserRepository.findUserByUsername(fileUser.getUsername()))
                .thenReturn(candidate);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }
}
