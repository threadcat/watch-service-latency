package com.threadcat.latency.common;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Operating system adapter to set thread CPU affinity.
 * Thread is expected to have a name unique in first 15 characters because of linux 'ps' format limitation.
 * <p>
 * Show isolated CPUs:
 * cat /sys/devices/system/cpu/isolated
 * <p>
 * Grubby persistent changes from command line:
 * grubby --info=DEFAULT
 * grubby --args=isolcpus=2,3 --update-kernel /boot/vmlinuz-5.5.13-200.fc31.x86_64
 * grubby --remove-args="isolcpus" --update-kernel /boot/vmlinuz-5.5.13-200.fc31.x86_64
 *
 * @author threadcat
 */
public class LinuxTaskSet {
    static final String LWP_PID = "ps -eL | grep MAIN_PID | grep THREAD_NAME | awk '{print $2}'";
    static final String GET_MASK = "taskset -p $(LWP_PID)";
    static final String SET_MASK = "taskset -p CPU_MASK $(LWP_PID)";
    static final String GET_RT_ATT = "chrt -p $(LWP_PID)";
    static final String SET_RT_ATT = "chrt -r -p PRIORITY $(LWP_PID)";

    public static long getCpuMask(String threadName) throws IOException, InterruptedException {
        String cmd = GET_MASK.replace("LWP_PID", lightWeightProcessId(threadName));
        String output = executeGet(cmd, "CPU affinity");
        String cpuMask = output.replaceAll("(^.* )|(\n)", "");
        return Long.parseLong(cpuMask);
    }

    public static void setCpuMask(String threadName, String cpuMask) throws IOException, InterruptedException {
        String cmd = SET_MASK
                .replace("CPU_MASK", cpuMask)
                .replace("LWP_PID", lightWeightProcessId(threadName));
        executeSet(cmd, "CPU affinity");
    }

    public static String getRuntimePolicy(String threadName) throws IOException, InterruptedException {
        String cmd = GET_RT_ATT.replace("LWP_PID", lightWeightProcessId(threadName));
        String output = executeGet(cmd, "runtime policy");
        String atts = output
                .replaceAll("(\n.*: )", ",")
                .replaceAll("(^.*: )|(\n)", "");
        return atts;
    }

    /**
     * Set scheduling policy to SCHED_RR with specified priority 1-99.
     */
    public static void setRealtimePriority(String threadName, int priority) throws IOException, InterruptedException {
        String cmd = SET_RT_ATT
                .replace("LWP_PID", lightWeightProcessId(threadName))
                .replace("PRIORITY", Integer.toString(priority));
        executeSet(cmd, "realtime scheduling policy");
    }

    private static String executeGet(String cmd, String name) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("sh", "-c", cmd).start();
        int status = process.waitFor();
        if (status == 0) {
            return new String(process.getInputStream().readAllBytes(), US_ASCII);
        } else {
            printErr(process);
            throw new RuntimeException(String.format("Failed getting %s: %s", name, cmd));
        }
    }

    private static void executeSet(String cmd, String name) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("sh", "-c", cmd).start();
        int status = process.waitFor();
        printOut(process);
        if (status != 0) {
            printErr(process);
            throw new RuntimeException(String.format("Failed setting %s: %s", name, cmd));
        }
    }

    private static String lightWeightProcessId(String threadName) {
        return LWP_PID
                .replace("MAIN_PID", processId())
                .replace("THREAD_NAME", threadName15(threadName));
    }

    private static String processId() {
        return Long.toString(ProcessHandle.current().pid());
    }

    private static String threadName15(String name) {
        return name.substring(0, Math.min(15, name.length()));
    }

    private static void printOut(Process process) throws IOException {
        System.out.print(new String(process.getInputStream().readAllBytes(), US_ASCII));
    }

    private static void printErr(Process process) throws IOException {
        System.err.print(new String(process.getErrorStream().readAllBytes(), US_ASCII));
    }
}
