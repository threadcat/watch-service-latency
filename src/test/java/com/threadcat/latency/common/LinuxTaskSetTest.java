package com.threadcat.latency.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinuxTaskSetTest {
    private static final String THREAD_NAME = "latency_something";

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName(THREAD_NAME);
        printCpuMaskHex();
        LinuxTaskSet.setCpuMask(THREAD_NAME, "0x4");
        printCpuMaskHex();
        printRuntimeAtts();
        LinuxTaskSet.setRealtimePriority(THREAD_NAME, 1);
        printRuntimeAtts();
    }

    private static void printRuntimeAtts() throws IOException, InterruptedException {
        String atts = LinuxTaskSet.getRuntimePolicy(THREAD_NAME);
        System.out.println(atts);
    }

    private static void printCpuMaskHex() throws IOException, InterruptedException {
        long cpuMask = LinuxTaskSet.getCpuMask(THREAD_NAME);
        System.out.println(Long.toBinaryString(cpuMask));
    }

    @Test
    void testSetCpuMask() throws Exception {
        Thread.currentThread().setName(THREAD_NAME);
        //
        LinuxTaskSet.setCpuMask(THREAD_NAME, "0x4");
        assertEquals(4L, LinuxTaskSet.getCpuMask(THREAD_NAME));
        //
        LinuxTaskSet.setCpuMask(THREAD_NAME, "0x8");
        assertEquals(8L,  LinuxTaskSet.getCpuMask(THREAD_NAME));
    }
}
