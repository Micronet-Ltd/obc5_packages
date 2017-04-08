package com.micronet.canbus;

/**
 * Created by brigham.diaz on 1/27/2017.
 */

interface ICanbusInterfaceBridge {
        public void create(CanbusHardwareFilter[] hardwareFilters);
        //added after adding CanbusI/F
        public void create(boolean listeningMode,CanbusHardwareFilter[] hardwareFilters);
        public void remove();
        public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters);
        public void setFilters(CanbusHardwareFilter[] hardwareFilters);
        public CanbusSocket createSocket();
        public void setListeningMode(boolean listeningModeEnable, CanbusHardwareFilter[] hardwareFilters);
        /*public void sendRecovery(int action);*/
}
