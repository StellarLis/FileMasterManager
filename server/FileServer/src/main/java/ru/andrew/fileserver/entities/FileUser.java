package ru.andrew.fileserver.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
    @Size(min = 8, max = 28)
    private String username;

    @Getter
    @Setter
    @Size(min = 6, max = 28)
    private String password;

    @OneToMany(mappedBy = "fileUser")
    private List<DatabaseFile> dbFiles;

    public FileUser() {}
    public FileUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
