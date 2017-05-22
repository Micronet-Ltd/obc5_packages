package com.micronet.canbus;

/*
* Set auto respond flow control messages.
* Configured when you need the firmware to automatically respond to a message with 1st data byte as <10>
* The number of available flow control codes is 8.
*
*/

public class CanbusFlowControl {

    private int[] searchIds;
    private int[] responseIds;
    private int[] flowDataLength;
    private byte[] dataBytes1=new byte[8];
    private byte[] dataBytes2=new byte[8];
    private byte[] dataBytes3=new byte[8];
    private byte[] dataBytes4=new byte[8];
    private byte[] dataBytes5=new byte[8];
    private byte[] dataBytes6=new byte[8];
    private byte[] dataBytes7=new byte[8];
    private byte[] dataBytes8=new byte[8];
    private int[] idType={EXTENDED};

    private int n = 0;
    int length=0;

    static public int STANDARD=0;
    static public int EXTENDED=1;

    /**
     * Sets Canbus Flow Control module to respond to certain IDS with specific response ids and data bytes.
     * Supports Upto 8 Flow codes per can instance
     *
     * @param searchIdArray Register search ids (Ids the Firmware will respond immediately to).
     * @param responseIdArray Register response ids which will be used by the firmware to respond to messages with registered searchIdArray.
     * @param type An array of Standard / Extended frames.
     * @param dataLengthArray The data length of the data bytes.
     * @param responseDataBytes The firmware responds with these data bytes with its respective response ID.
     */
    public CanbusFlowControl(int[] searchIdArray, int[] responseIdArray, int[] type, int[] dataLengthArray, byte[][] responseDataBytes) {

        searchIds = searchIdArray;
        responseIds = responseIdArray;
        idType = type;
        flowDataLength = dataLengthArray;
        length = responseDataBytes.length;
        for (n = length - 1; n > -1; n--) {
            if(n == 0) {dataBytes1 = responseDataBytes[n];}
            else if(n == 1){dataBytes2 = responseDataBytes[n];}
            else if(n == 2){dataBytes3 = responseDataBytes[n];}
            else if(n == 3){dataBytes4 = responseDataBytes[n];}
            else if(n == 4){dataBytes5=responseDataBytes[n];}
            else if(n == 5){dataBytes6=responseDataBytes[n];}
            else if(n == 6){dataBytes7=responseDataBytes[n];}
            else if(n == 7){dataBytes8=responseDataBytes[n];}
            else break;
        }
    }

    /**
     * Returns searchIds
     */
    public int[] getSearchIds() {
        return searchIds;
    }

    /**
     * Returns responseIds
     */
    public int[] getResponseIds() {
        return responseIds;
    }

    /**
     * Returns dataLength
     */
    public int[] getFlowDataLength() {
        return flowDataLength;
    }

    /**
     * Returns getIdType
     */
    public int[] getIdType() {
        return idType;
    }

    /**
     * Returns an array of data bytes for the first pair of search and response ids
     */
    public byte[] getDataBytes1() {
        return dataBytes1;
    }

    /**
     * Returns an array of data bytes for the second pair of search and response ids
     */
    public byte[] getDataBytes2() {
        return dataBytes2;
    }

    /**
     * Returns an array of data bytes for the third pair of search and response ids
     */
    public byte[] getDataBytes3() {
        return dataBytes3;
    }

    /**
     * Returns an array of data bytes for the fourth pair of search and response ids
     */
    public byte[] getDataBytes4() {
        return dataBytes4;
    }

    /**
     * Returns an array of data bytes for the fifth pair of search and response ids
     */
    public byte[] getDataBytes5() {
        return dataBytes5;
    }

    /**
     * Returns an array of data bytes for the sixth pair of search and response ids
     */
    public byte[] getDataBytes6() {
        return dataBytes6;
    }

    /**
     * Returns an array of data bytes for the seventh pair of search and response ids
     */
    public byte[] getDataBytes7() {
        return dataBytes7;
    }


}
