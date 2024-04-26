package com.jeipz.aws.s3.file.manager.exception.handler;

import com.jeipz.aws.s3.file.manager.exception.SystemFileAlreadyExistsException;
import com.jeipz.aws.s3.file.manager.exception.SystemFileNotFoundException;
import com.jeipz.aws.s3.file.manager.exception.response.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;


@ControllerAdvice
public class SystemFileExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SystemFileExceptionHandler.class);

    @ExceptionHandler(SystemFileNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleSystemFileNotFoundException(SystemFileNotFoundException e) {
        logger.error("System file not found.");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        e.getClass().getSimpleName(),
                        "System File not found.")
                );
    }

    @ExceptionHandler(SystemFileAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleSystemFileAlreadyExistsException(SystemFileAlreadyExistsException e) {
        logger.error("System file already exists.");
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ExceptionResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                e.getClass().getSimpleName(),
                                "System File already exists.")
                );
    }
}
