package com.micronet.canbus;

interface CanbusListener {
	 public void onPacketReceive1939Port1(CanbusFramePort1 frame);
	 public void onPacketReceive1939Port2(CanbusFramePort2 frame);
	 public void onPacketReceiveJ1708Port(J1708Frame frame);
}
