/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for this adapter. This class includes helper methods.
 * <p>This class is non-instantiable and provides only static utility methods.</p>
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.2.0
 */
public class Util {
    private Util() {
        // Prevent instantiation
    }

    /**
     * This method is used to calculate the checksum of a byte array
     * @param bytes This is the list of bytes against which the checksum should be calculated
     * @return byte This returns the calculated checksum.
     */
    public static byte checkSum(byte[] bytes) {
        int checksum = 0;

        for (byte aByte : bytes) {
            checksum = checksum + aByte & 0xFF;
        }

        return (byte) checksum;
    }

    /**
     * This method is used to build a string to be sent according to the NEC Protocol (See bellow)
     */
    public static byte[] buildSendString(byte monitorID, byte command) {
        return buildSendString(monitorID,command,null);
    }

    /**
     * This method is used to build a string to be sent according to the Samsung Protocol
     * @param monitorID This is byte representing the monitor ID
     * @param command This is the byte array reprensenting the command to be sent
     * @param param This is the byte array reprensenting the parameter values to be sent
     * @return byte[] This returns the string to be sent to the display
     */
    public static byte[] buildSendString(byte monitorID, byte command, byte[] param) {
        List<Byte> bytes = new ArrayList<>();

        bytes.add(command);
        bytes.add((byte)monitorID);

        if(param != null) {
            bytes.add((byte)param.length);
            for(byte b:param){
                bytes.add(b);
            }
        }else{
            bytes.add((byte)0x00);
        }

        byte[] message = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++) {
            message[i] = bytes.get(i);
        }

        bytes.add(checkSum(message));
        bytes.add(0, Constant.COMMAND_HEADER);
        bytes.add((byte)0x0D);
        bytes.add((byte)0x0A);

        byte[] byteArray = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++) {
            byteArray[i] = bytes.get(i);
        }

        return byteArray;
    }
}
