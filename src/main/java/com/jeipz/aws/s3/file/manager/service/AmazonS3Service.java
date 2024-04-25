package com.jeipz.aws.s3.file.manager.service;

import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.IOException;
import java.io.InputStream;

public interface AmazonS3Service {
    void uploadToS3(String bucketName, String filename, InputStream inputStream, ObjectMetadata metadata);
    byte[] downloadFromS3(String bucketName, String filename) throws IOException;
    void deleteFromS3(String bucketName, String fileName);
}
