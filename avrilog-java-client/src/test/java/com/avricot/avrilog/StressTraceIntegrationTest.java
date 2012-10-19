package com.avricot.avrilog;

import org.junit.Test;

public class StressTraceIntegrationTest {

    @Test
    public void stressTest() {
        AvrilogClient.init();
        for (int i = 0; i < 10000; i++) {
            AvrilogClient.trace(getTrace());
        }
    }

    private Trace getTrace() {
        return new Trace().setCategory("category");
    }
}
