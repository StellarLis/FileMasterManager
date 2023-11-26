package ru.andrew.fileserver.dto;

import ru.andrew.fileserver.entities.DatabaseFile;

public class FileResponseWithOneFile extends FileResponse {
    public DatabaseFile file;

    public FileResponseWithOneFile(String message, DatabaseFile file) {
        super(message);
        this.file = file;
    }
}
