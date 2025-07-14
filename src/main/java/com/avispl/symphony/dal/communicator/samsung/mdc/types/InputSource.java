/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc.types;

import java.util.Arrays;

import com.avispl.symphony.dal.communicator.samsung.mdc.common.Constant;

/**
 * Enum representing input source types available for the device.
 * Each input source includes a display name and its corresponding byte code used for identifying the source in MDC device.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.1.0
 */
public enum InputSource {
	UNDEFINED(Constant.NOT_AVAILABLE, (byte) 0x4E),
	S_VIDEO("S-Video", (byte) 0x04),
	COMPONENT("Component", (byte) 0x08),
	AV1_AV("AV", (byte) 0x0C),
	AV2("AV2", (byte) 0x0D),
	EXT_SCART1("Ext. (SCART1)", (byte) 0x0E),
	DVI("DVI", (byte) 0x18),
	PC("PC", (byte) 0x1A),
	BNC("BNC", (byte) 0x1E),
	DVI_VIDEO("DVI_VIDEO", (byte) 0x1F),
	MAGIC_INFO("MagicInfo", (byte) 0x20),
	HDMI1("HDMI1", (byte) 0x21),
	HDMI1_PC("HDMI1_PC", (byte) 0x22),
	HDMI2("HDMI2", (byte) 0x23),
	HDMI2_PC("HDMI2_PC", (byte) 0x24),
	DISPLAYPORT1("DisplayPort1", (byte) 0x25),
	DISPLAYPORT2("DisplayPort2", (byte) 0x26),
	DISPLAYPORT3("DisplayPort3", (byte) 0x27),
	RF_TV("TV", (byte) 0x30),
	HDMI3("HDMI3", (byte) 0x31),
	HDMI3_PC("HDMI3_PC", (byte) 0x32),
	HDMI4("HDMI4", (byte) 0x33),
	HDMI4_PC("HDMI4_PC", (byte) 0x34),
	TV_DTV("DTV", (byte) 0x40),
	PLUG_IN_MODULE("Plug-in Module", (byte) 0x50),
	HDBASE_T("HDBT", (byte) 0x55),
	MEDIA_MAGIC_INFO_S("MagicInfo Lite/S", (byte) 0x60),
	WIDI_SCREEN_MIRRORING("Screen Mirroring", (byte) 0x61),
	INTERNAL_USB("Internal/USB", (byte) 0x62),
	URL_LAUNCHER("URL Launcher", (byte) 0x63),
	IWB("MagicIWB S", (byte) 0x64),
	WEB_BROWSER("Web browser", (byte) 0x65),
	REMOTE_WORKSPACE("Remote Workspace", (byte) 0x66);

	private final String name;
	private final byte code;

	InputSource(String name, byte code) {
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
	 * Retrieves {@link #code}
	 *
	 * @return value of {@link #code}
	 */
	public byte getCode() {
		return code;
	}

	/**
	 * Returns the {@link InputSource} corresponding to the given byte code.
	 *
	 * @param code the byte code representing the input source
	 * @return the matching {@code InputSource}, or {@code null} if no match is found
	 */
	public static InputSource getByCode(byte code) {
		return Arrays.stream(values()).filter(i -> i.code == code).findFirst().orElse(UNDEFINED);
	}

	/**
	 * Returns the {@link InputSource} corresponding to the given byte code.
	 *
	 * @param name the name representing the input source
	 * @return the matching {@code InputSource}, or {@code null} if no match is found
	 */
	public static InputSource getByName(String name) {
		return Arrays.stream(values()).filter(i -> i.name.equals(name)).findFirst().orElse(UNDEFINED);
	}

	/**
	 * Returns an array of all input source names.
	 *
	 * @return a String array containing the names of all {@link InputSource} values
	 */
	public static String[] getNames() {
		return Arrays.stream(values())
				.filter(inputSource -> !inputSource.equals(UNDEFINED))
				.map(inputSource -> inputSource.name).sorted().toArray(String[]::new);
	}
}

