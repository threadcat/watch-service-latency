package com.threadcat.latency.socket;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.LinuxTaskSet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * Socket latency test echo server.
 * Reads sequence number from request and replies with that number and timestamp in response.
 *
 * @author threadcat
 */
public class SocketEchoServer {
    private static final String THREAD_NAME = "socket-echo-server";

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Required parameters: <host> <port> <cpu_mask_hex>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String cpuMask = args[2];
        Thread.currentThread().setName(THREAD_NAME);
        LinuxTaskSet.setCpuMask(THREAD_NAME, cpuMask);
        Selector selector = startSocketAcceptor(host, port);
        eventLoop(selector);
    }

    private static void eventLoop(Selector selector) throws IOException {
        DataHandler dataHandler = new DataHandler();
        System.out.println("Started");
        for (; ; ) {
            if (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    try {
                        if (key.isReadable()) {
                            processReadableKey(key, dataHandler);
                        } else if (key.isAcceptable()) {
                            processAcceptableKey(key);
                        }
                    } catch (IOException ioe) {
                        System.out.println("Disconnected");
                        key.cancel();
                    }
                    selectionKeys.remove(key);
                }
            }
        }
    }

    public static Selector startSocketAcceptor(String host, int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
        serverChannel.bind(new InetSocketAddress(host, port));
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        return selector;
    }

    public static void processAcceptableKey(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
        channel.register(key.selector(), SelectionKey.OP_READ);
        System.out.println("Connection accepted " + channel.getRemoteAddress());
    }

    private static void processReadableKey(SelectionKey key, DataHandler dataHandler) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (dataHandler.readSocket(channel)) {
            long sequence = dataHandler.getSequence();
            long timestamp = System.nanoTime();
            if (!dataHandler.writeSocket(channel, sequence, timestamp)) {
                System.out.println("Connection broken " + channel.getRemoteAddress());
                key.cancel();
            }
        } else {
            System.out.println("Disconnected " + channel.getRemoteAddress());
            key.cancel();
        }
    }
}
