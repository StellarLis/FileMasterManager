package ru.andrew.fileserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.andrew.fileserver.entities.DatabaseFile;

import java.util.List;

public interface DatabaseFileRepository extends JpaRepository<DatabaseFile, Long> {
    @Query(
            value = "SELECT * FROM DatabaseFile db WHERE db.filename LIKE ?1 AND isPrivate" +
                    " = false ORDER BY date DESC",
            nativeQuery = true
    )
    List<DatabaseFile> searchForFilesByName(String name);
}
