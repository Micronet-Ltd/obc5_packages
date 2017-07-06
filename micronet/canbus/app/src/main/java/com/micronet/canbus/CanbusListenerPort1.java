package com.micronet.canbus;

interface CanbusListenerPort1 {
	 public void onPacketReceive1939Port1(CanbusFramePort1 frame);
	 public void setCan1PacketCount();

}
