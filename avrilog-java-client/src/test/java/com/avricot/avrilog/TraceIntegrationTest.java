package com.avricot.avrilog;

import org.junit.Test;

public class TraceIntegrationTest {

    @Test
    public void init() {
        Trace.init();
        Trace.trance("test");
    }

    @Test
    public void stressTest() {
        Trace.init();
        for (int i = 0; i < 100; i++) {
            Trace.trance("test" + i);
        }
    }
}
