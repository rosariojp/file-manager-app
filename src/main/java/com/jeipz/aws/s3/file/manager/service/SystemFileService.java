package com.jeipz.aws.s3.file.manager.service;

import com.jeipz.aws.s3.file.manager.model.SystemFile;
import com.jeipz.aws.s3.file.manager.model.response.PageResponse;
import com.jeipz.aws.s3.file.manager.model.response.SystemFileDownloadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface SystemFileService {
    PageResponse<SystemFile> getFiles(int page, int size);
    void delete(UUID id);
    SystemFile upload(String description, MultipartFile file) throws IOException;
    SystemFileDownloadResponse download(UUID id) throws IOException;
}
