package com.micronet.canbus;

interface ICanbusInterfaceBridge {

        public void create(CanbusHardwareFilter[] hardwareFilters,int portNumber );
        public void create(CanbusHardwareFilter[] hardwareFilters,int portNumber,CanbusFlowControl[] flowControls );

        public void create(boolean listeningMode,CanbusHardwareFilter[] hardwareFilters, int portNumber);
        public void create(boolean listeningMode,CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControls);

        public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber);
        public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControls);

        public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters,int portNumber);
        public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters,int portNumber,CanbusFlowControl[] flowControls);

        public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters,int portNumber);
        public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters,int portNumber,CanbusFlowControl[] flowControls);

        public CanbusSocket createSocketCAN1();
        public CanbusSocket createSocketCAN2();
        public CanbusSocket createSocketJ1708();

        public void remove();


}
