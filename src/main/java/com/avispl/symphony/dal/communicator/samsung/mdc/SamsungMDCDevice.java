/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;

import com.avispl.symphony.api.common.error.NotImplementedException;
import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
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
import com.avispl.symphony.dal.communicator.samsung.mdc.types.properties.AdapterMetadataProperty;
import com.avispl.symphony.dal.communicator.samsung.mdc.types.properties.GeneralProperty;

/**
 * Main adapter class for Samsung MDC.
 * Responsible for generating monitoring, controllable.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.1.0
 */
public class SamsungMDCDevice extends SocketCommunicator implements Controller, Monitorable {
    /**
     * Set of property names supported for historical data tracking.
     */
    private static final Set<String> SUPPORTED_HISTORICAL_PROPS = new HashSet<>(Collections.singletonList(
        GeneralProperty.TEMPERATURE.getName()
    ));

    /**
     * Lock used to ensure thread-safe operations.
     */
    private final ReentrantLock reentrantLock;
    /**
     * Holds the application configuration properties loaded from the {@code version.properties} file.
     */
    private final Properties versionProperties;
    /**
     * Device adapter instantiation timestamp.
     */
    private final Long adapterInitializationTimestamp;

    /**
     * Store of extended statistics object.
     */
    private ExtendedStatistics localExtendedStatistics;
    /**
     * Represents the {@link PowerControl} of the adapter.
     */
    private PowerControl powerControl;
    /**
     * Represents the {@link StatusControl} of the adapter.
     */
    private StatusControl statusControl;
    /**
     * Represents the {@link InputSource} of the adapter.
     */
    private InputSource inputSource;

    /**
     * The ID of the MDC device
     */
    private int deviceId;
    private Set<String> historicalProperties = new HashSet<>();

    /**
     * Constructor set the TCP/IP port to be used as well the default monitor ID
     */
    public SamsungMDCDevice() {
        this.reentrantLock = new ReentrantLock();
        this.versionProperties = new Properties();
        this.adapterInitializationTimestamp = System.currentTimeMillis();

        this.localExtendedStatistics = new ExtendedStatistics();
        this.powerControl = PowerControl.UNDEFINED;
        this.statusControl = new StatusControl();
        this.inputSource = InputSource.UNDEFINED;

        this.deviceId = 0;
    }

    /**
     * Retrieves {@link #historicalProperties}
     *
     * @return value of {@link #historicalProperties}
     */
    public String getHistoricalProperties() {
        return String.join(Constant.COMMA, this.historicalProperties);
    }

    /**
     * Sets {@link #historicalProperties} value
     *
     * @param historicalProperties new value of {@link #historicalProperties}
     */
    public void setHistoricalProperties(String historicalProperties) {
        this.historicalProperties.clear();
        Arrays.asList(historicalProperties.split(Constant.COMMA))
            .forEach(propertyName -> this.historicalProperties.add(propertyName.trim()));
    }

    /**
     * Retrieves {@link #deviceId}
     *
     * @return value of {@link #deviceId}
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * Sets {@link #deviceId} value
     *
     * @param deviceId new value of {@link #deviceId}
     */
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    protected void internalInit() throws Exception {
        this.setCommandSuccessList(Collections.singletonList(""));
        this.setCommandErrorList(Collections.singletonList(""));
        this.loadProperties(this.versionProperties);
        super.internalInit();
    }

    @Override
    public void controlProperty(ControllableProperty controllableProperty) throws Exception {
        this.reentrantLock.lock();
        try {
            if (controllableProperty.getProperty().equals(GeneralProperty.POWER.getName())) {
                byte requestByte = controllableProperty.getValue().toString().equals("1")
                    ? PowerControl.ON.getCode()
                    : PowerControl.OFF.getCode();
                this.performControlOperation(Command.POWER, requestByte);
            } else if (controllableProperty.getProperty().equals(GeneralProperty.INPUT.getName())) {
                InputSource updatedInput = InputSource.getByName(controllableProperty.getValue().toString());
                Object response = this.performControlOperation(Command.INPUT_SOURCE, updatedInput.getCode());
                if (response.equals(InputSource.UNDEFINED)) {
                    throw new UnsupportedOperationException(String.format(Constant.SET_INPUT_FAILED, controllableProperty.getValue()));
                }
            }
        } finally {
            this.reentrantLock.unlock();
        }
    }

    @Override
    public void controlProperties(List<ControllableProperty> controllableProperties) throws Exception {
        if (CollectionUtils.isEmpty(controllableProperties)) {
            this.logger.warn(Constant.CONTROLLABLE_PROPS_EMPTY_WARNING);
            return;
        }
        controllableProperties.forEach(controllableProperty -> {
            try {
                this.controlProperty(controllableProperty);
            } catch (Exception e) {
                this.logger.error(Constant.CONTROL_PROPERTY_FAILED + controllableProperty.getProperty(), e);
            }
        });
    }

    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        this.reentrantLock.lock();
        try {
            this.setupData();
            ExtendedStatistics extendedStatistics = new ExtendedStatistics();
            Map<String, String> statistics = new HashMap<>();
            statistics.putAll(this.generateGeneralProperties());
            statistics.putAll(this.generateAdapterMetadataProperties());

            extendedStatistics.setStatistics(statistics);
            extendedStatistics.setControllableProperties(this.generateGeneralControls());
            extendedStatistics.setDynamicStatistics(this.generateDynamicStatistics(statistics));
            this.localExtendedStatistics = extendedStatistics;
        } finally {
            this.reentrantLock.unlock();
        }
        return Collections.singletonList(this.localExtendedStatistics);
    }

    @Override
    protected void internalDestroy() {
        this.logger.info(Constant.DESTROY_INTERNAL_INFO + this);
        this.powerControl = null;
        this.statusControl = null;
        this.inputSource = null;
        this.localExtendedStatistics = null;
        this.historicalProperties = null;
        super.internalDestroy();
    }

    /**
     * Load properties from the {@code version.properties} file into the provided {@link Properties} object.
     * <p>
     * If the file is not found or an error occurs during reading, the method logs the error but does not throw an exception.
     *
     * @param properties The {@link Properties} object to populate with configuration values.
     */
    private void loadProperties(Properties properties) {
        try {
            properties.load(getClass().getResourceAsStream("/version.properties"));
            properties.setProperty("adapter.uptime", String.valueOf(this.adapterInitializationTimestamp));
        } catch (IOException e) {
            this.logger.error(Constant.READ_PROPERTIES_FILE_FAILED + e.getMessage());
        }
    }

    /**
     * Initializes and sets up the device data by retrieving power control, status control,
     * and input source information.
     */
    private void setupData() {
        this.powerControl = this.fetchData(Command.POWER, PowerControl.class);
        this.statusControl = this.fetchData(Command.STATUS, StatusControl.class);
        this.inputSource = this.fetchData(Command.INPUT_SOURCE, InputSource.class);
    }

    /**
     * Generates general properties from the {@link PowerControl}, {@link StatusControl}, {@link InputSource}.
     *
     * @return A map of general property names and their corresponding values,
     * or an empty map if the current status is not available.
     */
    private Map<String, String> generateGeneralProperties() {
        Map<String, String> statistics = new HashMap<>();
        statistics.put(GeneralProperty.POWER.getName(), Util.mapToGeneralProperty(this.powerControl));
        statistics.put(GeneralProperty.LAMP.getName(), Util.mapToGeneralProperty(GeneralProperty.LAMP, this.statusControl));
        statistics.put(GeneralProperty.TEMPERATURE_STATUS.getName(), Util.mapToGeneralProperty(GeneralProperty.TEMPERATURE_STATUS, this.statusControl));
        statistics.put(GeneralProperty.SYNC.getName(), Util.mapToGeneralProperty(GeneralProperty.SYNC, this.statusControl));
        statistics.put(GeneralProperty.TEMPERATURE.getName(), Util.mapToGeneralProperty(GeneralProperty.TEMPERATURE, this.statusControl));
        statistics.put(GeneralProperty.FAN.getName(), Util.mapToGeneralProperty(GeneralProperty.FAN, this.statusControl));
        statistics.put(GeneralProperty.INPUT.getName(), Util.mapToGeneralProperty(this.inputSource));

        return statistics;
    }

    /**
     * Generates properties related to Adapter
     *
     * @return A map of Adapter metadata properties with the property names as keys and their corresponding mapped values as values.
     */
    private Map<String, String> generateAdapterMetadataProperties() {
        Map<String, String> statistics = new HashMap<>();
        statistics.put(
            String.format(Constant.PROPERTY_FORMAT, Constant.ADAPTER_METADATA_GROUP, AdapterMetadataProperty.ADAPTER_BUILD_DATE.getName()),
            Util.mapToAdapterMetadataProperty(this.versionProperties, AdapterMetadataProperty.ADAPTER_BUILD_DATE)
        );
        statistics.put(
            String.format(Constant.PROPERTY_FORMAT, Constant.ADAPTER_METADATA_GROUP, AdapterMetadataProperty.ADAPTER_UPTIME.getName()),
            Util.mapToAdapterMetadataProperty(this.versionProperties, AdapterMetadataProperty.ADAPTER_UPTIME)
        );
        statistics.put(
            String.format(Constant.PROPERTY_FORMAT, Constant.ADAPTER_METADATA_GROUP, AdapterMetadataProperty.ADAPTER_UPTIME_MIN.getName()),
            Util.mapToAdapterMetadataProperty(this.versionProperties, AdapterMetadataProperty.ADAPTER_UPTIME_MIN)
        );
        statistics.put(
            String.format(Constant.PROPERTY_FORMAT, Constant.ADAPTER_METADATA_GROUP, AdapterMetadataProperty.ADAPTER_VERSION.getName()),
            Util.mapToAdapterMetadataProperty(this.versionProperties, AdapterMetadataProperty.ADAPTER_VERSION)
        );

        return statistics;
    }

    /**
     * Generates a list of {@link AdvancedControllableProperty} objects representing
     * the current controllable properties of the device.
     *
     * @return a list of {@code AdvancedControllableProperty} instances representing the device's controls
     */
    private List<AdvancedControllableProperty> generateGeneralControls() {
        List<AdvancedControllableProperty> controllableProperties = new ArrayList<>();
        if (this.powerControl == null) {
            this.logger.warn(Constant.POWER_CONTROL_NULL_WARNING);
        } else {
            controllableProperties.add(this.generateControllableSwitch(
                GeneralProperty.POWER.getName(), Constant.ON, Constant.OFF, (int) this.powerControl.getCode()
            ));
        }
        if (this.inputSource == null || this.inputSource.equals(InputSource.UNDEFINED)) {
            this.logger.warn(String.format(Constant.INPUT_SOURCE_NULL_WARNING, this.inputSource));
        } else {
            controllableProperties.add(this.generateControllableDropdown(
                GeneralProperty.INPUT.getName(), InputSource.getNames(), InputSource.getNames(), this.inputSource.getName()
            ));
        }

        return controllableProperties;
    }

    /**
     * Filters the input statistics to include only supported historical properties.
     *
     * @param statistics A map of all available statistics.
     * @return A map containing only the supported historical properties and their values,
     * or an empty map if the statusControl, historical properties, or input map is null/empty.
     */
    private Map<String, String> generateDynamicStatistics(Map<String, String> statistics) {
        if (this.statusControl == null) {
            this.logger.warn(Constant.STATUS_CONTROL_NULL_WARNING);
            return Collections.emptyMap();
        }
        if (CollectionUtils.isEmpty(this.historicalProperties)) {
            this.logger.warn(Constant.HISTORICAL_PROPS_EMPTY_WARNING);
            return Collections.emptyMap();
        }

        Map<String, String> dynamicStatistics = new HashMap<>();
        this.historicalProperties.forEach(property -> {
            String value = statistics.get(property);
            if (SUPPORTED_HISTORICAL_PROPS.contains(property) && value != null) {
                dynamicStatistics.put(property, value);
            }
        });

        return dynamicStatistics;
    }

    /**
     * Generates an {@link AdvancedControllableProperty} of type Switch with the specified name, labels, and value.
     *
     * @param switchName the name of the switch control property
     * @param labelOn the label to display when the switch is in the "on" position
     * @param labelOff the label to display when the switch is in the "off" position
     * @param value the initial value of the switch (should be a Boolean or compatible object)
     * @return an {@link AdvancedControllableProperty} configured as a switch control
     */
    private AdvancedControllableProperty generateControllableSwitch(String switchName, String labelOn, String labelOff, Object value) {
        AdvancedControllableProperty.Switch toggleSwitch = new AdvancedControllableProperty.Switch();
        toggleSwitch.setLabelOn(labelOn);
        toggleSwitch.setLabelOff(labelOff);

        return new AdvancedControllableProperty(switchName, new Date(), toggleSwitch, value);
    }

    /**
     * Generates an {@link AdvancedControllableProperty} of type Dropdown with the specified name, labels, options, and value.
     *
     * @param dropdownName the name of the dropdown control property
     * @param labels       the display labels for each dropdown option
     * @param options      the actual option values associated with each label
     * @param value        the initial selected value of the dropdown
     * @return an {@link AdvancedControllableProperty} configured as a dropdown control
     */
    private AdvancedControllableProperty generateControllableDropdown(String dropdownName, String[] labels, String[] options, Object value) {
        AdvancedControllableProperty.DropDown dropdown = new AdvancedControllableProperty.DropDown();
        dropdown.setLabels(labels);
        dropdown.setOptions(options);

        return new AdvancedControllableProperty(dropdownName, new Date(), dropdown, value);
    }

    /**
     * Sends a request using the specified {@link Command} and parses the response into the given response type.
     *
     * <p>This method builds a request message based on the command and device ID,
     * sends the request over a TCP connection, and attempts to parse the response into
     * the specified response class. If an error occurs, appropriate error handling is performed,
     * including logging.</p>
     *
     * @param command the command used to build the request.
     * @param responseClass the class type to which the response should be cast.
     * @param <T> the expected type of the response.
     * @return the parsed response of type {@code T}, or {@code null} if an error occurs.
     * @throws ResourceNotReachableException if a connection error occurs.
     */
    private <T> T fetchData(Command command, Class<T> responseClass) {
        byte[] request = Util.buildSendString((byte) this.deviceId, command.getCode());
        try {
            byte[] response = super.send(request);
            return responseClass.cast(this.digestResponse(response, command));
        } catch (ConnectException e) {
            throw new RuntimeException(String.format("Unable to process the command [%s]: please check the device configuration.", command.getCode()), e);
        } catch (Exception e) {
            this.logger.error(String.format(Constant.FETCH_DATA_FAILED, command, Arrays.toString(request)), e);
            throw new RuntimeException(String.format("Unable to process the command [%s].", command.getCode()), e);
        }
    }

    /**
     * Performs a control operation by sending a specific command to the device.
     *
     * <p>This method builds the request payload using the given {@code command} and {@code requestByte},
     * sends it to the device, and processes the response. If the command is {@code POWER} and the request byte
     * is {@code ON}, the connection will be closed and the thread will wait for 20 seconds, as the device
     * becomes temporarily unresponsive after powering on.</p>
     *
     * @param command the command to be executed (e.g., POWER, VOLUME, etc.)
     * @param requestByte the specific byte value representing the control action (e.g., ON, OFF)
     * @return the parsed response object from the device
     * @throws ResourceNotReachableException if a connection error occurs while sending the request
     * @throws NotImplementedException if an unexpected exception occurs during the operation
     */
    private Object performControlOperation(Command command, byte requestByte) {
        byte[] request = Util.buildSendString((byte) this.deviceId, command.getCode(), new byte[] { requestByte });
        try {
            byte[] response = this.send(request);
            Object digestResponse = this.digestResponse(response, command);
            if (command == Command.POWER && requestByte == PowerControl.ON.getCode()) {
                //  Disconnect from the device and wait for 20 seconds as the device is unresponsive during this time
                this.destroyChannel();
                Thread.sleep(20000);
            }
            return digestResponse;
        } catch (ConnectException e) {
            throw new ResourceNotReachableException(e.getMessage(), e);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            this.logger.error(String.format(Constant.PERFORM_DATA_FAILED, command, Arrays.toString(request)));
            throw new NotImplementedException(Constant.SET_COMMAND_FAILED, e);
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
        byte checkSum = Util.checkSum(Arrays.copyOfRange(responseBytes, 1, responseBytes.length - 1));

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
                            StatusControl status = new StatusControl();
                            status.setLamp(StatusCode.getByStatusControlProperty("lamp", responseBytes[6]));
                            status.setTemperature(StatusCode.getByStatusControlProperty("temperature", responseBytes[7]));
                            status.setNoSync(StatusCode.getByStatusControlProperty("noSync", responseBytes[9]));
                            status.setCurrentTemperature(responseBytes[10]);
                            status.setFan(StatusCode.getByStatusControlProperty("fan", responseBytes[11]));

                            return status;
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
                            throw new ResourceNotReachableException("Power command returned NAK");
                        }
                        case INPUT_SOURCE: {
                            if (responseBytes[5] != Command.INPUT_SOURCE.getCode()) {
                                throw new ResourceNotReachableException("Unexpected response");
                            }
                            return InputSource.UNDEFINED;
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
