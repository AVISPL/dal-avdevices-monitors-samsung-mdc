/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc.types;

/**
 * Enum representing command types used to interact with the device.
 * Each command corresponds to a specific operation or control signal sent to the device.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.1.0
 */
public enum Command {
	STATUS((byte) 0x0D),
	INPUT_SOURCE((byte) 0x14),
	POWER((byte) 0x11);

	private final byte code;

	Command(byte code) {
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
}
