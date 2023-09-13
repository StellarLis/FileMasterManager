package ru.andrew.fileserver.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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
    @ColumnDefault("true")
    private boolean isPrivate;

    @Getter
    @Setter
    private long date;

    public DatabaseFile() {}
    public DatabaseFile(FileUser fileUser, String filename, long date, boolean isPrivate) {
        this.fileUser = fileUser;
        this.filename = filename;
        this.date = date;
        this.isPrivate = isPrivate;
    }
}
