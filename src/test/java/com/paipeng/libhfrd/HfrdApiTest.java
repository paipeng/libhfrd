package com.paipeng.libhfrd;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HfrdApiTest {

    @Test
    public void testConnect() {
        long deviceId = HfrdApi.connect(-1L);
        Assertions.assertTrue(deviceId >=0 );

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        boolean result = HfrdApi.close(-1L);
        Assertions.assertTrue(result);
    }

    @Test
    void getVersion() {
        String version = HfrdApi.getVersion(-1L);
        System.out.println("version: " + version);
    }
}