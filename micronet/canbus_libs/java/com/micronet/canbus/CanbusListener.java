package com.micronet.canbus;

interface CanbusListener {
	 public void onPacketReceive(CanbusFrame frame);
	 public void onPacketReceiveJ1708(J1708Frame frame);
}
