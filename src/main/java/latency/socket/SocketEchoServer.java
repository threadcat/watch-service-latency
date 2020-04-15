package latency.socket;

import latency.common.CpuAffinity;
import latency.common.DataHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Socket latency test echo server.
 * Reads sequence number from request and replies with that number and timestamp in response.
 */
public class SocketEchoServer {
    static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost", 10101);

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("socket_echo_server");
        CpuAffinity.setCpuAffinity("0x4");
        Selector selector = Selector.open();
        startSocketAcceptor(selector);
        DataHandler dataHandler = new DataHandler();
        System.out.println("Started");
        for (; ; ) {
            if (selector.select() > 0) {
                final var selectionKeys = selector.selectedKeys();
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

    private static void startSocketAcceptor(Selector selector) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
        serverChannel.bind(ADDRESS);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    static void processAcceptableKey(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
        channel.register(key.selector(), SelectionKey.OP_READ);
    }

    private static void processReadableKey(SelectionKey key, DataHandler dataHandler) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (dataHandler.readSocket(channel)) {
            long sequence = dataHandler.getSequence();
            long timestamp = System.nanoTime();
            if (!dataHandler.writeSocket(sequence, timestamp, channel)) {
                key.cancel();
            }
        } else {
            key.cancel();
        }
    }
}
