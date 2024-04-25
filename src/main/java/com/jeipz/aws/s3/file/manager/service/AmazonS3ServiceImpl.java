package com.jeipz.aws.s3.file.manager.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class AmazonS3ServiceImpl implements AmazonS3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Override
    public void uploadToS3(String bucketName, String fileName,
                           InputStream inputStream, ObjectMetadata metadata) {
        amazonS3.putObject(bucketName, fileName, inputStream, metadata);
    }

    @Override
    public byte[] downloadFromS3(String bucketName, String fileName) throws IOException {
        S3Object s3Object = amazonS3.getObject(bucketName, fileName);
        return s3Object.getObjectContent().readAllBytes();
    }

    @Override
    public void deleteFromS3(String bucketName, String fileName) {
        amazonS3.deleteObject(bucketName, fileName);
    }

}
