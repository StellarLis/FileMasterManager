package ru.andrew.fileserver.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

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

    public FileUser() {}
    public FileUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
