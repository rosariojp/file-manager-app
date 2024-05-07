package com.jeipz.aws.s3.file.manager.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jeipz.aws.s3.file.manager.exception.FileRequiredException;
import com.jeipz.aws.s3.file.manager.exception.SystemFileNotFoundException;
import com.jeipz.aws.s3.file.manager.model.SystemFile;
import com.jeipz.aws.s3.file.manager.model.response.PageResponse;
import com.jeipz.aws.s3.file.manager.model.response.SystemFileDownloadResponse;
import com.jeipz.aws.s3.file.manager.repository.SystemFileRepository;
import com.jeipz.aws.s3.file.manager.validation.SystemFileValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemFileServiceImplTest {

    private static final String BUCKET_NAME = "Test Bucket";

    private static final String FILE_NAME = "test-file.txt";

    private static final String DESCRIPTION = "Test Description";

    private static final int PAGE = 0;

    private static final int SIZE = 5;

    private static final Sort SORT = Sort.by(Sort.Order.desc("uploadDate"));

    @InjectMocks
    private SystemFileServiceImpl systemFileService;

    @Mock
    private SystemFileRepository systemFileRepository;

    @Mock
    private AmazonS3Service amazonS3Service;

    @Mock
    private SystemFileValidator systemFileValidator;

    @Test
    public void getFiles_Successful() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, SORT);
        List<SystemFile> systemFiles = List.of(
                SystemFile.builder()
                        .id(UUID.randomUUID())
                        .bucketName(BUCKET_NAME)
                        .fileName("text-file-1.txt")
                        .uploadDate(LocalDateTime.now())
                        .build(),
                SystemFile.builder()
                        .id(UUID.randomUUID())
                        .bucketName(BUCKET_NAME)
                        .fileName("test-file-2.txt")
                        .uploadDate(LocalDateTime.now())
                        .build()
        );
        Page<SystemFile> systemFilePages = new PageImpl<>(systemFiles);

        when(systemFileRepository.findAll(pageable)).thenReturn(systemFilePages);

        PageResponse<SystemFile> systemFilePageResponses = systemFileService.getFiles(PAGE, SIZE);

        assertEquals(systemFilePageResponses.content(), systemFiles);

        verify(systemFileRepository, times(1)).findAll(pageable);
    }

    @Test
    public void delete_Successful() {
        UUID id = UUID.randomUUID();
        SystemFile systemFile = SystemFile.builder()
                        .id(id)
                        .bucketName(BUCKET_NAME)
                        .fileName(FILE_NAME)
                        .uploadDate(LocalDateTime.now())
                        .build();

        when(systemFileRepository.findById(id)).thenReturn(Optional.of(systemFile));
        doNothing().when(systemFileRepository).delete(systemFile);
        doNothing().when(amazonS3Service).deleteFromS3(BUCKET_NAME, FILE_NAME);

        systemFileService.delete(id);

        verify(systemFileRepository, times(1)).findById(id);
        verify(systemFileRepository, times(1)).delete(systemFile);
        verify(amazonS3Service, times(1)).deleteFromS3(BUCKET_NAME, FILE_NAME);
    }

    @Test
    public void delete_SystemFileNotFoundException() {
        UUID id = UUID.randomUUID();

        doThrow(SystemFileNotFoundException.class)
                .when(systemFileRepository)
                .findById(id);

        assertThrows(SystemFileNotFoundException.class,
                () -> systemFileService.delete(id));

        verify(systemFileRepository, times(1))
                .findById(id);
    }

    @Test
    public void upload_Successful() throws IOException {
        byte[] fileData = FILE_NAME.getBytes();
        MultipartFile file = new MockMultipartFile(FILE_NAME, FILE_NAME, "text/plain", fileData);

        SystemFile systemFile = SystemFile.builder()
                .id(UUID.randomUUID())
                .description(DESCRIPTION)
                .bucketName(BUCKET_NAME)
                .fileName(file.getOriginalFilename())
                .uploadDate(LocalDateTime.now())
                .build();

        systemFile.setFileSize(file.getSize());
        systemFile.setContentType(file.getContentType());

        doNothing().when(systemFileValidator).validateSystemFileName(file.getOriginalFilename());
        when(systemFileRepository.save(any(SystemFile.class))).thenReturn(systemFile);
        doNothing()
                .when(amazonS3Service)
                .uploadToS3(anyString(), anyString(),
                        any(InputStream.class), any(ObjectMetadata.class));

        SystemFile uploadedSystemFile = systemFileService.upload(DESCRIPTION, file);

        assertAll("Validate saved system file details",
                () -> assertEquals(systemFile.getId(), uploadedSystemFile.getId()),
                () -> assertEquals(systemFile.getBucketName(), uploadedSystemFile.getBucketName()),
                () -> assertEquals(systemFile.getFileName(), uploadedSystemFile.getFileName()),
                () -> assertEquals(systemFile.getUploadDate(), uploadedSystemFile.getUploadDate()),
                () -> assertEquals(systemFile.getDescription(), uploadedSystemFile.getDescription()),
                () -> assertEquals(systemFile.getFileSize(), uploadedSystemFile.getFileSize()),
                () -> assertEquals(systemFile.getContentType(), uploadedSystemFile.getContentType()));

        verify(systemFileValidator, times(1)).validateSystemFileName(file.getOriginalFilename());
        verify(systemFileRepository, times(1)).save(any(SystemFile.class));
        verify(amazonS3Service, times(1))
                .uploadToS3(eq(systemFile.getBucketName()), eq(systemFile.getFileName()),
                        any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    public void upload_FileRequiredException() {
        MultipartFile file = new MockMultipartFile(FILE_NAME, new byte[0]);
        assertThrows(FileRequiredException.class, () -> systemFileService.upload(DESCRIPTION, file));
    }

    @Test
    public void upload_AmazonServiceException() {
        byte[] fileData = FILE_NAME.getBytes();
        MultipartFile file = new MockMultipartFile(FILE_NAME, FILE_NAME, "text/plain", fileData);

        SystemFile systemFile = SystemFile.builder()
                .id(UUID.randomUUID())
                .description(DESCRIPTION)
                .bucketName(BUCKET_NAME)
                .fileName(file.getOriginalFilename())
                .uploadDate(LocalDateTime.now())
                .build();

        doNothing().when(systemFileValidator).validateSystemFileName(file.getOriginalFilename());
        when(systemFileRepository.save(any(SystemFile.class))).thenReturn(systemFile);
        doThrow(AmazonServiceException.class)
                .when(amazonS3Service)
                .uploadToS3(anyString(), anyString(),
                        any(InputStream.class), any(ObjectMetadata.class));

        assertThrows(AmazonServiceException.class, () -> systemFileService.upload(DESCRIPTION, file));

        verify(systemFileValidator, times(1)).validateSystemFileName(file.getOriginalFilename());
        verify(systemFileRepository, times(1)).save(any(SystemFile.class));
        verify(amazonS3Service, times(1))
                .uploadToS3(eq(systemFile.getBucketName()),
                        eq(systemFile.getFileName()),
                        any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    public void download_Successful() throws IOException {
        UUID id = UUID.randomUUID();
        SystemFile systemFile = SystemFile.builder()
                .id(id)
                .bucketName(BUCKET_NAME)
                .fileName(FILE_NAME)
                .uploadDate(LocalDateTime.now())
                .build();

        when(systemFileRepository.findById(id)).thenReturn(Optional.of(systemFile));
        when(amazonS3Service.downloadFromS3(systemFile.getBucketName(), systemFile.getFileName())).thenReturn(any(byte[].class));

        SystemFileDownloadResponse systemFileDownloadResponse = systemFileService.download(id);

        assertNotNull(systemFileDownloadResponse);

        verify(systemFileRepository, times(1)).findById(id);
        verify(amazonS3Service, times(1)).downloadFromS3(systemFile.getBucketName(), systemFile.getFileName());
    }

    @Test
    public void download_SystemFileNotFoundException() throws IOException {
        UUID id = UUID.randomUUID();

        doThrow(SystemFileNotFoundException.class)
                .when(systemFileRepository)
                .findById(id);

        assertThrows(SystemFileNotFoundException.class, () -> systemFileService.download(id));

        verify(systemFileRepository, times(1)).findById(id);
    }

    @Test
    public void download_AmazonServiceException() throws IOException {
        UUID id = UUID.randomUUID();
        SystemFile systemFile = SystemFile.builder()
                .id(id)
                .bucketName(BUCKET_NAME)
                .fileName(FILE_NAME)
                .uploadDate(LocalDateTime.now())
                .build();

        when(systemFileRepository.findById(id)).thenReturn(Optional.of(systemFile));
        doThrow(AmazonServiceException.class)
                .when(amazonS3Service)
                .downloadFromS3(systemFile.getBucketName(), systemFile.getFileName());

        assertThrows(AmazonServiceException.class, () -> systemFileService.download(id));

        verify(systemFileRepository, times(1)).findById(id);
        verify(amazonS3Service, times(1)).downloadFromS3(systemFile.getBucketName(), systemFile.getFileName());
    }
}