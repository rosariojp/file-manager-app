package com.jeipz.aws.s3.file.manager.validation;

import com.jeipz.aws.s3.file.manager.repository.SystemFileRepository;
import jakarta.persistence.EntityExistsException;
import org.springframework.stereotype.Component;

@Component
public class SystemFileValidator {

    private final SystemFileRepository systemFileRepository;

    public SystemFileValidator(SystemFileRepository systemFileRepository) {
        this.systemFileRepository = systemFileRepository;
    }

    public void validateSystemFileName(String fileName) {
        systemFileRepository.findByFileName(fileName)
                .ifPresent(systemFile -> {
                        String message = String.format("File '%s' already exists.", systemFile.getFileName());
                        throw new EntityExistsException(message);
                });
    }
}
