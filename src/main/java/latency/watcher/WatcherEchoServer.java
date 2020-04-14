package latency.watcher;

import com.sun.nio.file.SensitivityWatchEventModifier;
import latency.common.DataHandler;

import java.io.File;
import java.io.IOException;
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

    public static void main(String[] args) throws Exception {
        FileChannel channelA = openFile(FILE_A);
        FileChannel channelB = openFile(FILE_B);
        WatchService watchService = registerWatch(WATCH_DIR_A);
        DataHandler dataHandler = new DataHandler();
        System.out.println("Started");
        for (; ; ) {
            poll(watchService);
            if (dataHandler.readFile(channelA)) {
                long sequence = dataHandler.getSequence();
                long timestamp = System.nanoTime();
                dataHandler.writeFile(sequence, timestamp, channelB);
            }
        }
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

    static void poll(WatchService ws) throws InterruptedException {
        WatchKey key = ws.take();
        key.pollEvents();
        key.reset();
    }
}
