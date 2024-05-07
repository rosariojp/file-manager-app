package com.jeipz.aws.s3.file.manager.exception.handler;

import com.jeipz.aws.s3.file.manager.exception.SystemFileAlreadyExistsException;
import com.jeipz.aws.s3.file.manager.exception.SystemFileNotFoundException;
import com.jeipz.aws.s3.file.manager.exception.response.ExceptionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SystemFileExceptionHandlerTest {

    @InjectMocks
    private SystemFileExceptionHandler systemFileExceptionHandler;

    @Mock
    private SystemFileNotFoundException systemFileNotFoundException;

    @Mock
    private SystemFileAlreadyExistsException systemFileAlreadyExistsException;

    @Test
    void handleSystemFileNotFoundException_Test() {
        ResponseEntity<ExceptionResponse> response =
                systemFileExceptionHandler.handleSystemFileNotFoundException(systemFileNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().status());
        assertEquals("SystemFileNotFoundException", response.getBody().exception());
        assertEquals("System File not found.", response.getBody().error());
    }

    @Test
    void handleSystemFileAlreadyExistsException_Test() {
        ResponseEntity<ExceptionResponse> response =
                systemFileExceptionHandler.handleSystemFileAlreadyExistsException(systemFileAlreadyExistsException);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().status());
        assertEquals("SystemFileAlreadyExistsException", response.getBody().exception());
        assertEquals("System File already exists.", response.getBody().error());
    }
}