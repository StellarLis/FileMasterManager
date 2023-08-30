package ru.andrew.fileserver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.andrew.fileserver.dao.FileUserDao;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.util.SessionFactoryImpl;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private Session session;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getSession()).thenReturn(session);
    }

    @Test
    void signUp_Returns200() throws Exception {
        FileUser fileUser = new FileUser("username", "password");
        when(fileUserDao.getCandidateByUsername(fileUser.getUsername(), session))
                .thenReturn(null).thenReturn(fileUser);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileUser)))
                .andExpect(status().isOk());
    }
}
