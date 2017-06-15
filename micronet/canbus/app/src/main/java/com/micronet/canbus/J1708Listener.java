package com.micronet.canbus;

interface J1708Listener {
    public void onPacketReceiveJ1708Port(J1708Frame frame);
}
