package com.threadcat.latency.common;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Utility class to set thread CPU affinity.
 * Thread is expected to have a name unique in first 15 characters because of linux 'ps' format limitation.
 * <p>
 * Show current system state:
 * cat /sys/devices/system/cpu/isolated
 * <p>
 * Grubby persistent changes from command line:
 * grubby --info=DEFAULT
 * grubby --args=isolcpus=2,3 --update-kernel /boot/vmlinuz-5.5.13-200.fc31.x86_64
 * grubby --remove-args="isolcpus" --update-kernel /boot/vmlinuz-5.5.13-200.fc31.x86_64
 *
 * @author threadcat
 */
public class NixTaskSet {
    static final String LWP_PID = "ps -eL | grep MAIN_PID | grep THREAD_NAME | awk '{print $2}'";
    static final String GET_MASK = "taskset -p $(LWP_PID)";
    static final String SET_MASK = "taskset -p CPU_MASK $(LWP_PID)";

    public static long getCpuMask(String threadName) throws IOException, InterruptedException {
        String cmd = GET_MASK.replace("LWP_PID", lightWeightProcessId(threadName));
        Process process = new ProcessBuilder("bash", "-c", cmd).start();
        int status = process.waitFor();
        if (status == 0) {
            String output = new String(process.getInputStream().readAllBytes(), US_ASCII);
            String cpuMask = output.replaceAll("(^.* )|(\n)", "");
            return Long.parseLong(cpuMask);
        } else {
            printErr(process);
            throw new RuntimeException("Failed getting CPU affinity: " + cmd);
        }
    }

    public static void setCpuMask(String threadName, String cpuMask) throws IOException, InterruptedException {
        String cmd = SET_MASK
                .replace("CPU_MASK", cpuMask)
                .replace("LWP_PID", lightWeightProcessId(threadName));
        Process process = new ProcessBuilder("bash", "-c", cmd).start();
        int status = process.waitFor();
        printOut(process);
        if (status != 0) {
            printErr(process);
            throw new RuntimeException("Failed setting CPU affinity: " + cmd);
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
