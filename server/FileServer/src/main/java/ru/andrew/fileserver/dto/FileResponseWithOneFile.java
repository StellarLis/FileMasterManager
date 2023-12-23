package ru.andrew.fileserver.dto;

import lombok.Getter;
import ru.andrew.fileserver.entities.DatabaseFile;

@Getter
public class FileResponseWithOneFile extends FileResponse {
    public DatabaseFile file;

    public FileResponseWithOneFile(String message, DatabaseFile file) {
        super(message);
        this.file = file;
    }
}
