package com.jeipz.aws.s3.file.manager.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileSizeFormatterTest {

    @Test
    public void formatFileSize_EmptyFileSize() {
        long size = 0;
        assertEquals("0 B", FileSizeFormatter.formatFileSize(size));
    }

    @Test
    public void formatFileSize_NotEmptyFileSize() {
        long size = 1024;
        assertEquals("1 KB", FileSizeFormatter.formatFileSize(size));
    }
}