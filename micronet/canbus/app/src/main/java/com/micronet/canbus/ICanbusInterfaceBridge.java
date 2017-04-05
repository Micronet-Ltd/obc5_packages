package com.micronet.canbus;

/**
 * Created by brigham.diaz on 1/27/2017.
 */

interface ICanbusInterfaceBridge {
        public void create();
        //added after adding CanbusI/F
        public void create(boolean listeningMode);
        public void remove();
        public void setBitrate(int bitrate);
        public void setFilters(CanbusHardwareFilter[] hardwareFilters);
        public CanbusSocket createSocket();
        public void setListeningMode(boolean listeningModeEnable);
        /*public void sendRecovery(int action);*/
}
