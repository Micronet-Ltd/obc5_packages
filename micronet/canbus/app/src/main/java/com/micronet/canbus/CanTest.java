package com.micronet.canbus;

import android.os.SystemClock;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class CanTest {
    private static CanTest instance = null;

    protected CanTest() {
    }

    // Lazy Initialization (If required then only)
    public static CanTest getInstance() {
        if (instance == null) {
            // Thread Safe. Might be costly operation in some case
            synchronized (CanTest.class) {
                if (instance == null) {
                    instance = new CanTest();
                }
            }
        }
        return instance;
    }

    private final static String TAG = "CanTest";

    CanbusFrameType canMessageType;
    CanbusHardwareFilter[] canbusFilter;
    CanbusFlowControl[] canbusFlowControls;

    int canMessageId;
    byte[] canMessageData;
    boolean usersData =false;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String STD = "STD";
    private static final String EXT = "EXT";
    private static final String STD_R = "STD_R";
    private static final String EXT_R = "EXT_R";
    private String txCanMessage="";

    private CanbusInterface canbusInterface;
    private CanbusSocket canbusSocket;


    private J1939Reader j1939Reader = null;
    private volatile boolean blockOnRead = false;

    public StringBuilder canData = new StringBuilder(1000);

    private int j1939IntervalDelay = 500; // ms


    private Thread j1939ReaderThread = null;
    private Thread j1939SendThread = null;

    private final int READ_TIMEOUT = 500; // read timeout (in milliseconds)
    private int baudrate;
    private boolean silentMode;
    private int portNumber;
    private boolean termination;
    private volatile boolean autoSendJ1939;

    private boolean enableFilters = false;
    private boolean enableFlowControls = false;
    private boolean isCanInterfaceOpen = false;
    private boolean discardInBuffer;

    public boolean isDiscardInBuffer() {
        return discardInBuffer;
    }

    public void setDiscardInBuffer(boolean discardInBuffer) {
        this.discardInBuffer = discardInBuffer;
    }

    public boolean isSocketOpen() {
        // there's actually no api call to check status of canbus socket but
        // this app will open the socket as soon as object is initialized.
        // also socket doesn't actually close even with call to QBridgeCanbusSocket.close()
        return canbusSocket != null;
    }

    public boolean isInterfaceOpen() {
        return isCanInterfaceOpen;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public boolean getTermination() {
        return termination;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int port) {this.portNumber = port;}


    public String getVersion() {return Info.VERSION;}

    public void CreateInterface( boolean silentMode, int baudrate,boolean termination, int port) {
        this.silentMode = silentMode;
        this.baudrate = baudrate;
        this.termination=termination;
        this.portNumber=port;

        if (canbusInterface == null) {
            canbusInterface = new CanbusInterface();
            canbusFilter=setFilters();
           // port=getPortNumber();
            canbusFlowControls=setFlowControlMessages();
            canbusInterface.create(silentMode,baudrate,termination,canbusFilter,port,canbusFlowControls);
        }

        if (canbusSocket == null) {
            canbusSocket = canbusInterface.createSocket();
            canbusSocket.open();
        }
        if (discardInBuffer) {
            canbusSocket.discardInBuffer();
    }
        isCanInterfaceOpen = true;
        startThreads();
    }

    public void silentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public CanbusHardwareFilter[] setFilters() {
        enableFilters = true;
        ArrayList<CanbusHardwareFilter> filterList = new ArrayList<CanbusHardwareFilter>();
        CanbusHardwareFilter[] filters;

        // Up to 24 filters.
        int[] ids = new int[]{123, 61444, 61443, 65248, 65276, 61445, 65262, 65266, 60416 , 60160, 61444};
        int[] mask = {0x100,0x1F000000,0x1FF00000};
        int[] type={CanbusHardwareFilter.STANDARD, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED};

        filterList.add(new CanbusHardwareFilter(ids,mask, type));
        filters = filterList.toArray(new CanbusHardwareFilter[0]);

        return filters;
    }

    public CanbusFlowControl[] setFlowControlMessages(){

        enableFlowControls = true;
        ArrayList<CanbusFlowControl> flowControlMessagesList = new ArrayList<CanbusFlowControl>();
        CanbusFlowControl[] flowControlMessages;

        int[] ids = new int[]{123, 61444, 61443};
        int[]responseIds= new int[]{65248, 65276, 61445};
        int[] dataLength = {8,8,8};
        int[] type={CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED};
        //String[] data={"1234567812345678","1234567812345678", "1234567812345678"};
        byte[] data1=new byte[]{0x12,0x34,0x56,0x78,0x1f,0x2f,0x3f,0x4f};
        byte[] data2=new byte[]{0x12,0x34,0x56,0x78,0x1f,0x2f,0x3f,0x4f};
        byte[] data3=new byte[]{0x12,0x34,0x56,0x78,0x1f,0x2f,0x3f,0x4f};
        byte[][] databytes=new byte[][]{data1, data2,data3 };

        flowControlMessagesList.add(new CanbusFlowControl(ids,responseIds,type,dataLength,databytes /*data*/));
        flowControlMessages = flowControlMessagesList.toArray(new CanbusFlowControl[0]);
        return flowControlMessages;
    }

    public void clearFilters() {
        // re-init the interface to clear filters
        enableFilters = false;
        CreateInterface(silentMode, baudrate, termination, portNumber);
    }

    public void discardInBuffer() {
        canbusSocket.discardInBuffer();
    }

    private void startThreads() {
        if (j1939Reader == null) {
            j1939Reader = new J1939Reader();
        }

        j1939Reader.clearValues();

        if (j1939ReaderThread == null || j1939ReaderThread.getState() != Thread.State.NEW) {
            j1939ReaderThread = new Thread(j1939Reader);
        }

        j1939ReaderThread.setPriority(Thread.NORM_PRIORITY + 3);
        j1939ReaderThread.start();

     /*   // For J1708 version of library
        if (j1708Reader == null) {
            j1708Reader = new J1708Reader();
        }

        j1708Reader.clearValues();

        if (j1708ReaderThread == null || j1708ReaderThread.getState() != Thread.State.NEW) {
            j1708ReaderThread = new Thread(j1708Reader);
        }
        j1708ReaderThread.start();*/
    }

    public void closeInterface() {
        if (canbusInterface != null) {
            canbusInterface.remove();
            canbusInterface = null;
        }
        isCanInterfaceOpen = false;
    }
    public void closeSocket() {
        if (isSocketOpen()) {
            canbusSocket.close();
            canbusSocket = null;

            if (j1939ReaderThread != null && j1939ReaderThread.isAlive()) {
                j1939ReaderThread.interrupt();
                try {
                    j1939ReaderThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /// J1939 Canbus Reader
    public int getCanbusFrameCount() {
        return j1939Reader.getCanbusFrameCount();
    }

    public int getCanbusByteCount() {
        return j1939Reader.getCanbusByteCount();
    }

    public int getCanbusRollovers() {
        return j1939Reader.getRollovers();
    }

    public int getCanbusMaxdiff() {
        return j1939Reader.getMaxdiff();
    }

    public boolean isAutoSendJ1939() {
        return autoSendJ1939;
    }

    public void setAutoSendJ1939(boolean autoSendJ1939) {
        this.autoSendJ1939 = autoSendJ1939;
    }
/*
    public boolean isAutoSendJ1708() {
        return autoSendJ1708;
    }

    public void setAutoSendJ1708(boolean autoSendJ1708) {
        this.autoSendJ1708 = autoSendJ1708;
    }*/

    public void setBlockOnRead(boolean blockOnRead) {
        this.blockOnRead = blockOnRead;
    }

    private class J1939Reader implements Runnable {
        private volatile int canbusFrameCount = 0;
        private volatile int canbusByteCount = 0;
        private volatile int rollovers = 0;
        private volatile int maxdiff = 0;

        public int getCanbusFrameCount() {
            return canbusFrameCount;
        }

        public int getCanbusByteCount() {
            return canbusByteCount;
        }

        public int getRollovers() {
            return rollovers;
        }

        public int getMaxdiff() {
            return maxdiff;
        }

        public void clearValues() {
            canbusByteCount = 0;
            canbusFrameCount = 0;
            rollovers = 0;
            maxdiff = 0;
        }

        @Override
        public void run() {
            // J1939 data unit:
            // 3 bit - priority
            // 1 bit - reserved
            // 1 bit - data page
            // 8 bit - PDU format
            // 8 bit - PDU specific
            // 8 bit - source address
            // We need to keep only PDU format + PDU specific, and mask out everything else
            /*final int mask = 0b000_0_0_11111111_11111111_00000000;*/
            final int mask = 0b000_1_1_11111111_11111111_00000000; //Included Reserved an DP
            //final int mask =0x03FFFF00;

            while (true) {
                CanbusFrame canbusFrame1 = null;
                try {
                    if (blockOnRead) {
                        canbusFrame1 = canbusSocket.read();
                    } else {
                        canbusFrame1 = canbusSocket.read(READ_TIMEOUT);
                    }
                    if (canbusFrame1 != null) {
                        long time = SystemClock.elapsedRealtime();
                        int pgn = ((canbusFrame1.getId() & mask)  >> 8);

                        String canFrameType="";
                        if(canbusFrame1.getType() == CanbusFrameType.STANDARD){
                            canFrameType=STD;
                        }
                        else if(canbusFrame1.getType() == CanbusFrameType.EXTENDED){
                            canFrameType=EXT;
                        }
                        else if (canbusFrame1.getType() == CanbusFrameType.STANDARD_REMOTE){
                            canFrameType=STD_R;
                        }
                        else if(canbusFrame1.getType() == CanbusFrameType.EXTENDED_REMOTE){
                            canFrameType=EXT_R;
                        }
                        // done to prevent adding too much text to UI at once
                        if (canData.length() < 500) {
                            // avoiding string.format for performance
                            canData.append(time);
                            canData.append(",");
                            canData.append(Integer.toHexString(canbusFrame1.getId()));
                            canData.append(",");
                            canData.append(canFrameType);
                            canData.append(",");
                            canData.append(Integer.toHexString(pgn));
                            canData.append(",[");
                            canData.append(bytesToHex(canbusFrame1.getData()));
                            canData.append("] (");
                            canData.append(new String(canbusFrame1.getData()));
                            canData.append("),");
                            canData.append(canbusFrame1.getData().length);
                            canData.append("\n");
                        }
                        switch (pgn) {
                            case 65265: // Vehicle Speed (2nd and 3rd byte -> SPN=84)
                                ByteBuffer vehicleSpeed = ByteBuffer.wrap(canbusFrame1.getData(), 1, 2);
                                vehicleSpeed.order(ByteOrder.LITTLE_ENDIAN);
                                int vehicle = vehicleSpeed.getShort() & 0xffff;
                                Log.d(TAG, "Vehicle Speed:" + vehicle);
                                break;

                            case 61444: // Engine Speed (4th and 5th byte -> SPN=190)
                                ByteBuffer engineSpeed = ByteBuffer.wrap(canbusFrame1.getData(), 3, 2);
                                engineSpeed.order(ByteOrder.LITTLE_ENDIAN);
                                int engine = engineSpeed.getShort() & 0xffff;
                                Log.d(TAG, "Engine Speed:" + engine);
                                break;

                            case 61443: // Throttle Position (2nd byte -> SPN=91)
                                ByteBuffer throttlePos = ByteBuffer.wrap(canbusFrame1.getData(), 1, 1);
                                int throttle = throttlePos.get() & 0xff;
                                Log.d(TAG, "Throttle Position:" + throttle);
                                break;

                            case 65248: // Odometer (5th, 6th, 7th and 8th bytes -> SPN=245)
                                ByteBuffer odometerValue = ByteBuffer.wrap(canbusFrame1.getData(), 4, 4);
                                odometerValue.order(ByteOrder.LITTLE_ENDIAN);
                                int odometer = odometerValue.getInt() & 0xffffffff;
                                Log.d(TAG, "Odometer:" + odometer);
                                break;

                            case 65276: // Fuel Level ( 2nd byte -> SPN=96)
                                ByteBuffer fuelLevel = ByteBuffer.wrap(canbusFrame1.getData(), 1, 1);
                                int level = fuelLevel.get() & 0xff;
                                Log.d(TAG, "Fuel Level:" + level);
                                break;

                            case 61445: // Transmission Gear ( 4th byte -> SPN=523)
                                ByteBuffer transmissionGear = ByteBuffer.wrap(canbusFrame1.getData(), 3, 1);
                                int gear = transmissionGear.get() & 0xff;
                                Log.d(TAG, "Transmission Gear:" + gear);
                                break;

                            case 65262: // Engine Coolant Temp ( 1sh byte -> SPN=110)
                                ByteBuffer engineCoolantTemp = ByteBuffer.wrap(canbusFrame1.getData(), 0, 1);
                                int coolantTemp = engineCoolantTemp.get() & 0xff;
                                Log.d(TAG, "Engine Coolant Temp:" + coolantTemp);
                                break;

                            case 65266: // Inst. Fuel Rate (1st and 2nd byte -> SPN=183)
                                ByteBuffer fuelRate = ByteBuffer.wrap(canbusFrame1.getData(), 0, 2);
                                fuelRate.order(ByteOrder.LITTLE_ENDIAN);
                                int rate = fuelRate.getShort() & 0xffff;
                                Log.d(TAG, "Fuel Economy:" + rate);
                                break;

                        }
                        ++canbusFrameCount;
                        canbusByteCount += canbusFrame1.getData().length;
                    } else {
                        Log.d(TAG, "Read timeout");
                    }
                } catch (NullPointerException ex) {
                    // socket is null
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

    public int getJ1939IntervalDelay() {
        return j1939IntervalDelay;
    }

    public void setJ1939IntervalDelay(int j1939IntervalDelay) {
        this.j1939IntervalDelay = j1939IntervalDelay;
    }

    /*public int getJ1708IntervalDelay() {
        return j1708IntervalDelay;
    }

    public void setJ1708IntervalDelay(int j1708IntervalDelay) {
        this.j1708IntervalDelay = j1708IntervalDelay;
    } */

    /* Convert a byte array to a hex friendly string */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }
    public void sendJ1939() {
        if (j1939SendThread == null || !j1939SendThread.isAlive()) {
            j1939SendThread = new Thread(sendJ1939Runnable);
            j1939SendThread.start();
        }
    }

    public void sendJ1939(boolean userData,String messageType, String messageId,String messageData) {
        usersData=userData;
        canMessageData = messageData.getBytes();
        canMessageId = Integer.parseInt(messageId);

        if (messageType.toString() == "T") {
            canMessageType = CanbusFrameType.EXTENDED;
        }
        else if (messageType.toString() == "t") {
            canMessageType = CanbusFrameType.STANDARD;
        }
        else if (messageType == "R") {
            canMessageType = CanbusFrameType.EXTENDED_REMOTE;
        }
        else if (messageType == "r") {
            canMessageType = CanbusFrameType.STANDARD_REMOTE;
        }

        if (j1939SendThread == null || !j1939SendThread.isAlive()) {
            j1939SendThread = new Thread(sendJ1939Runnablle);
            j1939SendThread.start();
        }
    }

    private Runnable sendJ1939Runnable = new Runnable() {
        @Override
        public void run() {
            CanbusFrameType MessageType;
            int MessageId;
            byte[] MessageData;
            do {
                //To send a different type of frame change this to CanbusFrameType.EXTENDED
                MessageType=CanbusFrameType.EXTENDED;
                //A different ID Can be sent by changing the value here
                MessageId=0xFEF2;
                int data = 0;
                ByteBuffer dbuf = ByteBuffer.allocate(8);
                dbuf.order(ByteOrder.LITTLE_ENDIAN);
                dbuf.putInt(data++);
                byte[] a = dbuf.array();
                a[0] = 0x12;
                a[1] = 0x34;
                a[2] = 0x45;
                a[3] = 0x67;
                a[4] = 0x1F;
                a[5] = 0x2F;
                a[6] = 0x3F;
                a[7] = 0x4F;
                MessageData=a;
                        if(canbusSocket != null) {
                            canbusSocket.write(new CanbusFrame(MessageId, MessageData,MessageType));
                }
                try {
                    Thread.sleep(j1939IntervalDelay);
                } catch (InterruptedException e) {
                }
            } while (autoSendJ1939);
        }
    };

    private Runnable sendJ1939Runnablle = new Runnable() {
        @Override
        public void run() {
            do {
                if(canbusSocket != null) {
                    canbusSocket.write(new CanbusFrame(canMessageId, canMessageData,canMessageType));
                }
                try {
                    Thread.sleep(j1939IntervalDelay);
                } catch (InterruptedException e) {
                }
            } while (autoSendJ1939);
        }
    };
    /// End J1939 methods


    
  /*  /// J1708 Reader Thread
    public int getJ1708FrameCount() {
        return j1708Reader.getJ1708FrameCount();
    }

    public boolean isJ1708Supported() {
        return canbusInterface.isJ1708Supported();
    }

    public int getJ1708ByteCount() {
        return j1708Reader.getJ1708ByteCount();
    }*/

    /*private class J1708Reader implements Runnable {
        private volatile int j1708FrameCount = 0;
        private volatile int j1708ByteCount = 0;

        public int getJ1708FrameCount() {
            return j1708FrameCount;
        }

        public int getJ1708ByteCount() {
            return j1708ByteCount;
        }

        public void clearValues() {
            j1708FrameCount = 0;
            j1708ByteCount = 0;
        }


        @Override
        public void run() {
            while (true) {
                J1708Frame j1708Frame = null;
                try {
                    if (blockOnRead) {
                        j1708Frame = canbusSocket.readJ1708();
                    } else {
                        j1708Frame = canbusSocket.readJ1708(READ_TIMEOUT);
                    }

                    if (j1708Frame != null) {
                        long time = SystemClock.elapsedRealtime();
                        // done to prevent adding too much text to UI at once
                        if (j1708Data.length() < 500) {
                            j1708Data.append(time);
                            j1708Data.append(", ");
                            j1708Data.append(Integer.toHexString(j1708Frame.getId()));
                            j1708Data.append(", ");
                            j1708Data.append(Integer.toHexString(j1708Frame.getPriority()));
                            j1708Data.append(", [");
                            j1708Data.append(bytesToHex(j1708Frame.getData()));
                            j1708Data.append("] (");
                            j1708Data.append(new String(j1708Frame.getData()));
                            j1708Data.append("), ");
                            j1708Data.append(j1708Frame.getData().length);
                            j1708Data.append("\n");

                        }
                        ++j1708FrameCount;
                        j1708ByteCount += j1708Frame.getData().length;

                    }
                } catch (NullPointerException ex){
                    // socket is null
                    return;
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }*/

/*
    public void sendJ1708() {
        if (canbusInterface.isJ1708Supported()) {
            if (j1708SendThread == null || !j1708SendThread.isAlive()) {
                j1708SendThread = new Thread(sendJ1708Runnable);
                j1708SendThread.start();
            }
        }
    }
*/

}
