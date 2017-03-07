package com.yihang.gesture.service;

public class ServerManager {

	private static ServerManager servermanger = null;

	private ServerManager() {
	}

	static {
		System.loadLibrary("Gesture");
	}

	public static ServerManager getInstance() {
		if (servermanger == null) {
			servermanger = new ServerManager();
		}
		return servermanger;
	}

	public native int GetGesture();

	public native int Closetp();
	
	public native int Opentp();
	
	public native int CloseGloveMode();
	
	public native int OpenGloveMode();

}
