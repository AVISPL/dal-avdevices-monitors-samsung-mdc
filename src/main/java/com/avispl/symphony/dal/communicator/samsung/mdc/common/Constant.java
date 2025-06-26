/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc.common;

/**
 * Utility class that defines constant values used across the application.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.2.0
 */
public class Constant {
  private Constant() {
    // Prevent instantiation
  }

  public static final String COMMA = ",";

  public static final byte COMMAND_HEADER = (byte) 0xAA;
  public static final String NOT_AVAILABLE = "N/A";
  public static final String ON = "On";
  public static final String OFF = "Off";
}
