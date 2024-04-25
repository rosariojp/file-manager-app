package com.jeipz.aws.s3.file.manager.model.response;

public record SystemFileDownloadResponse(
        String fileName,
        byte[] fileData
){}
