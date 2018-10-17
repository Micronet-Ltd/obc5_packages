package com.micronet.canbus;

interface IVehicleInterfaceBridge {

        void create(CanbusHardwareFilter[] hardwareFilters, int portNumber) throws CanbusException;
        void create(CanbusHardwareFilter[] hardwareFilters, int portNumber, CanbusFlowControl[] flowControls)
                throws CanbusException;

        void create(boolean listeningMode, CanbusHardwareFilter[] hardwareFilters, int portNumber)
                throws CanbusException;
        void create(boolean listeningMode, CanbusHardwareFilter[] hardwareFilters, int portNumber, CanbusFlowControl[] flowControls)
                throws CanbusException;

        void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber)
                throws CanbusException;
        void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber, CanbusFlowControl[] flowControls)
                throws CanbusException;

        void setBitrate(int bitrate, CanbusHardwareFilter[] hardwareFilters, int portNumber);
        void setBitrate(int bitrate, CanbusHardwareFilter[] hardwareFilters, int portNumber, CanbusFlowControl[] flowControls);

        void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber);
        void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber, CanbusFlowControl[] flowControls);

        CanbusSocket createSocketCAN1();
        CanbusSocket createSocketCAN2();

        void removeCAN1();
        void removeCAN2();

        J1708Socket createSocketJ1708();
        int createJ1708();
        int removeJ1708();



}
