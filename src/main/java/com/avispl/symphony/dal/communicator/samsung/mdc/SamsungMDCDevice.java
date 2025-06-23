/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc;

import static com.avispl.symphony.dal.communicator.samsung.mdc.SamsungMDCConstants.commandNames;
import static com.avispl.symphony.dal.communicator.samsung.mdc.SamsungMDCConstants.commands;
import static com.avispl.symphony.dal.communicator.samsung.mdc.SamsungMDCConstants.inputNames;
import static com.avispl.symphony.dal.communicator.samsung.mdc.SamsungMDCConstants.inputs;
import static com.avispl.symphony.dal.communicator.samsung.mdc.SamsungMDCConstants.powerStatus;
import static com.avispl.symphony.dal.communicator.samsung.mdc.SamsungMDCConstants.powerStatusNames;
import static com.avispl.symphony.dal.communicator.samsung.mdc.SamsungMDCConstants.statusCodes;
import static com.avispl.symphony.dal.communicator.samsung.mdc.SamsungMDCConstants.statusNames;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.communicator.SocketCommunicator;

public class SamsungMDCDevice extends SocketCommunicator implements Controller, Monitorable {

    private int monitorID;
    private Set<String> historicalProperties = new HashSet<>();

    /**
     * Constructor set the TCP/IP port to be used as well the default monitor ID
     */
    public SamsungMDCDevice(){
        super();

        this.setPort(1515);
        this.monitorID = 0;

        // set list of command success strings (included at the end of response when command succeeds, typically ending with command prompt)
        this.setCommandSuccessList(Collections.singletonList("A"));
        // set list of error response strings (included at the end of response when command fails, typically ending with command prompt)
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

    /**
     * This method is recalled by Symphony to control specific property
     * @param controllableProperty This is the property to be controled
     */
    @Override
    public void controlProperty(ControllableProperty controllableProperty) throws Exception {

        if (controllableProperty.getProperty().equals(commandNames.power.name())){
            if(controllableProperty.getValue().toString().equals("1")){
                powerON();
            }else if(controllableProperty.getValue().toString().equals("0")){
                powerOFF();
            }
        }
    }

    /**
     * This method is recalled by Symphony to control a list of properties
     * @param controllableProperties This is the list of properties to be controlled
     * @return byte This returns the calculated xor checksum.
     */
    @Override
    public void controlProperties(List<ControllableProperty> controllableProperties) throws Exception {
        // same as controlProperty(ControllableProperty controllableProperty), but for a multiples of ControllableProperty
        controllableProperties.stream().forEach(p -> {
            try {
                controlProperty(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * This method is recalled by Symphony to get the list of statistics to be displayed
     * @return List<Statistics> This return the list of statistics.
     */
    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        ExtendedStatistics extendedStatistics = new ExtendedStatistics();

        Map<String, String> controllable = new HashMap<String, String>(){{
            put(commandNames.power.name(),"Toggle");
        }};

        Map<String, String> statistics = new HashMap<>();
        Map<String, String> dynamicStatistics = new HashMap<>();

        String power;

        try {
            power = getPower().name();
            if(power.compareTo("ON") == 0) {
                statistics.put(commandNames.power.name(), "1");
            }else if(power.compareTo("OFF") == 0)
            {
                statistics.put(commandNames.power.name(), "0");
            }
        }catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getPower", e);
            }
            throw e;
        }



        try {
            SamsungMDCStatus status = getStatus();

            statistics.put(statusNames.lamp.name(), status.getLamp().name());
            statistics.put(statusNames.temperature_code.name().replaceAll("_", " "), status.getTemperatureError().name());
            //statistics.put(statusNames.brightness_sensor.name(), status.getBrightnessSensor().name());
            if(status.getNoSync().name() == "ERROR")
            {
                statistics.put(statusNames.sync.name(), "NO SYNC");
            }else if(status.getNoSync().name() == "NORMAL")
            {
                statistics.put(statusNames.sync.name(), "DETECTED");
            }

            statistics.put(statusNames.fan.name(), status.getFan().name());

            String temperatureParameter = statusNames.temperature.name();
            String temperatureValue = Integer.toString(status.getTemperature());
            if (!historicalProperties.isEmpty() && historicalProperties.contains(temperatureParameter)) {
                dynamicStatistics.put(temperatureParameter, temperatureValue);
            } else {
                statistics.put(temperatureParameter, temperatureValue);
            }
        }catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getStatus", e);
            }
            throw e;
        }

        String input;

        try {
            input =  getInput().name();
            statistics.put(commandNames.input.name(), input);
        }catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getInput", e);
            }
            throw e;
        }

        extendedStatistics.setControl(controllable);
        extendedStatistics.setStatistics(statistics);

        if(this.logger.isDebugEnabled()) {
            for (String s : controllable.keySet()) {
                this.logger.debug("controllable key: " + s + ",value: " + controllable.get(s));
            }
        }

        if(this.logger.isDebugEnabled()) {
            for (String s : statistics.keySet()) {
                this.logger.debug("statistics key: " + s + ",value: " + statistics.get(s));
            }
        }

        //return Collections.singletonList(extendedStatistics);
        return new ArrayList<Statistics>(Collections.singleton(extendedStatistics));
    }

    /**
     * This method is used to get the current display power status
     * @return powerStatusNames This returns the calculated xor checksum.
     */
    private powerStatusNames getPower() throws Exception{
        //sending the get power command
        byte[] response = send(SamsungMDCUtils.buildSendString((byte)monitorID,commands.get(commandNames.power)));

        //digest the result
        powerStatusNames power= (powerStatusNames)digestResponse(response,commandNames.power);

        if(power == null)
        {
            throw new Exception();
        }else{
            return power;
        }
    }

    /**
     * This method is used to send the power ON command to the display
     */
    private void powerON() throws IOException {

        byte[] toSend = SamsungMDCUtils.buildSendString((byte)monitorID,commands.get(commandNames.power),new byte[]{powerStatus.get(powerStatusNames.ON)});

        try {
            byte[] response = send(toSend);

            //digesting the response but voiding the result
            digestResponse(response,commandNames.power);

            //disconnect from the device and wait for 20 seconds as the device is unresponsive during this time
            destroyChannel();
            synchronized(this) {//synchronized block
                Thread.sleep(20000);
            }
            //wait(10000);
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

        byte[] toSend = SamsungMDCUtils.buildSendString((byte)monitorID,commands.get(commandNames.power),new byte[]{powerStatus.get(powerStatusNames.OFF)});

        try {
            byte[] response = send(toSend);

            digestResponse(response,commandNames.power);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during power OFF send", e);
            }
        }
    }

    /**
     * This method is used to get the current display input
     * @return inputNames This returns the current input.
     */
    private inputNames getInput()throws  Exception {
            byte[] response = send(SamsungMDCUtils.buildSendString((byte) monitorID, commands.get(commandNames.input)));

            inputNames input = (inputNames) digestResponse(response, commandNames.input);

            if(input == null)
            {
                throw new Exception();
            }else{
                return input;
            }
    }

    /**
     * This method is used to get the status results from the display
     * @return SamsungMDCStatus This returns the retrieved status results.
     */
    private SamsungMDCStatus getStatus() throws  Exception{
        byte[] response = send(SamsungMDCUtils.buildSendString((byte)monitorID,commands.get(commandNames.status)));

        SamsungMDCStatus status = (SamsungMDCStatus)digestResponse(response,commandNames.status);

        if(status == null)
        {
            throw new Exception();
        }else {
            return status;
        }
    }

    /**
     * This method is used to digest the response received from the device
     * @param responseBytes This is the response to be digested
     * @param expectedResponse This is the expected response type to be compared with received
     * @return Object This returns the result digested from the response.
     */
    private Object digestResponse(byte[] responseBytes, commandNames expectedResponse){
        Set set = null;

        //checksum verification
        byte checksumByte = SamsungMDCUtils.checkSum(java.util.Arrays.copyOfRange(responseBytes,1,responseBytes.length-1));

        if(checksumByte == responseBytes[responseBytes.length-1]){
            switch (responseBytes[4]){
                case 'A':
                {
                    switch (expectedResponse){
                        case power:{
                            if(responseBytes[5] == commands.get(commandNames.power)){
                                set = powerStatus.entrySet();

                                Iterator iterator = set.iterator();
                                while(iterator.hasNext()) {
                                    Map.Entry mentry = (Map.Entry)iterator.next();

                                    if((byte)mentry.getValue() == responseBytes[6]){
                                        return (Enum)mentry.getKey();
                                    }
                                }
                            }else{
                                if (this.logger.isErrorEnabled()) {
                                    this.logger.error("error: Unexpected response: " + this.host + " port: " + this.getPort());
                                }
                                throw new RuntimeException("Unexpected response");
                            }
                            break;
                        }case input:{
                            if(responseBytes[5] == commands.get(commandNames.input)){
                                set = inputs.entrySet();

                                Iterator iterator = set.iterator();
                                while(iterator.hasNext()) {
                                    Map.Entry mentry = (Map.Entry)iterator.next();

                                    if((byte)mentry.getValue() == responseBytes[6]){
                                        return (Enum)mentry.getKey();
                                    }
                                }
                            }else{
                                if (this.logger.isErrorEnabled()) {
                                    this.logger.error("error: Unexpected response: " + this.host + " port: " + this.getPort());
                                }
                                throw new RuntimeException("Unexpected response");
                            }
                            break;
                        }case status:{
                            if(responseBytes[5] == commands.get(commandNames.status)){
                                return new SamsungMDCStatus(statusCodes.get(responseBytes[6]),
                                        statusCodes.get(responseBytes[7]),
                                        statusCodes.get(responseBytes[8]),
                                        statusCodes.get(responseBytes[9]),
                                        statusCodes.get(responseBytes[11]),responseBytes[10]);
                            }else{
                                if (this.logger.isErrorEnabled()) {
                                    this.logger.error("error: Unexpected response: " + this.host + " port: " + this.getPort());
                                }
                                throw new RuntimeException("Unexpected response");
                            }
                        }
                    }
                    break;
                }
                case 'N':
                {
                    switch (expectedResponse){
                        case power:{
                            if (this.logger.isErrorEnabled()) {
                                this.logger.error("error: Power command returned NAK: " + this.host + " port: " + this.getPort());
                            }
                            throw new RuntimeException("Power command returned NAK");
                        }case input:{
                            if(responseBytes[5] == commands.get(commandNames.input)){
                                set = inputs.entrySet();

                                Iterator iterator = set.iterator();
                                while(iterator.hasNext()) {
                                    Map.Entry mentry = (Map.Entry)iterator.next();

                                    if((byte)mentry.getValue() == responseBytes[6]){
                                        return (Enum)mentry.getKey();
                                    }
                                }
                            }else{
                                if (this.logger.isErrorEnabled()) {
                                    this.logger.error("error: Unexpected response: " + this.host + " port: " + this.getPort());
                                }
                                throw new RuntimeException("Unexpected response");
                            }
                            break;
                        }
                    }
                    break;
                } default:
                {
                    if (this.logger.isErrorEnabled()) {
                        this.logger.error("error: Nor ACK or NAK received: " + this.host + " port: " + this.getPort());
                    }
                    throw new RuntimeException("Nor ACK or NAK received");
                }
            }
        }else{//Wrong checksum
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
