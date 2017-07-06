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

    CanbusHardwareFilter[] canbusFilter;
    CanbusFlowControl[] canbusFlowControls;

    int canMessageIdPort1;
    byte[] canMessageDataPort1;
    boolean usersDataPort1 =false;
    CanbusFrameType canMessageTypePort1;

    int canMessageIdPort2;
    byte[] canMessageDataPort2;
    boolean usersDataPort2 =false;
    CanbusFrameType canMessageTypePort2;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String STD = "STD";
    private static final String EXT = "EXT";
    private static final String STD_R = "STD_R";
    private static final String EXT_R = "EXT_R";
    private String txCanMessage="";

    private CanbusInterface canbusInterface1;
    private CanbusInterface canbusInterface2;
    private CanbusSocket canbusSocket1;
    private CanbusSocket canbusSocket2;


    private J1939Port1Reader j1939Port1Reader = null;
    private J1939Port2Reader j1939Port2Reader = null;
    //private J1708Reader j1708Reader = null;
    private volatile boolean blockOnReadPort1 = false;

    public StringBuilder can1Data = new StringBuilder(1000);
    public StringBuilder can2Data = new StringBuilder(1000);

    private int j1939IntervalDelay = 500; // ms


    private Thread j1939Port1ReaderThread = null;
    private Thread j1939Port1SendThread = null;

    private Thread j1939Port2ReaderThread = null;
    private Thread j1939Port2SendThread = null;

    private final int READ_TIMEOUT = 500; // readPort1 timeout (in milliseconds)
    private int baudrate;
    private boolean removeCan1;
    private boolean removeCan2;
    private boolean silentMode;
    private int portNumber;
    private boolean termination;
    private volatile boolean autoSendJ1939Port1;
    private volatile boolean autoSendJ1939Port2;

    private boolean enableFilters = false;
    private boolean enableFlowControls = false;
    private boolean isCan1InterfaceOpen = false;
    private boolean isCan2InterfaceOpen = false;
    private boolean discardInBuffer;

    public boolean isDiscardInBuffer() {
        return discardInBuffer;
    }

    public void setDiscardInBuffer(boolean discardInBuffer) {
        this.discardInBuffer = discardInBuffer;
    }

    public boolean isPort1SocketOpen() {
        // there's actually no api call to check status of canbus socket but
        // this app will open the socket as soon as object is initialized.
        // also socket doesn't actually close1939Port1 even with call to QBridgeCanbusSocket.close1939Port1()
        return canbusSocket1 != null;
    }

    public boolean isPort2SocketOpen() {
        // there's actually no api call to check status of canbus socket but
        // this app will open the socket as soon as object is initialized.
        // also socket doesn't actually close1939Port1 even with call to QBridgeCanbusSocket.close1939Port1()
        return canbusSocket2 != null;
    }


    public boolean isCAN1InterfaceOpen() {
        return isCan1InterfaceOpen;
    }

    public boolean isCAN2InterfaceOpen() {return isCan2InterfaceOpen;}



    public int getBaudrate() {
        return baudrate;
    }
    public boolean isSilentChecked() {
        return silentMode;
    }

    public boolean getRemoveCan1InterfaceState() {
        return removeCan1;
    }

    public boolean getRemoveCan2InterfaceState() {
        return removeCan2;
    }


    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public void setSilentMode(boolean isSilent) {
        this.silentMode = isSilent;
    }

    public void setRemoveCan1State(boolean removeCan1) {
        this.removeCan1 = removeCan1;
    }
    public void setRemoveCan2State(boolean removeCan2) {
        this.removeCan2 = removeCan2;
    }

    public boolean getTermination() {
        return termination;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int port) {this.portNumber = port;}

    public String getVersion() {return Info.VERSION;}

    public void CreateCanInterface1(boolean silentMode, int baudrate, boolean termination, int port) {
        this.silentMode = silentMode;
        this.baudrate = baudrate;
        this.termination=termination;
        this.portNumber=port;

        if (canbusInterface1 == null  ) {
            canbusInterface1 = new CanbusInterface();
            canbusFilter=setFilters();
            canbusFlowControls=setFlowControlMessages();
            canbusInterface1.create(silentMode,baudrate,termination,canbusFilter,2,canbusFlowControls);
        }

        if (canbusSocket1 == null) {
            canbusSocket1 = canbusInterface1.createSocketCAN1();
            canbusSocket1.openCan1();
        }
        if (discardInBuffer) {
            canbusSocket1.discardInBuffer();
    }
        isCan1InterfaceOpen = true;
        startPort1Threads();
    }


    public void CreateCanInterface2(boolean silentMode, int baudrate, boolean termination, int port) {
        this.silentMode = silentMode;
        this.baudrate = baudrate;
        this.termination=termination;
        this.portNumber=port;

        if (canbusInterface2 == null  ) {
            canbusInterface2 = new CanbusInterface();
            canbusFilter=setFilters();
            canbusFlowControls=setFlowControlMessages();
            canbusInterface2.create(silentMode,baudrate,termination,canbusFilter,3,canbusFlowControls);
        }
        if (canbusSocket2 == null) {
            canbusSocket2 = canbusInterface2.createSocketCAN2();
            canbusSocket2.openCan2();
        }
        if (discardInBuffer) {
            canbusSocket2.discardInBuffer();
        }
        isCan2InterfaceOpen = true;
        startPort2Threads();
    }

    public void silentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public CanbusHardwareFilter[] setFilters() {
        enableFilters = true;
        ArrayList<CanbusHardwareFilter> filterList = new ArrayList<CanbusHardwareFilter>();
        CanbusHardwareFilter[] filters;

        // Up to 24 filters.
        int[] ids = new int[]{123, 177, 61444, 61443, 65248, 65276, 61445, 65262, 65266, 60416 , 60160, 61444};
        int[] mask = {0x100,0x1F0, 0x1F000000,0x1FF00000};
        int[] type={CanbusHardwareFilter.STANDARD,CanbusHardwareFilter.STANDARD, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED, CanbusHardwareFilter.EXTENDED};

        filterList.add(new CanbusHardwareFilter(ids,mask, type));
        filters = filterList.toArray(new CanbusHardwareFilter[0]);

        return filters;
    }

    public CanbusFlowControl[] setFlowControlMessages(){

        enableFlowControls = true;
        ArrayList<CanbusFlowControl> flowControlMessagesList = new ArrayList<CanbusFlowControl>();
        CanbusFlowControl[] flowControlMessages;

        int[] ids = new int[]{61444, 61443, 124};
        int[] responseIds = new int[]{ 65276, 61445, 741};
        int[] dataLength = {8,4,6};
        int[] type={CanbusFlowControl.EXTENDED, CanbusFlowControl.EXTENDED, CanbusFlowControl.STANDARD};
        byte[] data1=new byte[]{0x7f,0x34,0x56,0x78,0x1f,0x2f,0x3f,0x4f};
        byte[] data2=new byte[]{0x1f,0x2f,0x3f,0x4f};
        byte[] data3=new byte[]{0x12,0x34,0x56,0x78,0x1f,0x2f};
        byte[][] databytes=new byte[][]{data1, data2,data3};

        flowControlMessagesList.add(new CanbusFlowControl(ids,responseIds,type,dataLength,databytes));
        flowControlMessages = flowControlMessagesList.toArray(new CanbusFlowControl[0]);
        return flowControlMessages;
    }

    public void clearFilters() {
        // re-init the interface to clear filters
        enableFilters = false;
        CreateCanInterface1(silentMode, baudrate, termination, portNumber);
    }

    public void discardInBuffer() {
        canbusSocket1.discardInBuffer();
    }

    private void startPort1Threads() {
        if (j1939Port1Reader == null) {
            j1939Port1Reader = new J1939Port1Reader();
        }

        j1939Port1Reader.clearValues();

        if (j1939Port1ReaderThread == null || j1939Port1ReaderThread.getState() != Thread.State.NEW) {
            j1939Port1ReaderThread = new Thread(j1939Port1Reader);
        }

        j1939Port1ReaderThread.setPriority(Thread.NORM_PRIORITY + 3);
        j1939Port1ReaderThread.start();
    }

    private void startPort2Threads(){

        //For CAN2_TTY
        if (j1939Port2Reader == null) {
            j1939Port2Reader = new J1939Port2Reader();
        }

        j1939Port2Reader.clearValues();

        if (j1939Port2ReaderThread == null || j1939Port2ReaderThread.getState() != Thread.State.NEW) {
            j1939Port2ReaderThread = new Thread(j1939Port2Reader);
        }

        //j1939Port2ReaderThread.setPriority(Thread.NORM_PRIORITY + 3);
        j1939Port2ReaderThread.start();
    }

    public void closeCan1Interface() {
        if (canbusInterface1 != null) {
            canbusInterface1.removeCAN1();
            canbusInterface1 = null;
        }
        isCan1InterfaceOpen = false;
    }


    public void closeCan2Interface() {
        if (canbusInterface2 != null) {
            canbusInterface2.removeCAN2();
            canbusInterface2 = null;
        }
        isCan2InterfaceOpen = false;
    }

    public void closeCan1Socket() {
        if (isPort1SocketOpen()) {
           canbusSocket1.close1939Port1();
            canbusSocket1 = null;

            if (j1939Port1ReaderThread != null && j1939Port1ReaderThread.isAlive()) {
                j1939Port1ReaderThread.interrupt();
                try {
                    j1939Port1ReaderThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void closeCan2Socket() {
        if (isPort2SocketOpen()) {
            canbusSocket2.close1939Port2();
            canbusSocket2 = null;

            if (j1939Port2ReaderThread != null && j1939Port2ReaderThread.isAlive()) {
                j1939Port2ReaderThread.interrupt();
                try {
                    j1939Port2ReaderThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /// J1939 Canbus Reader
    public int getPort1CanbusFrameCount() {return j1939Port1Reader.getCanbusFrameCount();}

    public int getPort1CanbusByteCount() {
        return j1939Port1Reader.getCanbusByteCount();
    }

    public int getPort1CanbusRollovers() {
        return j1939Port1Reader.getRollovers();
    }

    public int getPort1CanbusMaxdiff() {
        return j1939Port1Reader.getMaxdiff();
    }

    public int getPort2CanbusFrameCount() {return j1939Port2Reader.getCanbusFrameCount();}

    public int getPort2CanbusByteCount() {
        return j1939Port2Reader.getCanbusByteCount();
    }

    public int getPort2CanbusRollovers() {
        return j1939Port2Reader.getRollovers();
    }

    public int getPort2CanbusMaxdiff() {
        return j1939Port2Reader.getMaxdiff();
    }

    public boolean isAutoSendJ1939Port1() {
        return autoSendJ1939Port1;
    }

    public void setAutoSendJ1939Port1(boolean autoSendJ1939Port1) {
        this.autoSendJ1939Port1 = autoSendJ1939Port1;
    }

    public boolean isAutoSendJ1939Port2() {
        return autoSendJ1939Port2;
    }

    public void setAutoSendJ1939Port2(boolean autoSendJ1939Port2) {
        this.autoSendJ1939Port2 = autoSendJ1939Port2;
    }
/*
    public boolean isAutoSendJ1708() {
        return autoSendJ1708;
    }

    public void setAutoSendJ1708(boolean autoSendJ1708) {
        this.autoSendJ1708 = autoSendJ1708;
    }*/

    public void setBlockOnReadPort1(boolean blockOnReadPort1) {
        this.blockOnReadPort1 = blockOnReadPort1;
    }

    private class J1939Port1Reader implements Runnable {
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
                CanbusFramePort1 canbusFrame1 = null;
                try {
                    if (blockOnReadPort1) {
                        canbusFrame1 = canbusSocket1.readPort1();
                    } else {
                        canbusFrame1 = canbusSocket1.readPort1(READ_TIMEOUT);
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
                        if (can1Data.length() < 500) {
                            // avoiding string.format for performance
                            can1Data.append(time);
                            can1Data.append(",");
                            can1Data.append(Integer.toHexString(canbusFrame1.getId()));
                            can1Data.append(",");
                            can1Data.append(canFrameType);
                            can1Data.append(",");
                            can1Data.append(Integer.toHexString(pgn));
                            can1Data.append(",[");
                            can1Data.append(bytesToHex(canbusFrame1.getData()));
                            can1Data.append("] (");
                            can1Data.append(new String(canbusFrame1.getData()));
                            can1Data.append("),");
                            can1Data.append(canbusFrame1.getData().length);
                            can1Data.append("\n");
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

    private class J1939Port2Reader implements Runnable {
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
                CanbusFramePort2 canbusFrame2 = null;
                try {
                    if (blockOnReadPort1) {
                        canbusFrame2 = canbusSocket2.readPort2();
                    } else {
                        canbusFrame2 = canbusSocket2.readPort2(READ_TIMEOUT);
                    }
                    if (canbusFrame2 != null) {
                        long time = SystemClock.elapsedRealtime();
                        int pgn = ((canbusFrame2.getId() & mask)  >> 8);

                        String canFrameType="";
                        if(canbusFrame2.getType() == CanbusFrameType.STANDARD){
                            canFrameType=STD;
                        }
                        else if(canbusFrame2.getType() == CanbusFrameType.EXTENDED){
                            canFrameType=EXT;
                        }
                        else if (canbusFrame2.getType() == CanbusFrameType.STANDARD_REMOTE){
                            canFrameType=STD_R;
                        }
                        else if(canbusFrame2.getType() == CanbusFrameType.EXTENDED_REMOTE){
                            canFrameType=EXT_R;
                        }
                        // done to prevent adding too much text to UI at once
                        if (can2Data.length() < 500) {
                            // avoiding string.format for performance
                            can2Data.append(time);
                            can2Data.append(",");
                            can2Data.append(Integer.toHexString(canbusFrame2.getId()));
                            can2Data.append(",");
                            can2Data.append(canFrameType);
                            can2Data.append(",");
                            can2Data.append(Integer.toHexString(pgn));
                            can2Data.append(",[");
                            can2Data.append(bytesToHex(canbusFrame2.getData()));
                            can2Data.append("] (");
                            can2Data.append(new String(canbusFrame2.getData()));
                            can2Data.append("),");
                            can2Data.append(canbusFrame2.getData().length);
                            can2Data.append("\n");
                        }
                        switch (pgn) {
                            case 65265: // Vehicle Speed (2nd and 3rd byte -> SPN=84)
                                ByteBuffer vehicleSpeed = ByteBuffer.wrap(canbusFrame2.getData(), 1, 2);
                                vehicleSpeed.order(ByteOrder.LITTLE_ENDIAN);
                                int vehicle = vehicleSpeed.getShort() & 0xffff;
                                Log.d(TAG, "Vehicle Speed:" + vehicle);
                                break;

                            case 61444: // Engine Speed (4th and 5th byte -> SPN=190)
                                ByteBuffer engineSpeed = ByteBuffer.wrap(canbusFrame2.getData(), 3, 2);
                                engineSpeed.order(ByteOrder.LITTLE_ENDIAN);
                                int engine = engineSpeed.getShort() & 0xffff;
                                Log.d(TAG, "Engine Speed:" + engine);
                                break;

                            case 61443: // Throttle Position (2nd byte -> SPN=91)
                                ByteBuffer throttlePos = ByteBuffer.wrap(canbusFrame2.getData(), 1, 1);
                                int throttle = throttlePos.get() & 0xff;
                                Log.d(TAG, "Throttle Position:" + throttle);
                                break;

                            case 65248: // Odometer (5th, 6th, 7th and 8th bytes -> SPN=245)
                                ByteBuffer odometerValue = ByteBuffer.wrap(canbusFrame2.getData(), 4, 4);
                                odometerValue.order(ByteOrder.LITTLE_ENDIAN);
                                int odometer = odometerValue.getInt() & 0xffffffff;
                                Log.d(TAG, "Odometer:" + odometer);
                                break;

                            case 65276: // Fuel Level ( 2nd byte -> SPN=96)
                                ByteBuffer fuelLevel = ByteBuffer.wrap(canbusFrame2.getData(), 1, 1);
                                int level = fuelLevel.get() & 0xff;
                                Log.d(TAG, "Fuel Level:" + level);
                                break;

                            case 61445: // Transmission Gear ( 4th byte -> SPN=523)
                                ByteBuffer transmissionGear = ByteBuffer.wrap(canbusFrame2.getData(), 3, 1);
                                int gear = transmissionGear.get() & 0xff;
                                Log.d(TAG, "Transmission Gear:" + gear);
                                break;

                            case 65262: // Engine Coolant Temp ( 1sh byte -> SPN=110)
                                ByteBuffer engineCoolantTemp = ByteBuffer.wrap(canbusFrame2.getData(), 0, 1);
                                int coolantTemp = engineCoolantTemp.get() & 0xff;
                                Log.d(TAG, "Engine Coolant Temp:" + coolantTemp);
                                break;

                            case 65266: // Inst. Fuel Rate (1st and 2nd byte -> SPN=183)
                                ByteBuffer fuelRate = ByteBuffer.wrap(canbusFrame2.getData(), 0, 2);
                                fuelRate.order(ByteOrder.LITTLE_ENDIAN);
                                int rate = fuelRate.getShort() & 0xffff;
                                Log.d(TAG, "Fuel Economy:" + rate);
                                break;

                        }
                        ++canbusFrameCount;
                        canbusByteCount += canbusFrame2.getData().length;
                    } else {
                        Log.d(TAG, "Read timeout for Port 2");
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
    public void sendJ1939Port1() {
        if (j1939Port1SendThread == null || !j1939Port1SendThread.isAlive()) {
            j1939Port1SendThread = new Thread(sendJ1939Port1Runnable);
            j1939Port1SendThread.start();
        }
    }

    public void sendJ1939Port1(boolean userData, String messageType, String messageId, String messageData){
        usersDataPort1 =userData;
        canMessageDataPort1 = messageData.getBytes();
        canMessageIdPort1 = Integer.parseInt(messageId);

        if (messageType.toString() == "T") {
            canMessageTypePort1 = CanbusFrameType.EXTENDED;
        }
        else if (messageType.toString() == "t") {
            canMessageTypePort1 = CanbusFrameType.STANDARD;
        }
        else if (messageType == "R") {
            canMessageTypePort1 = CanbusFrameType.EXTENDED_REMOTE;
        }
        else if (messageType == "r") {
            canMessageTypePort1 = CanbusFrameType.STANDARD_REMOTE;
        }

        if (j1939Port1SendThread == null || !j1939Port1SendThread.isAlive()) {
            j1939Port1SendThread = new Thread(sendJ1939Port1Runnablle);
            j1939Port1SendThread.start();
        }
    }

    private Runnable sendJ1939Port1Runnable = new Runnable() {
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
                        if(canbusSocket1 != null) {
                            canbusSocket1.write1939Port1(new CanbusFramePort1(MessageId, MessageData,MessageType));
                }
                try {
                    Thread.sleep(j1939IntervalDelay);
                } catch (InterruptedException e) {
                }
            } while (autoSendJ1939Port1);
        }
    };

    private Runnable sendJ1939Port1Runnablle = new Runnable() {
        @Override
        public void run() {
            do {
                if(canbusSocket1 != null) {
                    canbusSocket1.write1939Port1(new CanbusFramePort1(canMessageIdPort1, canMessageDataPort1, canMessageTypePort1));
                }
                try {
                    Thread.sleep(j1939IntervalDelay);
                } catch (InterruptedException e) {
                }
            } while (autoSendJ1939Port1);
        }
    };

    public void sendJ1939Port2() {
        if (j1939Port2SendThread == null || !j1939Port2SendThread.isAlive()) {
            j1939Port2SendThread = new Thread(sendJ1939Port2Runnable);
            j1939Port2SendThread.start();
        }
    }

    public void sendJ1939Port2(boolean userData, String messageType, String messageId, String messageData){
        usersDataPort2 =userData;
        canMessageDataPort2 = messageData.getBytes();
        canMessageIdPort2 = Integer.parseInt(messageId);

        if (messageType.toString() == "T") {
            canMessageTypePort2 = CanbusFrameType.EXTENDED;
        }
        else if (messageType.toString() == "t") {
            canMessageTypePort2 = CanbusFrameType.STANDARD;
        }
        else if (messageType == "R") {
            canMessageTypePort2 = CanbusFrameType.EXTENDED_REMOTE;
        }
        else if (messageType == "r") {
            canMessageTypePort2 = CanbusFrameType.STANDARD_REMOTE;
        }

        if (j1939Port2SendThread == null || !j1939Port2SendThread.isAlive()) {
            j1939Port2SendThread = new Thread(sendJ1939Port2Runnablle);
            j1939Port2SendThread.start();
        }
    }

    private Runnable sendJ1939Port2Runnable = new Runnable() {
        @Override
        public void run() {
            CanbusFrameType MessageType;
            int MessageId;
            byte[] MessageData;
            do {
                //To send a different type of frame change this to CanbusFrameType.EXTENDED
                MessageType=CanbusFrameType.EXTENDED;
                //A different ID Can be sent by changing the value here
                MessageId=0xF110;
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
                if(canbusSocket2 != null) {
                    canbusSocket2.write1939Port2(new CanbusFramePort2(MessageId, MessageData,MessageType));
                }
                try {
                    Thread.sleep(j1939IntervalDelay);
                } catch (InterruptedException e) {
                }
            } while (autoSendJ1939Port2);
        }
    };

    private Runnable sendJ1939Port2Runnablle = new Runnable() {
        @Override
        public void run() {
            do {
                if(canbusSocket2 != null) {
                    canbusSocket2.write1939Port2(new CanbusFramePort2(canMessageIdPort2, canMessageDataPort2, canMessageTypePort2));
                }
                try {
                    Thread.sleep(j1939IntervalDelay);
                } catch (InterruptedException e) {
                }
            } while (autoSendJ1939Port2);
        }
    };
    /// End J1939 methods
    
  /*  /// J1708 Reader Thread
    public int getJ1708FrameCount() {
        return j1708Reader.getJ1708FrameCount();
    }

    public boolean isJ1708Supported() {
        return canbusInterface1.isJ1708Supported();
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
                    if (blockOnReadPort1) {
                        j1708Frame = canbusSocket1.readJ1708Port2();
                    } else {
                        j1708Frame = canbusSocket1.readJ1708Port2(READ_TIMEOUT);
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
        if (canbusInterface1.isJ1708Supported()) {
            if (j1708SendThread == null || !j1708SendThread.isAlive()) {
                j1708SendThread = new Thread(sendJ1708Runnable);
                j1708SendThread.start();
            }
        }
    }
*/
}
