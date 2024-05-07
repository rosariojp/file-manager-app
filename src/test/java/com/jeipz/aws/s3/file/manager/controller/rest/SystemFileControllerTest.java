package com.jeipz.aws.s3.file.manager.controller.rest;

import com.jeipz.aws.s3.file.manager.model.SystemFile;
import com.jeipz.aws.s3.file.manager.model.response.PageResponse;
import com.jeipz.aws.s3.file.manager.model.response.SystemFileDownloadResponse;
import com.jeipz.aws.s3.file.manager.service.SystemFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SystemFileControllerTest {

    private static final int PAGE = 0;

    private static final int SIZE = 5;

    private static final String API_REQUEST_MAPPING = "/fma/api";

    private static final String FILE_NAME = "test.txt";

    private static final String BUCKET_NAME = "Test Bucket";

    private static final String DESCRIPTION = "Test Description";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemFileService systemFileService;

    private List<SystemFile> generateSystemFiles() {
        return IntStream.range(0, SIZE)
                .mapToObj(i -> SystemFile.builder()
                        .id(UUID.randomUUID())
                        .fileName(String.format("test-file-%s.txt", i))
                        .bucketName("Test Bucket")
                        .uploadDate(LocalDateTime.now())
                        .build())
                .toList();
    }

    @Test
    public void getFiles_Successful() throws Exception {
        List<SystemFile> systemFiles = generateSystemFiles();
        Page<SystemFile> pages = new PageImpl<>(systemFiles);
        PageResponse<SystemFile> pageResponse = new PageResponse<>(
            pages.getContent(),
            pages.getNumber() + 1,
            pages.getSize(),
            pages.getTotalPages(),
            pages.getTotalElements()
        );

        when(systemFileService.getFiles(PAGE, SIZE))
                .thenReturn(pageResponse);

        mockMvc.perform(get(String.format("%s/files", API_REQUEST_MAPPING))
                .param("page", String.valueOf(PAGE))
                .param("size", String.valueOf(SIZE)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(pageResponse.content().size()))
                .andExpect(jsonPath("$.currentPage").value(pageResponse.currentPage()))
                .andExpect(jsonPath("$.elementSize").value(pageResponse.elementSize()))
                .andExpect(jsonPath("$.totalPages").value(pageResponse.totalPages()))
                .andExpect(jsonPath("$.totalElements").value(pageResponse.totalElements()));

    }

    @Test
    public void deleteFile_Successful() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(systemFileService)
                .delete(any(UUID.class));

        mockMvc.perform(delete(String.format("%s/delete", API_REQUEST_MAPPING))
                .param("id", String.valueOf(id)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void fileUpload() throws Exception {
        MockPart description = new MockPart("description", DESCRIPTION.getBytes(StandardCharsets.UTF_8));
        MockPart file = new MockPart("file", FILE_NAME, FILE_NAME.getBytes());
        file.getHeaders().setContentType(MediaType.TEXT_PLAIN);

        SystemFile systemFile = SystemFile.builder()
                .id(UUID.randomUUID())
                .description(DESCRIPTION)
                .bucketName(BUCKET_NAME)
                .fileName(FILE_NAME)
                .uploadDate(LocalDateTime.now())
                .build();

        when(systemFileService.upload(eq(DESCRIPTION), any(MultipartFile.class)))
                .thenReturn(systemFile);

        mockMvc.perform(multipart(String.format("%s/upload", API_REQUEST_MAPPING))
                        .part(description)
                        .part(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fileName").value(systemFile.getFileName()))
                .andExpect(jsonPath("$.bucketName").value(systemFile.getBucketName()))
                .andExpect(jsonPath("$.description").value(systemFile.getDescription()));
    }

    @Test
    public void fileDownload() throws Exception {
        UUID id = UUID.randomUUID();
        SystemFileDownloadResponse systemFileDownloadResponse =
                new SystemFileDownloadResponse(FILE_NAME, FILE_NAME.getBytes());

        when(systemFileService.download(any(UUID.class)))
                .thenReturn(systemFileDownloadResponse);

        mockMvc.perform(get(String.format("%s/download", API_REQUEST_MAPPING))
                .param("id", String.valueOf(id)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }
}