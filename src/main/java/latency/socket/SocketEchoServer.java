package latency.socket;

import latency.common.DataHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SocketEchoServer {
    static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost", 10101);

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().setReuseAddress(true);
        serverChannel.bind(ADDRESS);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        DataHandler telegrapher = new DataHandler();
        System.out.println("Started");
        for (; ; ) {
            if (selector.select() > 0) {
                final var selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    try {
                        if (key.isReadable()) {
                            processReadableKey(key, telegrapher);
                        } else if (key.isAcceptable()) {
                            processAcceptableKey(key);
                        }
                    } catch (IOException ioe) {
                        System.out.println("Disconnected");
                        key.cancel();
                    }
                    selectionKeys.remove(key);
                }
                ;
            }
        }
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
