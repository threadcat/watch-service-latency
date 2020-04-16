package com.threadcat.latency.socket;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.NixTaskSet;
import com.threadcat.latency.common.Statistics;

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

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Required parameters: <host> <port> <cpu_mask_hex>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String cpuMask = args[2];
        Thread.currentThread().setName("socket_echo_client");
        NixTaskSet.setCpuMask(cpuMask);
        SocketChannel channel = openSocket(host, port);
        loop(channel);
    }

    static void loop(SocketChannel channel) throws IOException {
        Selector selector = registerSelector(channel);
        DataHandler dataHandler = new DataHandler();
        Statistics statistics = new Statistics();
        long counter = 200_000;
        long warmup = counter - 100_000;
        System.out.println("Started");
        statistics.start();
        for (long i = 0; i < counter; i++) {
            long timeA = System.nanoTime();
            dataHandler.writeSocket(i, 0L, channel);
            if (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    try {
                        if (dataHandler.readSocket(channel)) {
                            long n = dataHandler.getSequence();
                            long timeB = dataHandler.getTimestamp();
                            long timeC = System.nanoTime();
                            dataHandler.validate(i, n, timeA, timeB, timeC);
                            statistics.update(timeA, timeB);
                            statistics.update(timeB, timeC);
                            if (i == warmup) {
                                System.out.println("Finished warming up");
                                statistics.reset();
                            }
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
        statistics.stop();
        System.out.printf("Executed %s times in %.3f seconds, one-way max latency %.3f us, average %.3f us\n",
                counter - warmup, statistics.elapsed(), statistics.max(), statistics.avg());
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
