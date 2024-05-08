package com.jeipz.aws.s3.file.manager.controller;

import com.jeipz.aws.s3.file.manager.model.SystemFile;
import com.jeipz.aws.s3.file.manager.model.response.PageResponse;
import com.jeipz.aws.s3.file.manager.model.response.SystemFileDownloadResponse;
import com.jeipz.aws.s3.file.manager.service.SystemFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileManagerApplication.class)
class FileManagerApplicationTest {

    private static final String REQUEST_MAPPING = "/fma";

    private static final int PAGE = 0;

    private static final int SIZE = 5;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemFileService systemFileService;

    private List<SystemFile> generateSystemFiles() {
        return IntStream.range(0, SIZE)
                .mapToObj(i -> SystemFile.builder()
                        .id(UUID.randomUUID())
                        .fileName(String.format("test-file-%s.txt", i))
                        .fileSize((long) (new Random().nextInt(1024) + 1))
                        .bucketName("Test Bucket")
                        .uploadDate(LocalDateTime.now())
                        .build())
                .toList();
    }

    @Test
    public void dashboard_Successful() throws Exception {
        List<SystemFile> systemFiles = generateSystemFiles();
        Page<SystemFile> pages = new PageImpl<>(systemFiles);
        PageResponse<SystemFile> pageResponse = new PageResponse<>(
                pages.getContent(),
                pages.getNumber() + 1,
                pages.getSize(),
                pages.getTotalPages(),
                pages.getNumberOfElements()
        );

        when(systemFileService.getFiles(PAGE, SIZE))
                .thenReturn(pageResponse);

        mockMvc.perform(get(String.format("%s/dashboard", REQUEST_MAPPING))
                .param("page", String.valueOf(0))
                .param("size", String.valueOf(5)))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("pageResponse"))
                .andExpect(model().attribute("pageResponse", pageResponse));

        verify(systemFileService, times(1)).getFiles(PAGE, SIZE);
    }

    @Test
    public void upload_Successful() throws Exception {
        MockPart description = new MockPart("description", "test-description".getBytes());
        MockPart file = new MockPart("file", "test-file.txt", "test-file.txt".getBytes());
        file.getHeaders().setContentType(MediaType.TEXT_PLAIN);

        mockMvc.perform(multipart(String.format("%s/upload", REQUEST_MAPPING))
                .part(description)
                .part(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(String.format("%s/dashboard", REQUEST_MAPPING)));

        verify(systemFileService, times(1))
                .upload(anyString(), any(MultipartFile.class));
    }

    @Test
    public void download_Successful() throws Exception {
        UUID id = UUID.randomUUID();
        SystemFileDownloadResponse systemFileDownloadResponse =
                new SystemFileDownloadResponse("test-file.txt", new byte[0]);

        when(systemFileService.download(id))
                .thenReturn(systemFileDownloadResponse);

        mockMvc.perform(get(String.format("%s/download/{id}", REQUEST_MAPPING), id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(systemFileDownloadResponse.fileData()));

        verify(systemFileService, times(1)).download(id);
    }

    @Test
    public void deleteFile_Successful() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(systemFileService)
                .delete(id);

        mockMvc.perform(delete(String.format("%s/delete/{id}", REQUEST_MAPPING), id))
                .andExpect(status().isNoContent());

        verify(systemFileService, times(1)).delete(id);
    }
}