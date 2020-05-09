package com.threadcat.latency.utils;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileUtils {
    public static final String WATCH_DIR_A = "watch_dir_a";
    public static final String WATCH_DIR_B = "watch_dir_b";
    public static final String FILE_A = "watch_file_a.dat";
    public static final String FILE_B = "watch_file_b.dat";

    public static FileChannel openFile(String tmpDir, String subDir, String fileName) throws IOException {
        File file = createFile(tmpDir, subDir, fileName);
        return FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
    }

    private static File createFile(String tmpDir, String subDir, String fileName) throws IOException {
        Path path = Path.of(tmpDir, subDir);
        File file = new File(path.toFile(), fileName);
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Failed creating directory " + dir.getAbsolutePath());
            }
        }
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new RuntimeException("Failed creating file " + file.getAbsolutePath());
            }
        }
        return file;
    }
}
