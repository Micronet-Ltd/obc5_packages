package com.micronet.canbus;

interface ICanbusInterfaceBridge {
        public void create(CanbusHardwareFilter[] hardwareFilters,int portNumber );
        public void create(boolean listeningMode,CanbusHardwareFilter[] hardwareFilters, int portNumber);
        public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber);
        public void remove();
        public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters,int portNumber);
        public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters,int portNumber);
        public CanbusSocket createSocket();

}
