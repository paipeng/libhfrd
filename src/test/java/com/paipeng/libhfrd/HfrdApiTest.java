package com.paipeng.libhfrd;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HfrdApiTest {

    @Test
    public void testConnect() {
        long deviceId = HfrdApi.connect(-1L);
        Assertions.assertTrue(deviceId >=0 );

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        boolean result = HfrdApi.close(deviceId);
        Assertions.assertTrue(result);
    }

    @Test
    void getVersion() {
        String version = HfrdApi.getVersion(-1L);
        System.out.println("version: " + version);
    }

    @Test
    void connect() {
    }

    @Test
    void close() {
    }

    @Test
    void changeLED() {
        long deviceId = -1L;
        byte color = 2;
        // color 0: LED OFF
        // color 1: LED ON RED
        // color 2: LED ON GREEN
        // color 3: LED ON ORANGE (RED/YELLOW)
        HfrdApi.changeLED(deviceId, HfrdApi.LED.LED_RED);
    }
}