package com.threadcat.latency.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Handler for read/write operations on {@link FileChannel} and {@link SocketChannel}.
 *
 * @author threadcat
 */
public class DataHandler {
    private final ByteBuffer bufferRd;
    private final ByteBuffer bufferWr;

    public DataHandler() {
        bufferRd = ByteBuffer.allocate(16);
        bufferWr = ByteBuffer.allocate(16);
    }

    public DataHandler(ByteBuffer bufferRd, ByteBuffer bufferWr) {
        this.bufferRd = bufferRd;
        this.bufferWr = bufferWr;
    }

    public long getSequence() {
        return bufferRd.getLong(0);
    }

    public long getTimestamp() {
        return bufferRd.getLong(8);
    }

    public boolean readFile(FileChannel channel) throws IOException {
        channel.position(0L);
        return read(channel);
    }

    public boolean readSocket(SocketChannel channel) throws IOException {
        return read(channel);
    }

    public boolean writeFile(FileChannel channel, long sequence, long timestamp) throws IOException {
        channel.position(0L);
        return write(channel, sequence, timestamp);
    }

    public boolean writeSocket(SocketChannel channel, long sequence, long timestamp) throws IOException {
        return write(channel, sequence, timestamp);
    }

    public void write(long sequence, long timestamp) {
        // Ordering is important for memory-mapped file data transfer
        // where client is spinning on 'sequence' so it is needed to write 'timestamp' before 'sequence'.
        bufferWr.putLong(8, timestamp)
                .putLong(0, sequence);
    }

    private boolean read(ByteChannel channel) throws IOException {
        bufferRd.clear();
        for (int i = 0; i < 16; ) {
            int n = channel.read(bufferRd);
            if (n < 0) {
                return false;
            }
            i += n;
        }
        return true;
    }

    private boolean write(ByteChannel channel, long sequence, long timestamp) throws IOException {
        write(sequence, timestamp);
        bufferWr.rewind();
        for (int i = 0; i < 16; ) {
            int n = channel.write(bufferWr);
            if (n < 0) {
                return false;
            }
            i += n;
        }
        return true;
    }
}
