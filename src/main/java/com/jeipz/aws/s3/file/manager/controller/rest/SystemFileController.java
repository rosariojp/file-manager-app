package com.jeipz.aws.s3.file.manager.controller.rest;

import com.jeipz.aws.s3.file.manager.model.SystemFile;
import com.jeipz.aws.s3.file.manager.model.response.SystemFileDownloadResponse;
import com.jeipz.aws.s3.file.manager.service.SystemFileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/fma/api")
public class SystemFileController {

    private final SystemFileService systemFileService;

    public SystemFileController(SystemFileService systemFileService) {
        this.systemFileService = systemFileService;
    }

    @GetMapping("/files")
    public ResponseEntity<?> getFiles(@RequestParam("page") int page,
                                      @RequestParam("size") int size) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(systemFileService.getFiles(page, size));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestParam("id") UUID id) {
        systemFileService.delete(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/upload")
    public ResponseEntity<SystemFile> fileUpload(
            @RequestPart("description") String description,
            @RequestPart("file") MultipartFile file) throws IOException {
        SystemFile systemFile = systemFileService.upload(description, file);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(systemFile);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> fileDownload(@RequestParam("id") UUID id) throws IOException {
        SystemFileDownloadResponse downloadResponse = systemFileService.download(id);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentLength(downloadResponse.fileData().length);
        httpHeaders.setContentDispositionFormData("attachment", downloadResponse.fileName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(httpHeaders)
                .body(downloadResponse.fileData());
    }
}
