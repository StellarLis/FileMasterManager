package ru.andrew.fileserver.dao;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import ru.andrew.fileserver.entities.DatabaseFile;
import ru.andrew.fileserver.entities.FileUser;

import java.util.List;

public class DatabaseFileDao {
    @Getter
    private int id;

    @Getter
    @Setter
    private FileUser fileUser;

    @Getter
    @Setter
    private String filename;

    @Getter
    @Setter
    private long date;

    private Session session;

    public DatabaseFileDao(DatabaseFile databaseFile, Session session) {
        this.id = databaseFile.getId();
        this.fileUser = databaseFile.getFileUser();
        this.filename = databaseFile.getFilename();
        this.date = databaseFile.getDate();
        this.session = session;
    }

    public void save() {
        DatabaseFile databaseFile = new DatabaseFile(fileUser, filename, date);
        session.beginTransaction();
        session.persist(databaseFile);
        session.getTransaction().commit();
    }

    public static List<DatabaseFile> getAllFilesByUser(FileUser fileUser, Session session) {
        return session
                .createQuery("FROM DatabaseFile WHERE fileUser = :fileUser ORDER BY date DESC",
                        DatabaseFile.class)
                .setParameter("fileUser", fileUser)
                .list();
    }

    public static DatabaseFile getFileById(int fileId, Session session) {
        return session
                .createQuery("FROM DatabaseFile WHERE id = :fileId", DatabaseFile.class)
                .setParameter("fileId", fileId)
                .uniqueResult();
    }

    public static void deleteFile(DatabaseFile file, Session session) {
        session.beginTransaction();
        session.remove(file);
        session.getTransaction().commit();
    }
}
