package wsl;

import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * WatchService latency test echo server.
 * Reads sequence number from file 'A' writing that number and timestamp to file 'B'.
 */
public class WatcherEchoServer {
    static final Path WATCH_DIR_A = Path.of("./watch_dir_a");
    static final Path WATCH_DIR_B = Path.of("./watch_dir_b");
    static final File FILE_A = new File(WATCH_DIR_A.toFile(), "watch_file_a.dat");
    static final File FILE_B = new File(WATCH_DIR_B.toFile(), "watch_file_b.dat");
    static final ByteBuffer BUFFER_RD = ByteBuffer.allocate(16);
    static final ByteBuffer BUFFER_WR = ByteBuffer.allocate(16);

    public static void main(String[] args) throws Exception {
        FileChannel channel_a = openFile(FILE_A);
        FileChannel channel_b = openFile(FILE_B);
        WatchService ws = registerWatch(WATCH_DIR_A);
        System.out.println("Echo started");
        for (; ; ) {
            read(ws, channel_a);
            long sequence = BUFFER_RD.getLong(0);
            long timestamp = System.nanoTime();
            write(sequence, timestamp, channel_b);
        }
    }

    static void read(WatchService ws, FileChannel channel_b) throws InterruptedException, IOException {
        WatchKey key = ws.take();
        key.pollEvents();
        key.reset();
        BUFFER_RD.clear();
        channel_b.read(BUFFER_RD, 0);
    }

    static void write(long sequence, long timestamp, FileChannel channel_a) throws IOException {
        BUFFER_WR.putLong(0, sequence)
                .putLong(8, timestamp)
                .rewind();
        channel_a.write(BUFFER_WR, 0);
    }

    static FileChannel openFile(java.io.File file) throws IOException {
        createFile(file);
        return FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
    }

    static void createFile(File file) throws IOException {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    static WatchService registerWatch(Path dir) throws IOException {
        WatchService ws = FileSystems.getDefault().newWatchService();
        WatchEvent.Kind<?>[] eventTypes = new WatchEvent.Kind[]{ENTRY_MODIFY};
        dir.register(ws, eventTypes, SensitivityWatchEventModifier.HIGH);
        return ws;
    }
}