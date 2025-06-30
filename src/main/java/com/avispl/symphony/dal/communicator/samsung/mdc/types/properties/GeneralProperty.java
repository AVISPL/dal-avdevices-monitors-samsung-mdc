/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc.types.properties;

/**
 * Enum representing properties related to a general.
 * Each property corresponds to a display name or configuration aspect of the general.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.2.0
 */
public enum GeneralProperty {
	FAN("Fan"),
	INPUT("Input"),
	LAMP("Lamp"),
	POWER("Power"),
	SYNC("Sync"),
	TEMPERATURE("Temperature(C)"),
	TEMPERATURE_STATUS("TemperatureStatus");

	private final String name;

	GeneralProperty(String name) {
		this.name = name;
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}
}
