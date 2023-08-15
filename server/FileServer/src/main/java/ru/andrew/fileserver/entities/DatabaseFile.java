package ru.andrew.fileserver.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
public class DatabaseFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private int id;

    @ManyToOne
    @JoinColumn(name = "fileUser", nullable = false)
    @Getter
    @Setter
    private FileUser fileUser;

    @Getter
    @Setter
    private String filename;

    @Getter
    @Setter
    private long date;

    public DatabaseFile() {}
    public DatabaseFile(FileUser fileUser, String filename, long date) {
        this.fileUser = fileUser;
        this.filename = filename;
        this.date = date;
    }
}
