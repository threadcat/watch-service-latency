package com.threadcat.latency.watcher;

import com.sun.nio.file.SensitivityWatchEventModifier;
import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.LinuxTaskSet;

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
    private static final String WATCHER_ECHO_SERVER = "watcher_echo_server";
    public static final String WATCH_DIR_A = "watch_dir_a";
    public static final String WATCH_DIR_B = "watch_dir_b";
    public static final String FILE_A = "watch_file_a.dat";
    public static final String FILE_B = "watch_file_b.dat";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Required parameters: <directory> <cpu_mask_hex>");
            return;
        }
        String dir = args[0];
        String cpuMask = args[1];
        Thread.currentThread().setName(WATCHER_ECHO_SERVER);
        LinuxTaskSet.setCpuMask(WATCHER_ECHO_SERVER, cpuMask);
        eventLoop(dir);
    }

    private static void eventLoop(String dir) throws IOException, InterruptedException {
        FileChannel channelA = openFile(dir, WATCH_DIR_A, FILE_A);
        FileChannel channelB = openFile(dir, WATCH_DIR_B, FILE_B);
        WatchService watchService = registerWatch(dir, WATCH_DIR_A);
        DataHandler dataHandler = new DataHandler();
        System.out.println("Started");
        for (; ; ) {
            poll(watchService);
            if (dataHandler.readFile(channelA)) {
                long sequence = dataHandler.getSequence();
                long timestamp = System.nanoTime();
                if (!dataHandler.writeFile(channelB, sequence, timestamp)) {
                    System.out.println("Failed writing to " + FILE_B);
                    break;
                }
            } else {
                System.out.println("Failed reading from " + FILE_A);
                break;
            }
        }
    }

    public static FileChannel openFile(String tmpDir, String subDir, String fileName) throws IOException {
        File file = createFile(tmpDir, subDir, fileName);
        return FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
    }

    static WatchService registerWatch(String tmpDir, String subDir) throws IOException {
        WatchService ws = FileSystems.getDefault().newWatchService();
        WatchEvent.Kind<?>[] eventTypes = new WatchEvent.Kind[]{ENTRY_MODIFY};
        Path path = Path.of(tmpDir, subDir);
        path.register(ws, eventTypes, SensitivityWatchEventModifier.HIGH);
        return ws;
    }

    static void poll(WatchService ws) throws InterruptedException {
        WatchKey key = ws.take();
        key.pollEvents();
        key.reset();
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
