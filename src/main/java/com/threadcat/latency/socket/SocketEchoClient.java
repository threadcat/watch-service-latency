package com.threadcat.latency.socket;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.LinuxTaskSet;
import com.threadcat.latency.common.PingClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * Socket latency test client.
 * Sends incremental sequence to echo server checking response time.
 * Prints out summary after execution.
 *
 * @author threadcat
 */
public class SocketEchoClient {
    private static final String THREAD_NAME = "socket-echo-client";

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
        Thread.currentThread().setName(THREAD_NAME);
        LinuxTaskSet.setCpuMask(THREAD_NAME, cpuMask);
        SocketChannel channel = openSocket(host, port);
        eventLoop(channel, warmup, measure);
    }

    private static void eventLoop(SocketChannel channel, long warmup, long measure) throws IOException {
        Selector selector = registerSelector(channel);
        DataHandler dataHandler = new DataHandler();
        long counter = warmup + measure;
        PingClient pingClient = new PingClient(warmup);
        for (long i = 0; i < counter; i++) {
            long timeA = System.nanoTime();
            if (!dataHandler.writeSocket(channel, i, 0L)) {
                System.out.println("Connection broken " + channel.getRemoteAddress());
                return;
            }
            if (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    if (dataHandler.readSocket(channel)) {
                        long n = dataHandler.getSequence();
                        long timeB = dataHandler.getTimestamp();
                        pingClient.update(i, n, timeA, timeB);
                    } else {
                        System.out.println("Disconnected " + channel.getRemoteAddress());
                        return;
                    }
                    selectionKeys.remove(key);
                }
            }
        }
        pingClient.printSummary();
    }

    public static SocketChannel openSocket(String host, int port) throws IOException {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
        return channel;
    }

    private static Selector registerSelector(SocketChannel channel) throws IOException {
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
        return selector;
    }
}
