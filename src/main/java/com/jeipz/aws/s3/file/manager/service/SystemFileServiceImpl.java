package com.jeipz.aws.s3.file.manager.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jeipz.aws.s3.file.manager.model.SystemFile;
import com.jeipz.aws.s3.file.manager.model.response.PageResponse;
import com.jeipz.aws.s3.file.manager.model.response.SystemFileDownloadResponse;
import com.jeipz.aws.s3.file.manager.repository.SystemFileRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SystemFileServiceImpl implements SystemFileService {

    private final SystemFileRepository systemFileRepository;

    private final AmazonS3Service amazonS3Service;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public SystemFileServiceImpl(AmazonS3Service amazonS3Service,
                                 SystemFileRepository systemFileRepository) {
        this.systemFileRepository = systemFileRepository;
        this.amazonS3Service = amazonS3Service;
    }

    @Override
    public PageResponse<SystemFile> getFiles(int page, int size) {
        String field = "uploadDate";
        Sort sort = Sort.by(Sort.Order.asc(field));
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SystemFile> pages = systemFileRepository.findAll(pageable);
        return new PageResponse<>(
                pages.getContent(),
                pages.getNumber() + 1,
                pages.getSize(),
                pages.getTotalPages(),
                pages.getTotalElements()
        );
    }

    @Transactional
    @Override
    public void delete(UUID id) throws SdkClientException {
        SystemFile systemFile = systemFileRepository.findById(id)
                        .orElseThrow(EntityNotFoundException::new);
        systemFileRepository.delete(systemFile);
        amazonS3Service.deleteFromS3(systemFile.getBucketName(), systemFile.getFileName());
    }

    @Transactional
    @Override
    public SystemFile upload(String description, MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file...");
        }

        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(fileSize);

        SystemFile systemFile = SystemFile.builder()
                .description(description)
                .bucketName(bucketName)
                .fileName(fileName)
                .fileSize(fileSize)
                .contentType(contentType)
                .uploadDate(LocalDateTime.now())
                .build();

        SystemFile savedSystemFile = systemFileRepository.save(systemFile);
        amazonS3Service.uploadToS3(savedSystemFile.getBucketName(), savedSystemFile.getFileName(),
                file.getInputStream(), metadata);
        return savedSystemFile;
    }

    @Override
    public SystemFileDownloadResponse download(UUID id) throws IOException {
        SystemFile systemFile = systemFileRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        byte[] fileData = amazonS3Service.downloadFromS3(systemFile.getBucketName(), systemFile.getFileName());
        return new SystemFileDownloadResponse(systemFile.getFileName(), fileData);
    }
}
