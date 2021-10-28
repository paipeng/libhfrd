package com.paipeng.libhfrd;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.win32.StdCallLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HfrdApi {

    public static Logger logger = LoggerFactory.getLogger(HfrdApi.class);
    private static final String LIB_NAME = "hfrdapi";

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
    }

    public static long connect(long deviceId) {
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

    public static boolean close(long deviceId) {
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

    public static String getVersion(long deviceId) {
        String version;
        int[] v = new int[3];

        deviceId = connect(deviceId);
        if (deviceId >= 0) {
            HrfdLib.INSTANCE.Sys_GetLibVersion(v);
            logger.trace("version: " + v[0] + "." + v[1] + "." + v[2]);
            version = v[0] + "." + v[1] + "." + v[2];
            close(deviceId);
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
     * @param deviceId
     * @param color
     */
    public static void changeLED(long deviceId, LED color, boolean close) {
        deviceId = connect(deviceId);
        if (deviceId >= 0) {
            HrfdLib.INSTANCE.Sys_SetLight(deviceId, (byte) color.ordinal());
            if (close) {
                close(deviceId);
            }
        }
    }

    public static void beep(long deviceId) {
        int status = 0;
        deviceId = connect(deviceId);
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

    public static String requestCard(long deviceId) {
        String sn = null;
        int status = 0;
        byte mode = 0x52;
        short[] TagType = new short[1];

        byte bcnt = 0;
        byte snr[] = new byte[16];
        byte len[] = new byte[1];
        byte sak[] = new byte[1];

        deviceId = connect(deviceId);
        if (deviceId >= 0) {
            while (true) {
                changeLED(deviceId, LED.LED_RED, false);
                //Request card
                try {
                    status = HrfdLib.INSTANCE.TyA_Request(deviceId, mode, TagType);//search all card
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
                if (status != 0) {
                    logger.error("TyA_Request failed: " + status);
                    changeLED(deviceId, LED.LED_ORANGE, false);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    logger.trace("tagType: " + TagType[0]);

                    if (TagType[0] == 68) {
                        sn = readNTAGSerialNumber(deviceId);
                        if (sn != null) {
                            beep(deviceId);
                            changeLED(deviceId, LED.LED_GREEN, false);
                        }
                        return sn;
                    } else {
                        //Anticollision
                        status = HrfdLib.INSTANCE.TyA_Anticollision(deviceId, bcnt, snr, len);//return serial number of card
                        if (status != 0 || len[0] != 4) {
                            changeLED(deviceId, LED.LED_ORANGE, false);
                            logger.error("TyA_Anticollision failed !");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                        } else {
                            logger.trace("anticollision len: " + len[0]);
                            beep(deviceId);
                            changeLED(deviceId, LED.LED_GREEN, false);
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
            }
        }

        return sn;
    }
}
