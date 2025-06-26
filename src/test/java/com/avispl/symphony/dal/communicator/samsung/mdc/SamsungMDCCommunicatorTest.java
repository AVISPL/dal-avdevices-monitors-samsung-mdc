/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;


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
		this.extendedStatistics = (ExtendedStatistics) this.communicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = this.extendedStatistics.getStatistics();
		System.out.println(statistics);
	}
}
