/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc.common;

/**
 * Utility class that defines constant values used across the application.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.1.0
 */
public class Constant {
  private Constant() {
    // Prevent instantiation
  }

  //	Formats
  public static final String PROPERTY_FORMAT = "%s#%s";

  //	Special characters
  public static final String COMMA = ",";

  //	Values
  public static final byte COMMAND_HEADER = (byte) 0xAA;
  public static final String NOT_AVAILABLE = "N/A";
  public static final String NONE = "None";
  public static final String ON = "On";
  public static final String OFF = "Off";
  public static final char ACK = 'A';
  public static final char NAK = 'N';

  //	Groups
  public static final String GENERAL_GROUP = "General";
  public static final String ADAPTER_METADATA_GROUP = "AdapterMetadata";

  //  Info messages
  public static final String INITIALIZED_SUCCESSFULLY_INFO = "SamsungMDCDevice initialized successfully";
  public static final String DESTROY_INTERNAL_INFO = "Destroying internal state of instance: ";

  //  Warn message
  public static final String PARAM_NULL_WARNING = "%s is null, returning None text";
  public static final String UNSUPPORTED_MAP_PROPERTY_WARNING = "Unsupported %s with property %s";
  public static final String CONTROLLABLE_PROPS_EMPTY_WARNING = "ControllableProperties list is null or empty, skipping control operation";
  public static final String POWER_CONTROL_NULL_WARNING = "The power control is Null, ignore power generation control";
  public static final String INPUT_SOURCE_NULL_WARNING = "The input source is Null with %s, ignore input generation control";
  public static final String STATUS_CONTROL_NULL_WARNING = "The status control is null, returning empty map";
  public static final String HISTORICAL_PROPS_EMPTY_WARNING = "The historical properties is empty, returning empty map.";

  //  Fail messages
  public static final String READ_PROPERTIES_FILE_FAILED = "Failed to load properties file: ";
  public static final String SET_UP_DATA_FAILED_1 = "Failed to set up data for ";
  public static final String SET_UP_DATA_FAILED_2 = "Failed to set up data for some reasons, returning the empty list";
  public static final String SET_UP_DATA_FAILED_3 = "Failed to set up data for some reasons, returning the false value";
  public static final String MAP_ELAPSED_TIME_FAILED = "Failed to mapToElapsedTime with uptime: ";
  public static final String CONTROL_PROPERTY_FAILED = "Failed to control property: ";
  public static final String SET_INPUT_FAILED = "Failed to set input source %s, may be it's not supported";
}
