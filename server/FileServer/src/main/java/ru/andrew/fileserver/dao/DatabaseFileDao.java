package ru.andrew.fileserver.dao;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.andrew.fileserver.entities.DatabaseFile;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.util.SessionFactoryImpl;

import java.util.List;

@Component
public class DatabaseFileDao {

    private final SessionFactoryImpl sessionFactoryImpl;

    @Autowired
    public DatabaseFileDao(SessionFactoryImpl sessionFactoryImpl) {
        this.sessionFactoryImpl = sessionFactoryImpl;
    }

    public void save(DatabaseFile databaseFile) {
        Session session = sessionFactoryImpl.getSessionFactory().openSession();
        session.beginTransaction();
        session.persist(databaseFile);
        session.getTransaction().commit();
        session.close();
    }

    public List<DatabaseFile> getAllFilesByUser(FileUser fileUser) {
        Session session = sessionFactoryImpl.getSessionFactory().openSession();
        List<DatabaseFile> result = session
                .createQuery("FROM DatabaseFile WHERE fileUser = :fileUser ORDER BY date DESC",
                        DatabaseFile.class)
                .setParameter("fileUser", fileUser)
                .list();
        session.close();
        return result;
    }

    public DatabaseFile getFileById(int fileId) {
        Session session = sessionFactoryImpl.getSessionFactory().openSession();
        DatabaseFile result = session
                .createQuery("FROM DatabaseFile WHERE id = :fileId", DatabaseFile.class)
                .setParameter("fileId", fileId)
                .uniqueResult();
        session.close();
        return result;
    }

    public void deleteFile(DatabaseFile file) {
        Session session = sessionFactoryImpl.getSessionFactory().openSession();
        session.beginTransaction();
        session.remove(file);
        session.getTransaction().commit();
        session.close();
    }
}
