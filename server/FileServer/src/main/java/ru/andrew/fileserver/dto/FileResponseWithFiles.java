package ru.andrew.fileserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.andrew.fileserver.entities.DatabaseFile;

import java.util.List;

@AllArgsConstructor
@Getter
public class FileResponseWithFiles {
    public List<DatabaseFile> filesList;
}
