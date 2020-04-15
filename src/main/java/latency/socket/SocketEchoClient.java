package latency.socket;

import latency.common.CpuAffinity;
import latency.common.DataHandler;
import latency.common.Statistics;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static latency.socket.SocketEchoServer.ADDRESS;

/**
 * Socket latency test client.
 * Sends incremental sequence to echo server checking response time.
 * Prints out summary after execution.
 */
public class SocketEchoClient {

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("socket_echo_client");
        CpuAffinity.setCpuAffinity("0x8");
        SocketChannel channel = openSocket();
        Selector selector = registerSelector(channel);
        DataHandler dataHandler = new DataHandler();
        Statistics statistics = new Statistics();
        long counter = 1000_000;
        long warmup = counter - 100_000;
        System.out.println("Started");
        statistics.start();
        for (long i = 0; i < counter; i++) {
            long timeA = System.nanoTime();
            dataHandler.writeSocket(i, 0L, channel);
            if (selector.select() > 0) {
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
                            if (i == warmup) {
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

    private static SocketChannel openSocket() throws IOException {
        SocketChannel channel = SocketChannel.open(ADDRESS);
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
