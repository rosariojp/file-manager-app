package com.jeipz.aws.s3.file.manager.model.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int currentPage,
        int elementSize,
        int totalPages,
        long totalElements
) {}
