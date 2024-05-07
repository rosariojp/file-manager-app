package com.jeipz.aws.s3.file.manager.exception.handler;

import com.jeipz.aws.s3.file.manager.exception.FileRequiredException;
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
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private FileRequiredException fileRequiredException;

    @Test
    void handleFileRequiredException_Test() {
        ResponseEntity<ExceptionResponse> response =
                globalExceptionHandler.handleFileRequiredException(fileRequiredException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().status());
        assertEquals("FileRequiredException", response.getBody().exception());
        assertEquals("File is required.", response.getBody().error());
    }
}