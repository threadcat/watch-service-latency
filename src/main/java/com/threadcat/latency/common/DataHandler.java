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

    public boolean writeFile(FileChannel channel, long sequence, long timestamp) throws IOException {
        channel.position(0L);
        return write(channel, sequence, timestamp);
    }

    public boolean writeSocket(SocketChannel channel, long sequence, long timestamp) throws IOException {
        return write(channel, sequence, timestamp);
    }

    private boolean read(ByteChannel channel) throws IOException {
        BUFFER_RD.clear();
        for (int i = 0; i < 16; ) {
            int n = channel.read(BUFFER_RD);
            if (n < 0) {
                return false;
            }
            i += n;
        }
        return true;
    }

    private boolean write(ByteChannel channel, long sequence, long timestamp) throws IOException {
        BUFFER_WR.putLong(0, sequence)
                .putLong(8, timestamp)
                .rewind();
        for (int i = 0; i < 16; ) {
            int n = channel.write(BUFFER_WR);
            if (n < 0) {
                return false;
            }
            i += n;
        }
        return true;
    }
}
