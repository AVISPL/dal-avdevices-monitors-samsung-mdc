/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc.types;

import java.util.Arrays;

/**
 * Enum representing power control states for the device.
 * Each state corresponds to a byte value used to turn the device on or off.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.2.0
 */
public enum PowerControl {
	OFF((byte) 0x00),
	ON((byte) 0x01);

	private final byte code;

	PowerControl(byte code) {
		this.code = code;
	}

	/**
	 * Retrieves {@link #code}
	 *
	 * @return value of {@link #code}
	 */
	public byte getCode() {
		return code;
	}

	/**
	 * Returns the {@link PowerControl} corresponding to the given byte code.
	 *
	 * @param code the byte code representing the power control state
	 * @return the matching {@code PowerControl}, or {@code null} if no match is found
	 */
	public static PowerControl getByCode(byte code) {
		return Arrays.stream(values()).filter(p -> p.code == code).findFirst().orElse(null);
	}
}
