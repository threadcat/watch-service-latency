package latency.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class DataHandler {
    private final ByteBuffer BUFFER_RD = ByteBuffer.allocate(16);
    private final ByteBuffer BUFFER_WR = ByteBuffer.allocate(16);

    public long getSequence() {
        return BUFFER_RD.getLong(0);
    }

    public long getTimestamp() {
        return BUFFER_RD.getLong(8);
    }

    public boolean readFile(FileChannel channel) throws IOException {
        channel.position(0L);
        return read(channel);
    }

    public boolean readSocket(SocketChannel channel) throws IOException {
        return read(channel);
    }

    public boolean writeFile(long sequence, long timestamp, FileChannel channel) throws IOException {
        channel.position(0L);
        return write(sequence, timestamp, channel);
    }

    public boolean writeSocket(long sequence, long timestamp, SocketChannel channel) throws IOException {
        return write(sequence, timestamp, channel);
    }

    public void validate(long sequenceA, long sequenceB, long timeA, long timeB, long timeC) {
        if (sequenceA != sequenceB) {
            throw new RuntimeException(String.format("Unexpected sequence number %s != %s", sequenceA, sequenceB));
        }
        if (timeB < timeA || timeB > timeC) {
            throw new RuntimeException(
                    String.format("System nano time does not hold between two JVM [%s, %s, %s]", timeA, timeB, timeC));
        }
    }

    private boolean read(ByteChannel channel) throws IOException {
        BUFFER_RD.clear();
        for (int i = 0; i < 16; ) {
            int n = channel.read(BUFFER_RD);
            if (n < 0) {
                System.out.println("Failed reading");
                return false;
            }
            i += n;
        }
        return true;
    }

    private boolean write(long sequence, long timestamp, ByteChannel channel) throws IOException {
        BUFFER_WR.putLong(0, sequence)
                .putLong(8, timestamp)
                .rewind();
        for (int i = 0; i < 16; ) {
            int n = channel.write(BUFFER_WR);
            if (n < 0) {
                System.out.println("Failed writing");
                return false;
            }
            i += n;
        }
        return true;
    }
}
