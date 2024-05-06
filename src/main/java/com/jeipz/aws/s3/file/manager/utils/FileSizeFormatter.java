package com.jeipz.aws.s3.file.manager.utils;

import java.text.DecimalFormat;

public class FileSizeFormatter {

    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#")
                .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
