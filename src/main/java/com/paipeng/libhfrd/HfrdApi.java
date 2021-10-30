package com.paipeng.libhfrd;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.win32.StdCallLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HfrdApi {

    public static Logger logger = LoggerFactory.getLogger(HfrdApi.class);
    private static final String LIB_NAME = "hfrdapi";
    public static long deviceId = -1;

    private static void setupNativeLibrary() {
        logger.trace("setupNativeLibrary");
        NativeLibrary.addSearchPath(LIB_NAME, "./libs");
        //System.setProperty("jna.library.path", "");
        //NativeLibrary.addSearchPath(LIB_NAME, "");
    }

    public enum LED {
        LED_OFF,
        LED_RED,
        LED_GREEN,
        LED_ORANGE
    }

    static {
        setupNativeLibrary();
    }

    //Load DLL Library
    public interface HrfdLib extends StdCallLibrary {
        HrfdLib INSTANCE = Native.load(LIB_NAME, HrfdLib.class);

        int Sys_GetLibVersion(int[] version);

        int Sys_Open(long[] device, int index, short vid, short pid);

        int Sys_Close(long[] device);

        boolean Sys_IsOpen(long device);

        int Sys_SetLight(long device, byte color);

        int Sys_SetBuzzer(long device, byte msec);

        int Sys_SetAntenna(long device, byte mode);

        int Sys_InitType(long device, byte type);

        int TyA_Request(long device, byte mode, short[] pTagType);

        int TyA_Anticollision(long device, byte bcnt, byte[] pSnr, byte[] pLen);

        int TyA_Select(long device, byte[] pSnr, byte snrLen, byte[] pSak);

        int TyA_Halt(long device);

        int TyA_CS_Authentication2(long device, byte mode, byte block, byte[] pKey);

        int TyA_CS_Read(long device, byte block, byte[] pData, byte[] pLen);

        int TyA_CS_Write(long device, byte block, byte[] pData);

        int TyA_UID_Write(long device, byte[] pData);


        // NTAG
        int TyA_NTAG_AnticollSelect(long device, byte[] pSnr, byte[] pLen);

        int TyA_NTAG_GetVersion(long device, byte[] pData, byte[] pLen);

        int TyA_NTAG_Read(long device, byte addr, byte[] pData, byte[] pLen);

        int TyA_NTAG_FastRead(long device, byte startAddr, byte endAddr, byte[] pData, byte[] pLen);

        int TyA_NTAG_Write(long device, byte addr, byte[] pdata);

        int TyA_NTAG_CompWrite(long device, byte addr, byte[] pData);

        int TyA_NTAG_ReadCnt(long device, byte addr, byte[] pData, byte[] pLen);

        int TyA_NTAG_PwdAuth(long device, byte[] pPwd, byte[] pData, byte[] pLen);

        int TyA_NTAG_ReadSig(long device, byte addr, byte[] pData, byte[] pLen);

        int TyA_UL_ReadCnt(long device, byte addr,byte[] pData, byte[] pLen);


    }

    public static long connect() {
        int status;
        boolean bStatus;
        long[] deviceIds = new long[]{-1};
        // long g_hDevice[] = new long[]{-1}; //hDevice must init as -1
        deviceIds[0] = deviceId;

        //=================== Connect the reader ===================
        //Check whether the reader is connected or not
        //If the reader is already open , close it firstly
        bStatus = HrfdLib.INSTANCE.Sys_IsOpen(deviceId);
        if (bStatus == true) {
            logger.trace("is opened");
            return deviceId;
        } else {
            logger.trace("opening...");
            //Connect
            status = HrfdLib.INSTANCE.Sys_Open(deviceIds, 0, (short) 0x0416, (short) 0x8020);
            if (status != 0) {
                logger.error("open device error: " + status);
                return deviceIds[0];
            }
        }

        logger.trace("deviceId: " + deviceIds[0]);

        //========== Init the reader before operating the card ==========
        //Close antenna of the reader
        status = HrfdLib.INSTANCE.Sys_SetAntenna(deviceIds[0], (byte) 0);
        if (status != 0) {
            logger.trace("Sys_SetAntenna failed !");
            return deviceIds[0];
        }
        //Appropriate delay after Sys_SetAntenna operating
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }

        //Set the reader's working mode
        status = HrfdLib.INSTANCE.Sys_InitType(deviceIds[0], (byte) 'A');
        if (status != 0) {
            logger.trace("Sys_InitType failed !");
            return deviceIds[0];
        }
        //Appropriate delay after Sys_SetAntenna operating
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }

        //Open antenna of the reader
        status = HrfdLib.INSTANCE.Sys_SetAntenna(deviceIds[0], (byte) 1);
        if (status != 0) {
            logger.trace("Sys_SetAntenna failed !");
            return deviceIds[0];
        }
        //Appropriate delay after Sys_SetAntenna operating
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }

        //==================== Success Tips ====================
        //Beep 200 ms
        /*
        status = HrfdLib.INSTANCE.Sys_SetBuzzer(deviceIds[0], (byte) 20);
        if (status != 0) {
            logger.trace("Sys_SetBuzzer failed !");
            return  deviceIds[0];
        }

         */

        //Tips
        logger.trace("Connect reader succeed !");

        return deviceIds[0];
    }

    public static boolean close() {
        int status;
        boolean bStatus;
        long[] deviceIds = new long[]{-1};
        deviceIds[0] = deviceId;

        //=================== Connect the reader ===================
        //Check whether the reader is connected or not
        //If the reader is already open , close it firstly
        bStatus = HrfdLib.INSTANCE.Sys_IsOpen(deviceId);
        if (bStatus == true) {
            deviceIds[0] = deviceId;
            status = HrfdLib.INSTANCE.Sys_Close(deviceIds);
            if (status != 0) {
                logger.error("close connect failed: " + status);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static String getVersion() {
        String version;
        int[] v = new int[3];

        deviceId = connect();
        if (deviceId >= 0) {
            HrfdLib.INSTANCE.Sys_GetLibVersion(v);
            logger.trace("version: " + v[0] + "." + v[1] + "." + v[2]);
            version = v[0] + "." + v[1] + "." + v[2];
            close();
        } else {
            version = null;
        }

        return version;
    }

    /**
     * // color 0: LED OFF
     * // color 1: LED ON RED
     * // color 2: LED ON GREEN
     * // color 3: LED ON ORANGE (RED/YELLOW)
     *
     * @param color
     */
    public static void changeLED(LED color, boolean close) {
        deviceId = connect();
        if (deviceId >= 0) {
            HrfdLib.INSTANCE.Sys_SetLight(deviceId, (byte) color.ordinal());
            if (close) {
                close();
            }
        }
    }

    public static void beep() {
        int status = 0;
        deviceId = connect();
        if (deviceId >= 0) {
            //==================== Success Tips ====================
            //Beep 200 ms 20
            // 400 ms -> 40
            status = HrfdLib.INSTANCE.Sys_SetBuzzer(deviceId, (byte) 40);
            if (status != 0) {
                logger.trace("Sys_SetBuzzer failed !");
            }
        }
    }

    public static String requestCard() {
        String sn = null;
        int status = 0;
        byte mode = 0x52;
        short[] TagType = new short[1];

        byte bcnt = 0;
        byte snr[] = new byte[16];
        byte len[] = new byte[1];
        byte sak[] = new byte[1];

        deviceId = connect();
        if (deviceId >= 0) {
            while (true) {
                changeLED(LED.LED_RED, false);
                //Request card
                try {
                    status = HrfdLib.INSTANCE.TyA_Request(deviceId, mode, TagType);//search all card
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
                if (status != 0) {
                    logger.error("TyA_Request failed: " + status);
                    changeLED(LED.LED_ORANGE, false);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    logger.trace("tagType: " + TagType[0]);

                    if (TagType[0] == 68) {
                        sn = readNTAGSerialNumber(deviceId);
                        if (sn != null) {
                            //beep();
                            changeLED(LED.LED_GREEN, false);
                        }
                        return sn;
                    } else {
                        //Anticollision
                        status = HrfdLib.INSTANCE.TyA_Anticollision(deviceId, bcnt, snr, len);//return serial number of card
                        if (status != 0 || len[0] != 4) {
                            changeLED(LED.LED_ORANGE, false);
                            logger.error("TyA_Anticollision failed !");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                        } else {
                            logger.trace("anticollision len: " + len[0]);
                            //beep(deviceId);
                            changeLED(LED.LED_GREEN, false);
                            String str = "";
                            for (int i = 0; i < (int) len[0]; i++) {
                                str = str + String.format("%02X", snr[i]);
                            }
                            sn = str;
                            break;
                        }
                    }
                }
            }
        }

        return sn;
    }


    public static String readNTAGSerialNumber(long deviceId) {
        String sn = null;
        int status = 0;
        byte mode = 0x52;
        short[] TagType = new short[1];

        byte bcnt = 0;
        byte snr[] = new byte[16];
        byte len[] = new byte[1];
        byte sak[] = new byte[1];
        byte data[] = new byte[32];


        // deviceId = connect(deviceId);
        if (deviceId >= 0) {
            status = HrfdLib.INSTANCE.TyA_NTAG_AnticollSelect(deviceId, snr, len);
            if (status != 0) {
                logger.error("read TyA_NTAG_AnticollSelect error: " + status);
                return null;
            } else {
                logger.trace("len: " + len[0]);
                String str = "";
                for (int i = 0; i < (int) len[0]; i++) {
                    str = str + String.format("%02X", snr[i]);
                }
                sn = str;
                status = HrfdLib.INSTANCE.TyA_NTAG_GetVersion(deviceId, data, len);
                if (status != 0) {
                    logger.error("read TyA_NTAG_GetVersion error: " + status);
                } else {
                    str = "";
                    for (int i = 0; i < (int) len[0]; i++) {
                        str = str + String.format("%02X", data[i]);
                    }
                    logger.trace("NTAG version: " + str);
                }
            }
        }
        return sn;
    }

    public static String read(byte addr) {
        String dataString = null;
        byte[] data = new byte[16];
        byte[] len = new byte[1];
        int status;
        if (deviceId >= 0) {
            status = HrfdLib.INSTANCE.TyA_NTAG_Read(deviceId, addr, data, len);
            if (status != 0) {
                logger.error("TyA_NTAG_Read error: " + status);
            } else {
                logger.trace("len: " + len[0]);
                String str = "";
                for (int i = 0; i < (int) len[0]; i++) {
                    str = str + String.format("%02X", data[i]);
                }
                dataString = str;
            }
        }
        return dataString;
    }


    public static String fastRead(byte startAddr, byte stopAddr) {
        String dataString = null;
        byte[] data = new byte[4 * (stopAddr - startAddr + 1)];
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
        byte[] len = new byte[1];
        int status;
        if (deviceId >= 0) {
            status = HrfdLib.INSTANCE.TyA_NTAG_FastRead(deviceId, startAddr, stopAddr, data, len);
            if (status != 0) {
                logger.error("TyA_NTAG_Read error: " + status);
            } else {
                logger.trace("len: " + len[0]);
                String str = "";
                for (int i = 0; i < (int) len[0]; i++) {
                    str = str + String.format("%02X", data[i]);
                    if (i > 0 && (i+1) % 4 == 0) {
                        str = str + " ";
                    }
                }
                dataString = str;
            }
        }
        return dataString;
    }


    public static boolean writeData(String data) {
        return false;
    }

    /**
     *
     * @param payload
     * @return
     */
    public static boolean writeData(byte[] payload) {
        byte[] data = new byte[payload.length + 7 + 1];
        byte[] writeBuffer = new byte[4];
        byte pageAddr;
        int data_len = 0;
        int status;
        data[data_len++] = 0x01; //The factory default data of NTAG203
        data[data_len++] = 0x03; //The factory default data of NTAG203
        data[data_len++] = (byte)0xA0; //The factory default data of NTAG203
        data[data_len++] = 0x10; //The factory default data of NTAG203
        data[data_len++] = 0x44; //The factory default data of NTAG203
        data[data_len++] = 0x03;
        data[data_len++] = (byte)payload.length;

        for (int i = 0; i < payload.length; i++) {
            data[data_len++] = payload[i];
        }

        // ending with 0xFE
        data[data_len++] = (byte) 0xFE;

        for (int i = 0; i < data_len; i++) {
            logger.trace(String.format("idx: %d -> %02X", i, data[i]));
        }
        // NTAG 213
        if (data_len > 144) {
            logger.error("data size too big!");
        }

        // WRITE
        for (int i = 0; i < data_len; i += 4) {
            pageAddr = (byte)(4+i/4); // begin to write from Page 4
            int len = 4;
            if ((i+4) > data_len) {
                len = data_len - i;
            }
            logger.trace("write to page " + pageAddr + " len: " + len);
            //System.arraycopy(writeBuffer, 0, data, i, (byte)len);
            for (int j = 0; j < len; j++) {
                writeBuffer[j] = data[i+j];
            }
            logger.trace(String.format("wirte data byte: %02X %02X %02X %02X", writeBuffer[0], writeBuffer[1], writeBuffer[2], writeBuffer[3]));
            status = HrfdLib.INSTANCE.TyA_NTAG_Write(deviceId, pageAddr, writeBuffer);
            if (status != 0) {
                logger.error("TyA_NTAG_Write error: " + status);
            } else {
                logger.trace("TyA_NTAG_Write success");
            }
        }
        return false;
    }

    public static int readCount() {
        byte[] data = new byte[3];
        byte[] len = new byte[1];
        int status;
        String serialNumber = HfrdApi.requestCard();
        logger.trace("serialNumber: " + serialNumber);
        if (serialNumber == null) {
            logger.error("read requestCard error");
            return -1;
        } else {
            //status = HrfdLib.INSTANCE.TyA_NTAG_ReadCnt(deviceId, (byte)0x02, data, len);
            status = HrfdLib.INSTANCE.TyA_UL_ReadCnt(deviceId, (byte)0x00, data, len);

            if (status != 0) {
                logger.error("TyA_NTAG_ReadCnt error: " + status);
                return -2;
            } else {
                logger.trace("len: " + len[0]);
                String str = "";
                for (int i = 0; i < (int) len[0]; i++) {
                    str = str + String.format("%02X", data[i]);
                }
                logger.trace("count: " + str);
            }
            return 0;
        }
    }

    /**
     * ECC signature
     */
    public static int readSignature() {
        byte[] data = new byte[32];
        int status;
        byte[] len = new byte[1];
        String serialNumber = HfrdApi.requestCard();
        logger.trace("serialNumber: " + serialNumber);
        if (serialNumber == null) {
            logger.error("read requestCard error");
            return -1;
        } else {
            status = HrfdLib.INSTANCE.TyA_NTAG_ReadSig(deviceId, (byte)0x00, data, len);
            if (status != 0) {
                logger.error("TyA_NTAG_ReadSig error: " + status);
                return -2;
            } else {
                logger.trace("len: " + len[0]);
                String str = "";
                for (int i = 0; i < (int) len[0]; i++) {
                    str = str + String.format("%02X", data[i]);
                    if (i > 0 && (i+1) % 4 == 0) {
                        str = str + " ";
                    } else {
                        str = str + "-";
                    }
                }
                logger.trace("signature: " + str);
                return 0;
            }
        }
    }

    public static boolean validatePassword(byte[] password) {
        byte[] data = new byte[32];
        int status;
        byte[] len = new byte[1];
        String serialNumber = HfrdApi.requestCard();
        logger.trace("serialNumber: " + serialNumber);
        if (serialNumber == null) {
            logger.error("read requestCard error");
            return false;
        } else {
            status = HrfdLib.INSTANCE.TyA_NTAG_PwdAuth(deviceId, password, data, len);
            if (status != 0) {
                logger.error("TyA_NTAG_PwdAuth error: " + status);
                return false;
            } else {
                logger.trace("len: " + len[0]);
                String str = "";
                for (int i = 0; i < (int) len[0]; i++) {
                    str = str + String.format("%02X", data[i]);
                    if (i > 0 && (i+1) % 4 == 0) {
                        str = str + " ";
                    } else {
                        str = str + "-";
                    }
                }
                logger.trace("pass data: " + str);
                return true;
            }
        }
    }

}
