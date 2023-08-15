package ru.andrew.fileserver.dao;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import ru.andrew.fileserver.entities.DatabaseFile;
import ru.andrew.fileserver.entities.FileUser;

public class DatabaseFileDao {
    @Getter
    private int id;

    @Getter
    @Setter
    private FileUser fileUser;

    @Getter
    @Setter
    private String filename;

    private Session session;

    public DatabaseFileDao(DatabaseFile databaseFile, Session session) {
        this.id = databaseFile.getId();
        this.fileUser = databaseFile.getFileUser();
        this.filename = databaseFile.getFilename();
        this.session = session;
    }

    public void save() {
        DatabaseFile databaseFile = new DatabaseFile(fileUser, filename);
        session.beginTransaction();
        session.persist(databaseFile);
        session.getTransaction().commit();
    }
}
