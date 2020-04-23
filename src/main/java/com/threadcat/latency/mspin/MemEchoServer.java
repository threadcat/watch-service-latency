package com.threadcat.latency.mspin;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.LinuxTaskSet;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static com.threadcat.latency.watcher.WatcherEchoServer.*;
import static com.threadcat.latency.watcher.WatcherEchoServer.FILE_B;

public class MemEchoServer {
    private static final String MEM_ECHO_SERVER = "mem-echo-server";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Required parameters: <directory> <cpu_mask_hex>");
            return;
        }
        String dir = args[0];
        String cliCpuMask = args[1];
        Thread.currentThread().setName(MEM_ECHO_SERVER);
        LinuxTaskSet.setCpuMask(MEM_ECHO_SERVER, cliCpuMask);
        eventLoop(dir);
    }

    private static void eventLoop(String dir) {
        try (FileChannel channelA = openFile(dir, WATCH_DIR_A, FILE_A);
             FileChannel channelB = openFile(dir, WATCH_DIR_B, FILE_B)) {
            MappedByteBuffer bufferA = channelA.map(FileChannel.MapMode.READ_WRITE, 0, 16);
            MappedByteBuffer bufferB = channelB.map(FileChannel.MapMode.READ_WRITE, 0, 16);
            DataHandler dataHandler = new DataHandler(bufferA, bufferB);
            System.out.println("Started");
            long lastSequence = -1L;
            for (; ; ) {
                    long sequence = dataHandler.getSequence();
                    if (sequence != lastSequence) {
                        long timestamp = System.nanoTime();
                        dataHandler.write(sequence, timestamp);
                        lastSequence = sequence;
                    }
            }
        } catch (IOException e) {
            System.out.println("Failed running echo server");
        }
    }
}
