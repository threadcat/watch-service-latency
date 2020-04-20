package com.threadcat.latency.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NixTaskSetTest {
    private static final String THREAD_NAME = "latency_something";

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName(THREAD_NAME);
        printCpuMaskHex();
        NixTaskSet.setCpuMask(THREAD_NAME, "0x4");
        printCpuMaskHex();
    }

    private static void printCpuMaskHex() throws IOException, InterruptedException {
        long cpuMask = NixTaskSet.getCpuMask(THREAD_NAME);
        System.out.println(Long.toBinaryString(cpuMask));
    }

    @Test
    void testSetCpuMask() throws Exception {
        Thread.currentThread().setName(THREAD_NAME);
        //
        NixTaskSet.setCpuMask(THREAD_NAME, "0x4");
        assertEquals(4L, NixTaskSet.getCpuMask(THREAD_NAME));
        //
        NixTaskSet.setCpuMask(THREAD_NAME, "0x8");
        assertEquals(8L,  NixTaskSet.getCpuMask(THREAD_NAME));
    }
}
