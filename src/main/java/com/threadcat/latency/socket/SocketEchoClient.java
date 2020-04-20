package com.threadcat.latency.socket;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.NixTaskSet;
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
    private static final String SOCKET_ECHO_CLIENT = "socket_echo_client";

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Required parameters: <host> <port> <cpu_mask_hex>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String cpuMask = args[2];
        Thread.currentThread().setName(SOCKET_ECHO_CLIENT);
        NixTaskSet.setCpuMask(SOCKET_ECHO_CLIENT, cpuMask);
        SocketChannel channel = openSocket(host, port);
        eventLoop(channel);
    }

    private static void eventLoop(SocketChannel channel) throws IOException {
        Selector selector = registerSelector(channel);
        DataHandler dataHandler = new DataHandler();
        long counter = 200_000;
        long warmup = counter - 100_000;
        PingClient pingClient = new PingClient(warmup);
        for (long i = 0; i < counter; i++) {
            long timeA = System.nanoTime();
            dataHandler.writeSocket(channel, i, 0L);
            if (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    try {
                        if (dataHandler.readSocket(channel)) {
                            long n = dataHandler.getSequence();
                            long timeB = dataHandler.getTimestamp();
                            pingClient.update(i, n, timeA, timeB);
                        } else {
                            key.cancel();
                        }
                    } catch (IOException e) {
                        System.out.println("Disconnected");
                        key.cancel();
                    }
                    selectionKeys.remove(key);
                }
            }
        }
        pingClient.printSummary();
    }

    private static SocketChannel openSocket(String host, int port) throws IOException {
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
