/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc.models;

import com.avispl.symphony.dal.communicator.samsung.mdc.types.StatusCode;

/**
 * Class representing the status control information of a device.
 *
 * <br>
 * This class is typically used to encapsulate the overall health or status of the device.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.1.0
 */
public class StatusControl {
    private StatusCode lamp;
    private StatusCode temperature;
    private StatusCode noSync;
    private int currentTemperature;
    private StatusCode fan;

    /**
     * Retrieves {@link #lamp}
     *
     * @return value of {@link #lamp}
     */
    public StatusCode getLamp() {
        return lamp;
    }

    /**
     * Sets {@link #lamp} value
     *
     * @param lamp new value of {@link #lamp}
     */
    public void setLamp(StatusCode lamp) {
        this.lamp = lamp;
    }

    /**
     * Retrieves {@link #temperature}
     *
     * @return value of {@link #temperature}
     */
    public StatusCode getTemperature() {
        return temperature;
    }

    /**
     * Sets {@link #temperature} value
     *
     * @param temperature new value of {@link #temperature}
     */
    public void setTemperature(StatusCode temperature) {
        this.temperature = temperature;
    }

    /**
     * Retrieves {@link #noSync}
     *
     * @return value of {@link #noSync}
     */
    public StatusCode getNoSync() {
        return noSync;
    }

    /**
     * Sets {@link #noSync} value
     *
     * @param noSync new value of {@link #noSync}
     */
    public void setNoSync(StatusCode noSync) {
        this.noSync = noSync;
    }

    /**
     * Retrieves {@link #currentTemperature}
     *
     * @return value of {@link #currentTemperature}
     */
    public int getCurrentTemperature() {
        return currentTemperature;
    }

    /**
     * Sets {@link #currentTemperature} value
     *
     * @param currentTemperature new value of {@link #currentTemperature}
     */
    public void setCurrentTemperature(int currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    /**
     * Retrieves {@link #fan}
     *
     * @return value of {@link #fan}
     */
    public StatusCode getFan() {
        return fan;
    }

    /**
     * Sets {@link #fan} value
     *
     * @param fan new value of {@link #fan}
     */
    public void setFan(StatusCode fan) {
        this.fan = fan;
    }
}
