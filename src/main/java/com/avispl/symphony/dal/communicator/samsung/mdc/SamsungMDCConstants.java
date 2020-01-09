package com.avispl.symphony.dal.communicator.samsung.mdc;

import java.util.HashMap;
import java.util.Map;

class SamsungMDCConstants {

    //Power status values
    enum powerStatusNames {ON,OFF}
    //Power HEX codes map
    final static Map<powerStatusNames,Byte> powerStatus = new HashMap<powerStatusNames,Byte>(){{
        put(powerStatusNames.ON, (byte)0x01);
        put(powerStatusNames.OFF, (byte)0x00);
    }};

    //Device commands
    enum commandNames{POWER,INPUT,STATUS}
    //Commands HEX values map
    final static Map<commandNames, Byte> commands = new HashMap<commandNames,Byte>(){{
        put(commandNames.POWER, (byte)0x11);
        put(commandNames.INPUT, (byte)0x14);
        put(commandNames.STATUS, (byte)0x0D);
    }};

    //Input values
    enum inputNames {OFF,HDMI_1,HDMI_2,PC,DVI,DVI_VIDEO,COMPONENT,RF,DTV,DISPLAYPORT,MAGIC_INFO}
    //Inputs HEX values map
    final static Map<inputNames, Byte> inputs = new HashMap<inputNames, Byte>() {{
        put(inputNames.OFF, (byte)0x01);
        put(inputNames.HDMI_1, (byte)0x21);
        put(inputNames.HDMI_2, (byte)0x23);
        put(inputNames.PC, (byte)0x15);
        put(inputNames.DVI, (byte)0x18);
        put(inputNames.DVI_VIDEO, (byte)0x1F);
        put(inputNames.COMPONENT, (byte)0x08);
        put(inputNames.RF, (byte)0x30);
        put(inputNames.DTV, (byte)0x40);
        put(inputNames.DISPLAYPORT, (byte)0x25);
        put(inputNames.MAGIC_INFO, (byte)0x20);
    }};

    //status codes values
    enum statusCodeNames{NORMAL,ERROR,UNKNOWN}
    //status codes HEX balues map
    final static Map<Byte,statusCodeNames> statusCodes = new HashMap<Byte,statusCodeNames>(){{
       put((byte)0x00,statusCodeNames.NORMAL);
       put((byte)0x01,statusCodeNames.ERROR);
        put((byte)0x02,statusCodeNames.UNKNOWN);
    }};

    enum statusNames{LAMP,TEMPERATURE_CODE,BRIGHTNESS_SENSOR,NO_SYNC,TEMPERATURE,FAN}
}
