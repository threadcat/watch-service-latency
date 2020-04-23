package com.threadcat.latency.utils;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.LinuxTaskSet;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.threadcat.latency.socket.SocketEchoClient.openSocket;

/**
 * Measures socket 'send' latency.
 * {@link SocketServer} is needed running as data sink.
 */
/*
Sent 1000000 packets in 3.518 seconds, latency 3517 ns
 */
public class SocketClient {
    private static final String SOCKET_CLIENT = "socket_client";

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.out.println("Required parameters: <host> <port> <cpu_mask_hex> <warmup_cycles> <measure_cycles>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String cpuMask = args[2];
        int warmup = Integer.parseInt(args[3]);
        int measure = Integer.parseInt(args[4]);
        Thread.currentThread().setName(SOCKET_CLIENT);
        LinuxTaskSet.setCpuMask(SOCKET_CLIENT, cpuMask);
        SocketChannel channel = openSocket(host, port);
        eventLoop(channel, warmup, measure);
    }

    private static void eventLoop(SocketChannel channel, long warmup, long measure) throws IOException {
        DataHandler dataHandler = new DataHandler();
        long counter = warmup + measure;
        long start = 0;
        for (long i = 0; i < counter; i++) {
            if (!dataHandler.writeSocket(channel, i, 0L)) {
                System.out.println("Connection broken " + channel.getRemoteAddress());
                return;
            }
            if (i == warmup) {
                start = System.nanoTime();
            }
        }
        long stop = System.nanoTime();
        double elapsed = 1e-9 * (stop - start);
        long latency = (stop - start) / measure;
        System.out.printf("Sent %s packets in %.3f seconds, latency %d ns\n", measure, elapsed, latency);
    }
}
