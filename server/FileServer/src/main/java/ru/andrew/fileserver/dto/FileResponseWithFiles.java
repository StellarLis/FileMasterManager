package ru.andrew.fileserver.dto;

import lombok.AllArgsConstructor;
import ru.andrew.fileserver.entities.DatabaseFile;

import java.util.List;

@AllArgsConstructor
public class FileResponseWithFiles {
    public List<DatabaseFile> filesList;
}
