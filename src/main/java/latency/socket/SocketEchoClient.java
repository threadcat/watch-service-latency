package latency.socket;

import latency.common.DataHandler;
import latency.common.Statistics;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static latency.socket.SocketEchoServer.*;


public class SocketEchoClient {

    public static void main(String[] args) throws Exception {
        SocketChannel channel = SocketChannel.open(ADDRESS);
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
        DataHandler dataHandler = new DataHandler();
        Statistics statistics = new Statistics();
        statistics.start();
        long counter = 100_000;
        for (long i = 0; i < counter; i++) {
            long timeA = System.nanoTime();
            dataHandler.writeSocket(i, 0L, channel);
            int selected = selector.select();
            if (selected > 0) {
                final var selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    try {
                        if (dataHandler.readSocket(channel)) {
                            long n = dataHandler.getSequence();
                            long timeB = dataHandler.getTimestamp();
                            long timeC = System.nanoTime();
                            dataHandler.validate(i, n, timeA, timeB, timeC);
                            statistics.update(timeA, timeB);
                            statistics.update(timeB, timeC);
                        } else {
                            key.cancel();
                        }
                    } catch (IOException e) {
                        System.out.println("Disconnected");
                    }
                    selectionKeys.remove(key);
                }
            }
        }
        statistics.stop();
        System.out.printf("Executed %s times in %.3f seconds, one-way max latency %.3f millis, average %.3f micros\n",
                counter, statistics.elapsed(), statistics.max(), statistics.avg());
    }
}