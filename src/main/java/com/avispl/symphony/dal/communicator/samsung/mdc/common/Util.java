/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.avispl.symphony.dal.communicator.samsung.mdc.models.StatusControl;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.InputSource;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.PowerControl;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.properties.AdapterMetadataProperty;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.properties.GeneralProperty;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * Utility class for this adapter. This class includes helper methods.
 * <p>This class is non-instantiable and provides only static utility methods.</p>
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.1.0
 */
public class Util {
    private static final Log LOGGER = LogFactory.getLog(Util.class);

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

    /**
     * Maps the given {@link PowerControl} state to a general property string.
     *
     * @param powerControl the power control state to map
     * @return the mapped value, or {@code Constant.NOT_AVAILABLE} if input is null
     */
    public static String mapToGeneralProperty(PowerControl powerControl) {
        if (powerControl == null) {
            LOGGER.warn(String.format(Constant.PARAM_NULL_WARNING, "powerControl"));
            return Constant.NOT_AVAILABLE;
        }

        return powerControl == PowerControl.ON ? "1" : "0";
    }

    /**
     * Maps a {@link GeneralProperty} and corresponding {@link StatusControl} to a string value representing the property's current state.
     *
     * @param property the general property to map
     * @param statusControl the status control object containing current statuses
     * @return a string representing the property's value or {@code Constant.NOT_AVAILABLE} if invalid
     */
    public static String mapToGeneralProperty(GeneralProperty property, StatusControl statusControl) {
        if (statusControl == null) {
            LOGGER.warn(String.format(Constant.PARAM_NULL_WARNING, "statusControl"));
            return Constant.NOT_AVAILABLE;
        }

        switch (property) {
            case LAMP:
                return mapToValue(statusControl.getLamp().getName());
            case TEMPERATURE_STATUS:
                return mapToValue(statusControl.getTemperature().getName());
            case SYNC:
                return mapToValue(statusControl.getNoSync().getName());
            case TEMPERATURE:
                return String.valueOf(statusControl.getCurrentTemperature());
            case FAN:
                return mapToValue(statusControl.getFan().getName());
            default: {
                LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToGeneralProperty", property));
                return Constant.NOT_AVAILABLE;
            }
        }
    }

    /**
     * Maps the given {@link InputSource} to its display name.
     *
     * @param inputSource the input source to map
     * @return the input source name, or {@code Constant.NOT_AVAILABLE} if null
     */
    public static String mapToGeneralProperty(InputSource inputSource) {
        if (inputSource == null) {
            LOGGER.warn(String.format(Constant.PARAM_NULL_WARNING, "inputSource"));
            return Constant.NOT_AVAILABLE;
        }

        return inputSource.getName();
    }

    /**
     * Maps the given {@link AdapterMetadataProperty} to its value using the provided properties.
     *
     * @param versionProperties the source of property values
     * @param property the property to map
     * @return the formatted value, or {@code Constant.NOT_AVAILABLE} if not available
     */
    public static String mapToAdapterMetadataProperty(Properties versionProperties, AdapterMetadataProperty property) {
        String adapterBuildDate = versionProperties.getProperty("adapter.build.date");
        String adapterUptime = versionProperties.getProperty("adapter.uptime");
        String adapterVersion = versionProperties.getProperty("adapter.version");

        switch (property) {
            case ADAPTER_BUILD_DATE:
                return mapToValue(adapterBuildDate);
            case ADAPTER_UPTIME:
                return mapToUptime(adapterUptime);
            case ADAPTER_UPTIME_MIN:
                return mapToUptimeMin(adapterUptime);
            case ADAPTER_VERSION:
                return mapToValue(adapterVersion);
            default:
                LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToAdapterMetadataProperty", property));
                return Constant.NOT_AVAILABLE;
        }
    }

    /**
     * Maps a string value to itself if not null or empty; otherwise returns null.
     */
    private static String mapToValue(String value) {
        return StringUtils.isNotNullOrEmpty(value) ? value : Constant.NOT_AVAILABLE;
    }

    /**
     * Returns the elapsed uptime between the current system time and the given timestamp in milliseconds.
     * <p>
     * The input timestamp represents the start time in milliseconds (typically from {@link System#currentTimeMillis()}).
     * The returned string represents the absolute duration in the format:
     * "X day(s) Y hour(s) Z minute(s) W second(s)", omitting any zero-value units except seconds.
     *
     * @param uptime the start time in milliseconds as a string (e.g., "1717581000000")
     * @return a formatted duration string like "2 day(s) 3 hour(s) 15 minute(s) 42 second(s)",
     * or {@link Constant#NOT_AVAILABLE} if parsing fails
     */
    private static String mapToUptime(String uptime) {
        try {
            if (StringUtils.isNullOrEmpty(uptime)) {
                return Constant.NOT_AVAILABLE;
            }

            long uptimeSecond = (System.currentTimeMillis() - Long.parseLong(uptime)) / 1000;
            long seconds = uptimeSecond % 60;
            long minutes = uptimeSecond % 3600 / 60;
            long hours = uptimeSecond % 86400 / 3600;
            long days = uptimeSecond / 86400;
            StringBuilder rs = new StringBuilder();
            if (days > 0) {
                rs.append(days).append(" day(s) ");
            }
            if (hours > 0) {
                rs.append(hours).append(" hour(s) ");
            }
            if (minutes > 0) {
                rs.append(minutes).append(" minute(s) ");
            }
            rs.append(seconds).append(" second(s)");

            return rs.toString().trim();
        } catch (Exception e) {
            LOGGER.error(Constant.MAP_TO_UPTIME_FAILED + uptime, e);
            return Constant.NOT_AVAILABLE;
        }
    }

    /**
     * Returns the elapsed uptime in **whole minutes** between the current system time and the given timestamp in milliseconds.
     * <p>
     * The input timestamp represents the start time in milliseconds (typically from {@link System#currentTimeMillis()}).
     * The returned string is the total number of minutes that have elapsed, excluding seconds.
     *
     * @param uptime the start time in milliseconds as a string (e.g., "1717581000000")
     * @return a string representing the total number of elapsed minutes (e.g., "125"),
     * or {@link Constant#NOT_AVAILABLE} if parsing fails
     */
    private static String mapToUptimeMin(String uptime) {
        try {
            if (StringUtils.isNullOrEmpty(uptime)) {
                return Constant.NOT_AVAILABLE;
            }

            long uptimeSecond = (System.currentTimeMillis() - Long.parseLong(uptime)) / 1000;
            long minutes = uptimeSecond / 60;

            return String.valueOf(minutes);
        } catch (Exception e) {
            LOGGER.error(Constant.MAP_TO_UPTIME_MIN_FAILED + uptime, e);
            return Constant.NOT_AVAILABLE;
        }
    }
}
