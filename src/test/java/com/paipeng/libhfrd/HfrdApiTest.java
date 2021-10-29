package com.paipeng.libhfrd;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HfrdApiTest {
    public static Logger logger = LoggerFactory.getLogger(HfrdApiTest.class);

    @Test
    public void testConnect() {
        long deviceId = HfrdApi.connect();
        Assertions.assertTrue(deviceId >= 0);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        boolean result = HfrdApi.close();
        Assertions.assertTrue(result);
    }

    @Test
    void getVersion() {
        String version = HfrdApi.getVersion();
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
        byte color = 2;
        // color 0: LED OFF
        // color 1: LED ON RED
        // color 2: LED ON GREEN
        // color 3: LED ON ORANGE (RED/YELLOW)
        HfrdApi.changeLED(HfrdApi.LED.LED_RED, true);
    }

    @Test
    void requestCard() {
        String serialNumber = HfrdApi.requestCard();
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
        String serialNumber = HfrdApi.requestCard();
        if (serialNumber == null) {
            logger.error("read requestCard error");
            return;
        } else {
            // HfrdApi.beep(deviceId);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            // NTAG 213 0x00 - 0x2C
            // NTAG 215 0x00 - 0x86
            // NTAG 216 0x00 - 0xE6
            byte b = 0x04;
            String data = HfrdApi.read(b);
            System.out.println("data: " + data + " addr: " + b);
        }
        boolean result = HfrdApi.close();
        Assertions.assertTrue(result);
    }

    @Test
    void writeData() {
        byte[] payload = new byte[17];
        int len = 0;
        payload[len++] = (byte)0xD1;
        payload[len++] = (byte)0x01;
        payload[len++] = (byte)0x0D;
        payload[len++] = (byte)0x54;
        payload[len++] = (byte)0x02;

        payload[len++] = (byte)0x7A;
        payload[len++] = (byte)0x68;


        payload[len++] = (byte)0x30;
        payload[len++] = (byte)0x31;
        payload[len++] = (byte)0x32;
        payload[len++] = (byte)0x33;
        payload[len++] = (byte)0x34;
        payload[len++] = (byte)0x35;
        payload[len++] = (byte)0x36;
        payload[len++] = (byte)0x37;
        payload[len++] = (byte)0x38;
        payload[len++] = (byte)0x39;

        String serialNumber = HfrdApi.requestCard();
        if (serialNumber != null) {
            HfrdApi.writeData(payload);
        }

    }

    @Test
    void testWriteData() {
    }

    @Test
    void fastRead() {
        String serialNumber = HfrdApi.requestCard();
        logger.trace("serialNumber: " + serialNumber);
        if (serialNumber == null) {
            logger.error("read requestCard error");
            return;
        } else {
            // NTAG 213 0x2C  error 23
            // NTAG 215 0x86  error
            // NTAG 216 0xE6  error
            //for (byte b = -127; b < 128; b++) {
            byte b = 0x00;
            byte b2 = 0x0F;
            // max size: 16 blocks (4 bytes/block)
            String data = HfrdApi.fastRead(b, b2);
            System.out.println("data: " + data + " addr: " + b);
        }
        boolean result = HfrdApi.close();
        Assertions.assertTrue(result);
    }

    @Test
    void readCount() {
        int readCount = HfrdApi.readCount();
        logger.trace("readCount: " + readCount);
    }

    @Test
    void readSignature() {
        int readCount = HfrdApi.readSignature();
        logger.trace("readSignature: " + readCount);
    }

    @Test
    void validatePassword() {
        byte[] password = new byte[4];
        password[0] = (byte) 0xFF;
        password[1] = (byte) 0xFF;
        password[2] = (byte) 0xFF;
        password[3] = (byte) 0xFF;
        boolean readCount = HfrdApi.validatePassword(password);
        logger.trace("validatePassword: " + readCount);
    }
}