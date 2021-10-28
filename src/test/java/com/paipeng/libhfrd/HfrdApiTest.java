package com.paipeng.libhfrd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HfrdApiTest {

    @Test
    public void testConnect() {
        if (HfrdApi.connect(-1L)) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            HfrdApi.close(-1L);
        }
    }

}