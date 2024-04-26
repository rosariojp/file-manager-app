package com.jeipz.aws.s3.file.manager.exception.response;

import java.time.LocalDateTime;

public record ExceptionResponse (
        LocalDateTime timestamp,
        int status,
        String exception,
        String error
) {}
