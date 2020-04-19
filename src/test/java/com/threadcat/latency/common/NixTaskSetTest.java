package com.threadcat.latency.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NixTaskSetTest {

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("latency_something");
        printCpuMaskHex();
        NixTaskSet.setCpuMask("0x4");
        printCpuMaskHex();
    }

    private static void printCpuMaskHex() throws IOException, InterruptedException {
        long cpuMask = NixTaskSet.getCpuMask();
        System.out.println(Long.toBinaryString(cpuMask));
    }

    @Test
    void testSetCpuMask() throws Exception {
        Thread.currentThread().setName("latency_something");
        NixTaskSet.setCpuMask("0x4");
        long cpuAffinity = NixTaskSet.getCpuMask();
        assertEquals(4L, cpuAffinity);
    }
}
