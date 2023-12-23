package ru.andrew.fileserver.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.andrew.fileserver.dto.FileResponse;
import ru.andrew.fileserver.dto.FileResponseWithFiles;
import ru.andrew.fileserver.service.FileService;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FileController {
    private final FileService fileService;

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(
            @RequestParam MultipartFile multipartFile
    ) {
        return fileService.upload(multipartFile);
    }

    @GetMapping(value = "/getFiles", produces = "application/json")
    public ResponseEntity<FileResponseWithFiles> getFilesByUsername() {
        return fileService.getFilesByUsername();
    }

    @GetMapping(value = "/getFileById/{fileId}", produces = "application/json")
    public ResponseEntity<FileResponse> getFileById(@PathVariable int fileId) {
        return fileService.getFileById(fileId);
    }

    @DeleteMapping(value = "/deleteFileById/{fileId}", produces = "application/json")
    public ResponseEntity<FileResponse> deleteFileById(@PathVariable int fileId) {
        return fileService.deleteFileById(fileId);
    }

    @GetMapping(value = "/download/{fileId}")
    public ResponseEntity<String> download(
            @PathVariable int fileId,
            HttpServletResponse response
    ) {
        return fileService.download(fileId, response);
    }

    @PutMapping(value = "/changePrivacy/{fileId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<FileResponse> changePrivacy(
            @PathVariable int fileId,
            @RequestParam boolean newPrivacyOption
    ) {
        return fileService.changePrivacy(fileId, newPrivacyOption);
    }

    @GetMapping(value = "/search/{textInput}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<FileResponseWithFiles> search(@PathVariable String textInput) {
        return fileService.search(textInput);
    }
}
