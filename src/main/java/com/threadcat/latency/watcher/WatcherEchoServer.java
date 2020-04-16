package com.threadcat.latency.watcher;

import com.sun.nio.file.SensitivityWatchEventModifier;
import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.NixTaskSet;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * WatchService latency test echo server.
 * Reads sequence number from file 'A' writing that number and timestamp to file 'B'.
 *
 * @author threadcat
 */
public class WatcherEchoServer {
    static final String WATCH_DIR_A = "watch_dir_a";
    static final String WATCH_DIR_B = "watch_dir_b";
    static final String FILE_A = "watch_file_a.dat";
    static final String FILE_B = "watch_file_b.dat";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Required parameters: <directory> <cpu_mask_hex>");
            return;
        }
        String dir = args[0];
        String cpuMask = args[1];
        Thread.currentThread().setName("watcher_echo_server");
        NixTaskSet.setCpuMask(cpuMask);
        File fileA = createFile(dir, WATCH_DIR_A, FILE_A);
        File fileB = createFile(dir, WATCH_DIR_B, FILE_B);
        FileChannel channelA = openFile(fileA);
        FileChannel channelB = openFile(fileB);
        WatchService watchService = registerWatch(fileA.getParentFile());
        DataHandler dataHandler = new DataHandler();
        System.out.println("Started");
        for (; ; ) {
            poll(watchService);
            if (dataHandler.readFile(channelA)) {
                long sequence = dataHandler.getSequence();
                long timestamp = System.nanoTime();
                if (!dataHandler.writeFile(sequence, timestamp, channelB)) {
                    throw new RuntimeException("Failed writing to " + FILE_B);
                }
            }
        }
    }

    static FileChannel openFile(File file) throws IOException {
        return FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
    }

    static File createFile(String tmpDir, String subDir, String fileName) throws IOException {
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

    static WatchService registerWatch(File dir) throws IOException {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Expected directory: " + dir);
        }
        WatchService ws = FileSystems.getDefault().newWatchService();
        WatchEvent.Kind<?>[] eventTypes = new WatchEvent.Kind[]{ENTRY_MODIFY};
        dir.toPath().register(ws, eventTypes, SensitivityWatchEventModifier.HIGH);
        return ws;
    }

    static void poll(WatchService ws) throws InterruptedException {
        WatchKey key = ws.take();
        key.pollEvents();
        key.reset();
    }
}
