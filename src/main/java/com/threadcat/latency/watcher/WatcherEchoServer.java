package com.threadcat.latency.watcher;

import com.sun.nio.file.SensitivityWatchEventModifier;
import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.LinuxTaskSet;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import static com.threadcat.latency.utils.FileUtils.*;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * WatchService latency test echo server.
 * Reads sequence number from file 'A' writing that number and timestamp to file 'B'.
 *
 * @author threadcat
 */
public class WatcherEchoServer {
    private static final String THREAD_NAME = "watcher_echo_server";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Required parameters: <directory> <cpu_mask_hex>");
            return;
        }
        String dir = args[0];
        String cpuMask = args[1];
        Thread.currentThread().setName(THREAD_NAME);
        LinuxTaskSet.setCpuMask(THREAD_NAME, cpuMask);
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

}
