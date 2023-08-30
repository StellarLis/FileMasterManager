package ru.andrew.fileserver.dao;

import org.hibernate.Session;
import org.springframework.stereotype.Component;
import ru.andrew.fileserver.entities.FileUser;

@Component
public class FileUserDao {
    public FileUser getCandidateByUsername(String username, Session session) {
        return session
                .createQuery("FROM FileUser WHERE username = :username", FileUser.class)
                .setParameter("username", username)
                .uniqueResult();
    }
    public FileUser getCandidateByUserId(int userId, Session session) {
        return session
                .createQuery("FROM FileUser WHERE id = :userId", FileUser.class)
                .setParameter("userId", userId)
                .uniqueResult();
    }

    public void save(FileUser fileUser, Session session) {
        session.beginTransaction();
        session.persist(fileUser);
        session.getTransaction().commit();
    }
}
