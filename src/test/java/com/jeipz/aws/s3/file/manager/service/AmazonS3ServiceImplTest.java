package com.jeipz.aws.s3.file.manager.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmazonS3ServiceImplTest {

    private static final String BUCKET_NAME = "Test Bucket";

    private static final String FILE_NAME = "test-file.txt";

    @InjectMocks
    private AmazonS3ServiceImpl amazonS3Service;

    @Mock
    private AmazonS3 amazonS3;

    @Test
    public void uploadToS3_Successful() {
        byte[] fileBytes = FILE_NAME.getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileBytes);
        ObjectMetadata metadata = new ObjectMetadata();

        amazonS3Service.uploadToS3(BUCKET_NAME, FILE_NAME, inputStream, metadata);

        verify(amazonS3, times(1))
                .putObject(BUCKET_NAME, FILE_NAME, inputStream, metadata);
    }

    @Test
    public void uploadToS3_AmazonServiceException() {
        byte[] fileBytes = FILE_NAME.getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileBytes);
        ObjectMetadata metadata = new ObjectMetadata();

        doThrow(AmazonServiceException.class)
                .when(amazonS3)
                .putObject(BUCKET_NAME, FILE_NAME, inputStream, metadata);

        assertThrows(AmazonServiceException.class,
                () -> amazonS3Service.uploadToS3(BUCKET_NAME, FILE_NAME, inputStream, metadata));

        verify(amazonS3, times(1))
                .putObject(BUCKET_NAME, FILE_NAME, inputStream, metadata);
    }

    @Test
    public void downloadFromS3_Successful() throws IOException {
        S3Object s3Object = mock(S3Object.class);
        byte[] testBytes = FILE_NAME.getBytes();
        InputStream inputStream = new ByteArrayInputStream(testBytes);
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(inputStream, null);

        when(amazonS3.getObject(BUCKET_NAME, FILE_NAME)).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);

        byte[] fileBytes = amazonS3Service.downloadFromS3(BUCKET_NAME, FILE_NAME);

        Assertions.assertNotNull(fileBytes);

        verify(amazonS3, times(1)).getObject(BUCKET_NAME, FILE_NAME);
        verify(s3Object, times(1)).getObjectContent();
    }

    @Test
    public void downloadFromS3_AmazonServiceException() {
        doThrow(AmazonServiceException.class)
                .when(amazonS3)
                .getObject(BUCKET_NAME, FILE_NAME);

        assertThrows(AmazonServiceException.class, () ->
                amazonS3Service.downloadFromS3(BUCKET_NAME, FILE_NAME));

        verify(amazonS3, times(1))
                .getObject(BUCKET_NAME, FILE_NAME);
    }

    @Test
    public void deleteFromS3_Successful() {
        amazonS3Service.deleteFromS3(BUCKET_NAME, FILE_NAME);

        verify(amazonS3, times(1))
                .deleteObject(BUCKET_NAME, FILE_NAME);
    }

    @Test
    public void deleteFromS3_AmazonServiceException() {
        doThrow(AmazonServiceException.class)
                .when(amazonS3)
                .deleteObject(BUCKET_NAME, FILE_NAME);

        assertThrows(AmazonServiceException.class,
                () -> amazonS3Service.deleteFromS3(BUCKET_NAME, FILE_NAME));

        verify(amazonS3, times(1))
                .deleteObject(BUCKET_NAME, FILE_NAME);
    }
}