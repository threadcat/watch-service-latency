package latency.common;

public class Statistics {
    private long startMs;
    private long stopMs;
    private long maxNs;
    private long totalNs;
    private long counter;

    public void start() {
        reset();
        this.startMs = System.currentTimeMillis();
    }

    public void stop() {
        this.stopMs = System.currentTimeMillis();
    }

    public void reset() {
        maxNs = 0;
        totalNs = 0;
        counter = 0;
    }

    public void update(long timeA, long timeB) {
        long delta = timeB - timeA;
        if (delta > maxNs) {
            maxNs = delta;
        }
        totalNs += delta;
        counter++;
    }

    // Returns millis
    public double max() {
        return maxNs * 1e-6;
    }

    // Returns micros
    public double avg() {
        return 1e-3 * totalNs / counter;
    }

    // Returns seconds
    public double elapsed() {
        return 1e-3 * (stopMs - startMs);
    }
}
