package com.threadcat.latency.utils;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.LinuxTaskSet;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

import static com.threadcat.latency.socket.SocketEchoServer.processAcceptableKey;
import static com.threadcat.latency.socket.SocketEchoServer.startSocketAcceptor;

/**
 * Data sink for {@link SocketClient}.
 */
public class SocketServer {
    private static final String SOCKET_SERVER = "socket_server";

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Required parameters: <host> <port> <cpu_mask_hex>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String cpuMask = args[2];
        Thread.currentThread().setName(SOCKET_SERVER);
        LinuxTaskSet.setCpuMask(SOCKET_SERVER, cpuMask);
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

    private static void processReadableKey(SelectionKey key, DataHandler dataHandler) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (dataHandler.readSocket(channel)) {
            //
        } else {
            System.out.println("Disconnected " + channel.getRemoteAddress());
            key.cancel();
        }
    }
}