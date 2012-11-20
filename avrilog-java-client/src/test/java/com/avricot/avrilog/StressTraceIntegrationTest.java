package com.avricot.avrilog;

import org.junit.Test;

import com.avricot.avrilog.trace.Trace;

public class StressTraceIntegrationTest {

    @Test
    public void stressTest() {
        AvrilogClient.init("test");
        for (int i = 0; i < 10000; i++) {
            long date = System.currentTimeMillis();
            AvrilogClient.trace(getTrace(), true);
            System.out.println(System.currentTimeMillis() - date);
        }
    }

    private Trace getTrace() {
        return new Trace().setCategory("category");
    }
}
