package com.qrt.factory.calibrate;

public class CalibrateServerManager {

	private static CalibrateServerManager servermanger = null;

	private CalibrateServerManager() {
	}

	static {
		System.loadLibrary("Sensor_calibrate");
	}

	public static CalibrateServerManager getInstance() {
		if (servermanger == null) {
			servermanger = new CalibrateServerManager();
		}
		return servermanger;
	}
	
//	public native int Opentp();

	public native int SensorCalibrate(int type);

}