package com.threadcat.latency.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NixTaskSetTest {

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("latency_something");
        String cpuAffinity = Long.toBinaryString(NixTaskSet.getCpuMask());
        System.out.println(cpuAffinity);
        NixTaskSet.setCpuMask("0x4");
        cpuAffinity = Long.toBinaryString(NixTaskSet.getCpuMask());
        System.out.println(cpuAffinity);
    }

    @Test
    void testSetCpuMask() throws Exception {
        Thread.currentThread().setName("latency_something");
        NixTaskSet.setCpuMask("0x4");
        long cpuAffinity = NixTaskSet.getCpuMask();
        assertEquals(4L, cpuAffinity);
    }
}
