package com.jeipz.aws.s3.file.manager.validation;

import com.jeipz.aws.s3.file.manager.exception.SystemFileAlreadyExistsException;
import com.jeipz.aws.s3.file.manager.repository.SystemFileRepository;
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
                        throw new SystemFileAlreadyExistsException();
                });
    }
}
