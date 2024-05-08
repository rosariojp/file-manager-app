package com.jeipz.aws.s3.file.manager.controller;

import com.jeipz.aws.s3.file.manager.model.SystemFile;
import com.jeipz.aws.s3.file.manager.model.response.PageResponse;
import com.jeipz.aws.s3.file.manager.model.response.SystemFileDownloadResponse;
import com.jeipz.aws.s3.file.manager.service.SystemFileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/fma")
public class FileManagerApplication {

    private final SystemFileService systemFileService;

    public FileManagerApplication(SystemFileService systemFileService) {
        this.systemFileService = systemFileService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "5") int size) {
        PageResponse<SystemFile> pageResponse = systemFileService.getFiles(page, size);
        model.addAttribute("pageResponse", pageResponse);
        return "dashboard";
    }

    @PostMapping("upload")
    public String upload(@RequestPart(value = "description", required = false) String description,
                         @RequestPart("file") MultipartFile file) throws IOException {
        systemFileService.upload(description, file);
        return "redirect:/fma/dashboard";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) throws IOException {

        SystemFileDownloadResponse downloadResponse = systemFileService.download(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(downloadResponse.fileData().length);
        headers.setContentDispositionFormData("attachment", downloadResponse.fileName());

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(downloadResponse.fileData());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable("id") UUID id) {
        systemFileService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
