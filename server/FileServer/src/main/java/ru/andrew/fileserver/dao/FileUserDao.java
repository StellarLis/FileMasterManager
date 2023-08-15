package ru.andrew.fileserver.dao;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import ru.andrew.fileserver.entities.FileUser;

public class FileUserDao {
    @Getter
    private int id;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    private Session session;

    public FileUserDao(FileUser fileUser, Session session) {
        this.id = fileUser.getId();
        this.username = fileUser.getUsername();
        this.password = fileUser.getPassword();
        this.session = session;
    }

    public FileUser getCandidate() {
        return session
                .createQuery("FROM FileUser WHERE username = :username", FileUser.class)
                .setParameter("username", username)
                .uniqueResult();
    }
    public static FileUser getCandidate(String providedUsername, Session session) {
        return session
                .createQuery("FROM FileUser WHERE username = :username", FileUser.class)
                .setParameter("username", providedUsername)
                .uniqueResult();
    }

    public void save() {
        FileUser fileUser = new FileUser(username, password);
        session.beginTransaction();
        session.persist(fileUser);
        session.getTransaction().commit();
    }
}
