package com.paipeng.libhfrd;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HfrdApiTest {

    @Test
    public void testConnect() {
        long deviceId = HfrdApi.connect(-1L);
        Assertions.assertTrue(deviceId >= 0);

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
        HfrdApi.changeLED(deviceId, HfrdApi.LED.LED_RED, true);
    }

    @Test
    void requestCard() {
        long deviceId = -1L;
        String serialNumber = HfrdApi.requestCard(deviceId);
        System.out.println("serialNumber: " + serialNumber);
    }

    @Test
    void beep() {
    }

    @Test
    void readNTAGSerialNumber() {
    }

    @Test
    void read() {
        long deviceId = HfrdApi.connect(-1L);
        Assertions.assertTrue(deviceId >= 0);

        // HfrdApi.beep(deviceId);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        // NTAG 213 0x2C  error 23
        // NTAG 215 0x86  error
        // NTAG 216 0xE6  error
        //for (byte b = -127; b < 128; b++) {
        byte b = 0x04;
        String data = HfrdApi.read(deviceId, b);
        System.out.println("data: " + data + " addr: " + b);
        //}
        boolean result = HfrdApi.close(deviceId);
        Assertions.assertTrue(result);
    }

    @Test
    void writeData() {
    }

    @Test
    void testWriteData() {
    }

    @Test
    void fastRead() {
        long deviceId = HfrdApi.connect(-1L);
        Assertions.assertTrue(deviceId >= 0);

        // HfrdApi.beep(deviceId);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        // NTAG 213 0x2C  error 23
        // NTAG 215 0x86  error
        // NTAG 216 0xE6  error
        //for (byte b = -127; b < 128; b++) {
        byte b = 0x00;
        byte b2 = 0x03;
        String data = HfrdApi.fastRead(deviceId, b, b2);
        System.out.println("data: " + data + " addr: " + b);
        //}
        boolean result = HfrdApi.close(deviceId);
        Assertions.assertTrue(result);
    }
}