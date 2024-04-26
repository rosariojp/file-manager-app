package com.jeipz.aws.s3.file.manager.exception.handler;

import com.jeipz.aws.s3.file.manager.exception.FileRequiredException;
import com.jeipz.aws.s3.file.manager.exception.response.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;


@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FileRequiredException.class)
    public ResponseEntity<ExceptionResponse> handleFileRequiredException(FileRequiredException e) {
        logger.error("File is required.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        e.getClass().getSimpleName(),
                        "File is required.")
                );
    }
}
