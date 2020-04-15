package latency.common;

import java.io.IOException;

/**
 * Utility class to set thread cpu affinity.
 * Thread has to have name unique in first 15 characters because of linux 'ps' format limitation.
 * <p>
 * Show current system state:
 * cat /sys/devices/system/cpu/isolated
 * <p>
 * Default Grubby parameters:
 * cat /etc/default/grub
 * <p>
 * Grubby persistent changes from command line:
 * grubby --info=DEFAULT
 * grubby --args=isolcpus=2,3 --update-kernel /boot/vmlinuz-5.5.13-200.fc31.x86_64
 * grubby --remove-args="..." --update-kernel /boot/vmlinuz-5.5.13-200.fc31.x86_64
 */
public class CpuAffinity {

    // @todo move test method to tests
    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("latency_something");
        String cpuAffinity = Long.toBinaryString(getCpuAffinity());
        System.out.println(cpuAffinity);
        setCpuAffinity("0x4");
        cpuAffinity = Long.toBinaryString(getCpuAffinity());
        System.out.println(cpuAffinity);
    }

    public static long getCpuAffinity() throws IOException, InterruptedException {
        String lwp = lightweightProcessId();
        String cmd = "taskset -p $(LWP)".replace("LWP", lwp);
        Process process = new ProcessBuilder("bash", "-c", cmd).start();
        int tasksetStatus = process.waitFor();
        if (tasksetStatus == 0) {
            String s = new String(process.getInputStream().readAllBytes());
            String number = s.replaceAll("(^.* )|(\n)", "");
            return Long.parseLong(number);
        } else {
            System.err.println(new String(process.getErrorStream().readAllBytes()));
            return -1;
        }
    }

    public static void setCpuAffinity(String cpuMask) throws IOException, InterruptedException {
        String cmd = "/bin/taskset -p CPU_MASK $(LWP)"
                .replace("CPU_MASK", cpuMask)
                .replace("LWP", lightweightProcessId());
        Process process = new ProcessBuilder("bash", "-c", cmd).start();
        int tasksetStatus = process.waitFor();
        System.out.println(new String(process.getInputStream().readAllBytes()));
        if (tasksetStatus != 0) {
            System.out.println(new String(process.getErrorStream().readAllBytes()));
            System.out.println("Failed assigning cpu affinity, taskset exit status " + tasksetStatus);
            System.exit(tasksetStatus);
        }
    }

    private static String lightweightProcessId() {
        String ppid = "ps -ef | grep CALLER_CLASS_NAME | grep -v grep | awk '{print $3}'"
                .replace("CALLER_CLASS_NAME", getCallerClassName());
        return "ps -L --ppid $(PPID) | grep THREAD_NAME_15 | awk '{print $2}'"
                .replace("PPID", ppid)
                .replace("THREAD_NAME_15", linuxThreadName());
    }

    private static String linuxThreadName() {
        String name = Thread.currentThread().getName();
        int length = Math.min(15, name.length());
        return name.substring(0, length);
    }

    private static String getCallerClassName() {
        return Thread.currentThread().getStackTrace()[4].getClassName();
    }
}

