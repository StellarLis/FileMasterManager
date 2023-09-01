package ru.andrew.fileserver.dao;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.util.SessionFactoryImpl;

@Component
public class FileUserDao {

    private final SessionFactoryImpl sessionFactoryImpl;

    @Autowired
    public FileUserDao(SessionFactoryImpl sessionFactoryImpl) {
        this.sessionFactoryImpl = sessionFactoryImpl;
    }

    public FileUser getCandidateByUsername(String username) {
        Session session = sessionFactoryImpl.getSessionFactory().openSession();
        FileUser result = session
                .createQuery("FROM FileUser WHERE username = :username", FileUser.class)
                .setParameter("username", username)
                .uniqueResult();
        session.close();
        return result;
    }
    public FileUser getCandidateByUserId(int userId) {
        Session session = sessionFactoryImpl.getSessionFactory().openSession();
        FileUser result = session
                .createQuery("FROM FileUser WHERE id = :userId", FileUser.class)
                .setParameter("userId", userId)
                .uniqueResult();
        session.close();
        return result;
    }

    public void save(FileUser fileUser) {
        Session session = sessionFactoryImpl.getSessionFactory().openSession();
        session.beginTransaction();
        session.persist(fileUser);
        session.getTransaction().commit();
        session.close();
    }
}
