package com.micronet.canbus;

/**
 * Created by brigham.diaz on 1/27/2017.
 */

interface ICanbusInterfaceBridge {
        public void create(CanbusHardwareFilter[] hardwareFilters);
        public void create(boolean listeningMode,CanbusHardwareFilter[] hardwareFilters);
        public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters);
        public void remove();
        public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters);
        public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters);
        public CanbusSocket createSocket();

}
