/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.communicator.SocketCommunicator;
import com.avispl.symphony.dal.communicator.samsung.mdc.common.Constant;
import com.avispl.symphony.dal.communicator.samsung.mdc.common.Util;
import com.avispl.symphony.dal.communicator.samsung.mdc.models.StatusControl;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.Command;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.InputSource;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.PowerControl;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.StatusCode;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.properties.GeneralProperty;

public class SamsungMDCDevice extends SocketCommunicator implements Controller, Monitorable {
    private int monitorID;
    private Set<String> historicalProperties = new HashSet<>();

    /**
     * Constructor set the TCP/IP port to be used as well the default monitor ID
     */
    public SamsungMDCDevice() {
        this.monitorID = 0;

        this.setCommandSuccessList(Collections.singletonList("A"));
        this.setCommandErrorList(Collections.singletonList("ERROR"));
    }

    /**
     * Retrieves {@link #historicalProperties}
     *
     * @return value of {@link #historicalProperties}
     */
    public String getHistoricalProperties() {
        return String.join(",", this.historicalProperties);
    }

    /**
     * Sets {@link #historicalProperties} value
     *
     * @param historicalProperties new value of {@link #historicalProperties}
     */
    public void setHistoricalProperties(String historicalProperties) {
        this.historicalProperties.clear();
        Arrays.asList(historicalProperties.split(",")).forEach(propertyName -> {
            this.historicalProperties.add(propertyName.trim());
        });
    }

    public int getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(int monitorID) {
        this.monitorID = monitorID;
    }

    @Override
    public void controlProperty(ControllableProperty controllableProperty) throws Exception {
        if (controllableProperty.getProperty().equals(GeneralProperty.POWER.getName())) {
            if (controllableProperty.getValue().toString().equals("1")) {
                powerON();
            } else if (controllableProperty.getValue().toString().equals("0")) {
                powerOFF();
            }
        }
    }

    @Override
    public void controlProperties(List<ControllableProperty> controllableProperties) throws Exception {
        controllableProperties.forEach(p -> {
            try {
                controlProperty(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        ExtendedStatistics extendedStatistics = new ExtendedStatistics();

        Map<String, String> controllable = new HashMap<>();
        controllable.put(GeneralProperty.POWER.getName(), Constant.NOT_AVAILABLE);

        Map<String, String> statistics = new HashMap<>();

        String power;

        try {
            power = getPower().name();
            if (power.compareTo("ON") == 0) {
                statistics.put(GeneralProperty.POWER.getName(), "1");
            } else if (power.compareTo("OFF") == 0) {
                statistics.put(GeneralProperty.POWER.getName(), "0");
            }
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getPower", e);
            }
            throw e;
        }
        try {
            StatusControl status = getStatus();

            statistics.put(GeneralProperty.LAMP.getName(), status.getLamp().getName());
            statistics.put(GeneralProperty.TEMPERATURE_STATUS.getName(), status.getTemperature().getName());
            statistics.put(GeneralProperty.SYNC.getName(), status.getNoSync().getName());
            statistics.put(GeneralProperty.TEMPERATURE.getName(), String.valueOf(status.getCurrentTemperature()));
            statistics.put(GeneralProperty.FAN.getName(), status.getFan().getName());
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getStatus", e);
            }
            throw e;
        }
        try {
            statistics.put(GeneralProperty.INPUT.getName(), getInput().getName());
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getInput", e);
            }
            throw e;
        }

        extendedStatistics.setControl(controllable);
        extendedStatistics.setStatistics(statistics);
        return Collections.singletonList(extendedStatistics);
    }

    /**
     * This method is used to get the current display power status
     *
     * @return powerStatusNames This returns the calculated xor checksum.
     */
    private PowerControl getPower() throws Exception {
        byte[] response = this.send(Util.buildSendString((byte) monitorID, Command.POWER.getCode()));
        PowerControl power = (PowerControl) this.digestResponse(response, Command.POWER);

        if (power == null) {
            throw new Exception();
        } else {
            return power;
        }
    }

    /**
     * This method is used to send the power ON command to the display
     */
    private void powerON() throws IOException {
        byte[] toSend = Util.buildSendString((byte) monitorID, Command.POWER.getCode(), new byte[] { PowerControl.ON.getCode() });
        try {
            byte[] response = send(toSend);

            //digesting the response but voiding the result
            digestResponse(response, Command.POWER);

            //disconnect from the device and wait for 20 seconds as the device is unresponsive during this time
            destroyChannel();
            synchronized (this) {//synchronized block
                Thread.sleep(20000);
            }
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during power ON send", e);
            }
        }
    }

    /**
     * This method is used to send the power OFF command to the display
     */
    private void powerOFF() throws IOException {
        byte[] toSend = Util.buildSendString((byte) monitorID, Command.POWER.getCode(), new byte[] { PowerControl.OFF.getCode() });
        try {
            byte[] response = send(toSend);

            digestResponse(response, Command.POWER);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during power OFF send", e);
            }
        }
    }

    /**
     * This method is used to get the current display input
     *
     * @return inputNames This returns the current input.
     */
    private InputSource getInput() throws Exception {
        byte[] response = send(Util.buildSendString((byte) monitorID, Command.INPUT_SOURCE.getCode()));
        InputSource input = (InputSource) digestResponse(response, Command.INPUT_SOURCE);

        if (input == null) {
            throw new Exception();
        } else {
            return input;
        }
    }

    /**
     * This method is used to get the status results from the display
     *
     * @return SamsungMDCStatus This returns the retrieved status results.
     */
    private StatusControl getStatus() throws Exception {
        byte[] response = send(Util.buildSendString((byte) monitorID, Command.STATUS.getCode()));
        StatusControl status = (StatusControl) digestResponse(response, Command.STATUS);

        if (status == null) {
            throw new Exception();
        } else {
            return status;
        }
    }

    /**
     * This method is used to digest the response received from the device
     *
     * @param responseBytes This is the response to be digested
     * @param expectedResponse This is the expected response type to be compared with received
     * @return Object This returns the result digested from the response.
     */
    private Object digestResponse(byte[] responseBytes, Command expectedResponse) {
        byte checkSum = Util.checkSum(java.util.Arrays.copyOfRange(responseBytes, 1, responseBytes.length - 1));

        if (checkSum == responseBytes[responseBytes.length - 1]) {
            switch (responseBytes[4]) {
                case 'A': {
                    switch (expectedResponse) {
                        case POWER: {
                            if (responseBytes[5] != Command.POWER.getCode()) {
                                throw new ResourceNotReachableException("Unexpected response");
                            }
                            return PowerControl.getByCode(responseBytes[6]);
                        }
                        case INPUT_SOURCE: {
                            if (responseBytes[5] != Command.INPUT_SOURCE.getCode()) {
                                throw new ResourceNotReachableException("Unexpected response");
                            }
                            return InputSource.getByCode(responseBytes[6]);
                        }
                        case STATUS: {
                            if (responseBytes[5] != Command.STATUS.getCode()) {
                                throw new ResourceNotReachableException("Unexpected response");
                            }
                            StatusControl statusControl = new StatusControl();
                            statusControl.setLamp(StatusCode.getByStatusControlProperty("lamp", responseBytes[6]));
                            statusControl.setTemperature(StatusCode.getByStatusControlProperty("temperature", responseBytes[7]));
                            statusControl.setNoSync(StatusCode.getByStatusControlProperty("noSync", responseBytes[9]));
                            statusControl.setCurrentTemperature(responseBytes[10]);
                            statusControl.setFan(StatusCode.getByStatusControlProperty("fan", responseBytes[11]));

                            return statusControl;
                        }
                    }
                    break;
                }
                case 'N': {
                    switch (expectedResponse) {
                        case POWER: {
                            if (this.logger.isErrorEnabled()) {
                                this.logger.error("error: Power command returned NAK: " + this.host + " port: " + this.getPort());
                            }
                            throw new RuntimeException("Power command returned NAK");
                        }
                        case INPUT_SOURCE: {
                            if (responseBytes[5] != Command.INPUT_SOURCE.getCode()) {
                                throw new ResourceNotReachableException("Unexpected response");
                            }
                            return InputSource.getByCode(responseBytes[6]);
                        }
                    }
                    break;
                }
                default: {
                    if (this.logger.isErrorEnabled()) {
                        this.logger.error("error: Nor ACK or NAK received: " + this.host + " port: " + this.getPort());
                    }
                    throw new RuntimeException("Nor ACK or NAK received");
                }
            }
        } else {//Wrong checksum
            if (this.logger.isErrorEnabled()) {
                this.logger.error("error: wrong checksum communicating with: " + this.host + " port: " + this.getPort());
            }
            throw new RuntimeException("wrong Checksum received");
        }
        if (this.logger.isErrorEnabled()) {
            this.logger.error("error: End of digestResponse reached without a solution: " + this.host + " port: " + this.getPort());
        }
        throw new RuntimeException("End of digestResponse reached without a solution");
    }
}
