package com.micronet.canbus;

interface J1708Listener {
    void onPacketReceiveJ1708Port(J1708Frame frame);
}
