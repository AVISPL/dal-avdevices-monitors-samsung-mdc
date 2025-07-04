/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc.types;

import java.util.EnumSet;

/**
 * Enum representing various status codes returned by the device.
 * Each status includes a description and a corresponding byte value used in communication.
 * <br>
 * Note: Some statuses may share the same byte value but differ in context or meaning.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.1.0
 */
public enum StatusCode {
	NORMAL("Normal", (byte) 0x00),
	ERROR("Error", (byte) 0x01),
	ERROR_NO_SYNC("Error, No Sync", (byte) 0x01),
	NONE("None", (byte) 0x02);

	/**
	 * A predefined set of common status codes used for general device status properties.
	 */
	private static final EnumSet<StatusCode> COMMON_STATUS = EnumSet.of(NORMAL, ERROR, NONE);
	/**
	 * A predefined set of status codes specifically used for the "noSync" property.
	 */
	private static final EnumSet<StatusCode> NO_SYNC_STATUS = EnumSet.of(NORMAL, ERROR_NO_SYNC, NONE);

	private final String name;
	private final byte code;

	StatusCode(String name, byte code) {
		this.name = name;
		this.code = code;
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the corresponding {@link StatusCode} based on the given property name and byte code.
	 * <p>
	 * If the property is "noSync", the method will search within {@code NO_SYNC_STATUS}.
	 * Otherwise, it searches in {@code COMMON_STATUS}.
	 * </p>
	 *
	 * @param property the name of the status control property (e.g., "noSync", "lamp", etc.)
	 * @param code the byte code representing the status
	 * @return the matching {@code StatusCode}, or {@code null} if no match is found
	 */
	public static StatusCode getByStatusControlProperty(String property, byte code) {
		return property.equals("noSync")
				? NO_SYNC_STATUS.stream().filter(statusCode -> statusCode.code == code).findFirst().orElse(null)
				: COMMON_STATUS.stream().filter(statusCode -> statusCode.code == code).findFirst().orElse(null);
	}
}
