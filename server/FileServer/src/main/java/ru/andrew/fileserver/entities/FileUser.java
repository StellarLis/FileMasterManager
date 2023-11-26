package ru.andrew.fileserver.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 8, max = 28)
    private String username;

    @Size(min = 6)
    private String password;

    @OneToMany(mappedBy = "fileUser")
    private List<DatabaseFile> dbFiles;
}
