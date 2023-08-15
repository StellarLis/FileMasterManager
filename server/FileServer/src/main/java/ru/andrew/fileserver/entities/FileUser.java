package ru.andrew.fileserver.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
public class FileUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private int id;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    @OneToMany(mappedBy = "fileUser")
    private List<DatabaseFile> dbFiles;

    public FileUser() {}
    public FileUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
