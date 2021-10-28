package com.paipeng.libhfrd;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HfrdApi {

    public static Logger logger = LoggerFactory.getLogger(HfrdApi.class);
    private static final String LIB_NAME = "hfrdapi";

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
    }

    public static boolean connect(long deviceId) {
        int status;
        boolean bStatus;
        long[] deviceIds = new long[1];

        //=================== Connect the reader ===================
        //Check whether the reader is connected or not
        //If the reader is already open , close it firstly
        bStatus = HrfdLib.INSTANCE.Sys_IsOpen(deviceId);
        if (bStatus == true) {
            /*
            deviceIds[0] = deviceId;
            status = HrfdLib.INSTANCE.Sys_Close(deviceIds);
            if(status != 0)
            {
                return null;
            }

             */

            return true;
        } else {
            //Connect
            status = HrfdLib.INSTANCE.Sys_Open(deviceIds, 0, (short) 0x0416, (short) 0x8020);
            if (status != 0) {
                logger.error("open device error: " + status);
                return false;
            }
        }

        //========== Init the reader before operating the card ==========
        //Close antenna of the reader
        status = HrfdLib.INSTANCE.Sys_SetAntenna(deviceId, (byte) 0);
        if (status != 0) {
            logger.trace("Sys_SetAntenna failed !");
            return false;
        }
        //Appropriate delay after Sys_SetAntenna operating
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }

        //Set the reader's working mode
        status = HrfdLib.INSTANCE.Sys_InitType(deviceId, (byte) 'A');
        if (status != 0) {
            logger.trace("Sys_InitType failed !");
            return false;
        }
        //Appropriate delay after Sys_SetAntenna operating
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }

        //Open antenna of the reader
        status = HrfdLib.INSTANCE.Sys_SetAntenna(deviceId, (byte) 1);
        if (status != 0) {
            logger.trace("Sys_SetAntenna failed !");
            return false;
        }
        //Appropriate delay after Sys_SetAntenna operating
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }

        //==================== Success Tips ====================
        //Beep 200 ms
        status = HrfdLib.INSTANCE.Sys_SetBuzzer(deviceId, (byte) 20);
        if (status != 0) {
            logger.trace("Sys_SetBuzzer failed !");
            return false;
        }

        //Tips
        logger.trace("Connect reader succeed !");

        return true;
    }

    public static boolean close(long deviceId) {
        int status;
        boolean bStatus;
        long[] deviceIds = new long[1];

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
        int status;
        boolean bStatus;
        long[] deviceIds = new long[1];
        String version;
        if (connect(deviceId)) {
            version = "";
        } else {
            version = null;
        }

        return version;
    }
}
