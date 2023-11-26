package ru.andrew.fileserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.andrew.fileserver.entities.FileUser;

public interface FileUserRepository extends JpaRepository<FileUser, Long> {
    FileUser findUserByUsername(String username);
}
