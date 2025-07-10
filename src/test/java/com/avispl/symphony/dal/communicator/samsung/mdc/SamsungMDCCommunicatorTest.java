/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.InputSource;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.properties.AdapterMetadataProperty;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.properties.GeneralProperty;


/**
 * Unit tests for the {@code SamsungMDCDevice} class.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.2.0
 */
class SamsungMDCCommunicatorTest {
	private ExtendedStatistics extendedStatistics;
	private SamsungMDCDevice communicator;

	@BeforeEach
	void setUp() throws Exception {
		this.communicator = new SamsungMDCDevice();
		this.communicator.setHost("");
		this.communicator.setPort(1515);
		this.communicator.setLogin("");
		this.communicator.setPassword("");
		this.communicator.init();
		this.communicator.connect();
	}

	@AfterEach
	void destroy() throws Exception {
		this.communicator.disconnect();
		this.communicator.destroy();
	}

	@Test
	void testGetMultipleStatistics() throws Exception {
		this.communicator.setHistoricalProperties(GeneralProperty.TEMPERATURE.getName());
		this.extendedStatistics = (ExtendedStatistics) this.communicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = this.extendedStatistics.getStatistics();
		int expectedStatisticSize = GeneralProperty.values().length + AdapterMetadataProperty.values().length;

		Assertions.assertEquals(statistics.size(), expectedStatisticSize, "Statistics have unexpected number of properties");
	}

	@Test
	void testControlInputWithSupportedInput() throws Exception {
		this.extendedStatistics = (ExtendedStatistics) this.communicator.getMultipleStatistics().get(0);

		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(GeneralProperty.INPUT.getName());
		controllableProperty.setValue(InputSource.HDMI1.getName());
		this.communicator.controlProperty(controllableProperty);

		this.extendedStatistics = (ExtendedStatistics) this.communicator.getMultipleStatistics().get(0);
		AdvancedControllableProperty comparedControllableProperty = this.extendedStatistics.getControllableProperties().stream()
				.filter(property -> property.getName().equals(GeneralProperty.INPUT.getName())).findFirst().orElse(null);

		Assertions.assertNotNull(comparedControllableProperty, "ComparedControllableProperty is null");
		Assertions.assertEquals(controllableProperty.getValue(), comparedControllableProperty.getValue(), "ComparedControllableProperty have unexpected value");
	}

	@Test
	void testControlInputWithUnsupportedInput() throws Exception {
		this.extendedStatistics = (ExtendedStatistics) this.communicator.getMultipleStatistics().get(0);
		InputSource currentInput = InputSource.getByName(this.extendedStatistics.getStatistics().get(GeneralProperty.INPUT.getName()));

		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(GeneralProperty.INPUT.getName());
		controllableProperty.setValue(InputSource.AV1_AV.getName());
		Assertions.assertThrows(UnsupportedOperationException.class, () -> this.communicator.controlProperty(controllableProperty));

		this.extendedStatistics = (ExtendedStatistics) this.communicator.getMultipleStatistics().get(0);
		AdvancedControllableProperty comparedControllableProperty = this.extendedStatistics.getControllableProperties().stream()
				.filter(property -> property.getName().equals(GeneralProperty.INPUT.getName())).findFirst().orElse(null);
		Assertions.assertNotNull(comparedControllableProperty, "ComparedControllableProperty is null");
		Assertions.assertEquals(currentInput.getName(), comparedControllableProperty.getValue(), "ComparedControllableProperty have unexpected value");
	}
}
