package com.jeipz.aws.s3.file.manager.validation;

import com.jeipz.aws.s3.file.manager.exception.SystemFileAlreadyExistsException;
import com.jeipz.aws.s3.file.manager.model.SystemFile;
import com.jeipz.aws.s3.file.manager.repository.SystemFileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemFileValidatorTest {

    private static final String FILE_NAME = "test-file.txt";

    @InjectMocks
    private SystemFileValidator systemFileValidator;

    @Mock
    private SystemFileRepository systemFileRepository;

    @Test
    public void validateSystemFileName_Successful() {
        systemFileValidator.validateSystemFileName(FILE_NAME);
        verify(systemFileRepository, times(1)).findByFileName(FILE_NAME);
    }

    @Test
    public void validateSystemFileName_SystemFileAlreadyExistsException() {
        when(systemFileRepository.findByFileName(anyString()))
                .thenReturn(Optional.of(SystemFile.builder()
                        .fileName(FILE_NAME)
                        .build()));

        assertThrows(SystemFileAlreadyExistsException.class,
                () -> systemFileValidator.validateSystemFileName(FILE_NAME));

        verify(systemFileRepository, times(1)).findByFileName(FILE_NAME);
    }
}