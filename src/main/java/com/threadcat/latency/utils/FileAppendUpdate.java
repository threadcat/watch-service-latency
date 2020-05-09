package com.threadcat.latency.utils;

import com.threadcat.latency.common.LinuxTaskSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Compares 'append' vs 'update' FileChannel latency.
 */
/*
SATA3:
Executed 10M updates in 13.018 s, latency 1301 ns
Executed 10M appends in 13.596 s, latency 1359 ns

NVMe:
Executed 10M updates in 10.594 s, latency 1059 ns
Executed 10M appends in 10.763 s, latency 1076 ns

/tmp (SATA3):
Executed 10M updates in 7.937 s, latency 793 ns
Executed 10M appends in 8.304 s, latency 830 ns
 */
public class FileAppendUpdate {
    private static final String THREAD_NAME = "file-append-update";

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Required parameters: <directory> <cpu_mask_hex> <warmup_cycles> <measure_cycles>");
            return;
        }
        String dir = args[0];
        String cpuMask = args[1];
        int warmup = Integer.parseInt(args[2]);
        int measure = Integer.parseInt(args[3]);
        Thread.currentThread().setName(THREAD_NAME);
        LinuxTaskSet.setCpuMask(THREAD_NAME, cpuMask);
        eventLoop(dir, warmup, measure);
    }

    private static void eventLoop(String dir, int warmup, int measure) throws IOException {
        FileChannel channelA = FileUtils.openFile(dir, FileUtils.WATCH_DIR_A, FileUtils.FILE_A);
        FileChannel channelB = FileUtils.openFile(dir, FileUtils.WATCH_DIR_B, FileUtils.FILE_B);
        channelA.truncate(0);
        ByteBuffer buffer = ByteBuffer.allocate(16);
        update(channelA, buffer, false, warmup);
        update(channelB, buffer, true, warmup);
        update(channelA, buffer, false, measure);
        update(channelB, buffer, true, measure);
    }

    private static void update(FileChannel channelA, ByteBuffer buffer, boolean append, int count) throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            buffer.rewind();
            if (append) {
                channelA.write(buffer);
            } else {
                channelA.write(buffer, 0);
            }
        }
        long stop = System.nanoTime();
        long fileSize = channelA.size();
        System.out.println("File size " + fileSize);
        double elapsed = 1e-9 * (stop - start);
        long num = (long) (1e-6 * count);
        long latency = (stop - start) / count;
        String type = append ? "appends" : "updates";
        System.out.printf("Executed %dM %s in %.3f s, latency %d ns\n", num, type, elapsed, latency);
    }
}
