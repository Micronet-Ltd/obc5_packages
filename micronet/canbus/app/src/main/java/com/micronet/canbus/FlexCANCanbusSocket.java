package com.micronet.canbus;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Handles the read/write Canbus communication via CanSocket interface.
 * CanbusSocketPort1 can be created when Canbus interface is available.
 */
class FlexCANCanbusSocket extends CanbusSocket implements CanbusListenerPort1, CanbusListenerPort2{
    private static final String TAG = "CanbusSocketPort1";

    private int mSocket1;
    private int mSocket2;
    private int mSocket3;

    //TODO: how many elements ??
    // Needs enough to prevent blocking insertion.
    BlockingQueue<CanbusFramePort1> mQueuej1939Port1 = new LinkedBlockingQueue<CanbusFramePort1>(2000);
    BlockingQueue<CanbusFramePort2> mQueuej1939Port2 = new LinkedBlockingQueue<CanbusFramePort2>(2000);
    BlockingQueue<J1708Frame> mQueueJ1708 = new LinkedBlockingQueue<J1708Frame>(2000);

    /**
     * Creates Canbus socket.
     */
    protected FlexCANCanbusSocket(int fd, int port){
        if (port==2) {mSocket1 = fd;}
        else if (port==3){mSocket2 = fd;}
        else if (port==3){mSocket3 = fd;}
    }

    /**
     * Reads Canbus frame. Will block the calling thread__port1 until data
     * is written to Canbus socket.
     */
    public CanbusFramePort1 readPort1(){

        CanbusFramePort1 frame = null;
        try {
            frame = mQueuej1939Port1.take();
        } catch (InterruptedException e) {
            Log.e(TAG, "readPort1 queue error !!");
            e.printStackTrace();
        }

        return frame;
    }

    /**
     * Reads Canbus frame. Will block the calling thread__port1 until data
     * is written to Canbus socket or timeout has elapsed.
     *
     * @param timeout how long to wait before giving up, in units of milliseconds
     *
     * @return the head Canbus frame, or null if th specified waiting time elapses before Canbus frame is available
     */
    public CanbusFramePort1 readPort1(long timeout) {

        CanbusFramePort1 frame = null;

        try {
            frame = mQueuej1939Port1.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "readPort1 queue error !!");
            e.printStackTrace();
        }
        Log.d(TAG,"Returning a frame from CanbusFramePort1 readPort2(timeout)");
        return frame;
    }

    /**
     * Reads Canbus frame. Will block the calling thread__port1 until data
     * is written to Canbus socket.
     */
    public CanbusFramePort2 readPort2(){

        CanbusFramePort2 frame = null;
        try {
            frame = mQueuej1939Port2.take();
        } catch (InterruptedException e) {
            Log.e(TAG, "readPort2 queue error !!");
            e.printStackTrace();
        }

        return frame;
    }

    /**
     * Reads Canbus frame. Will block the calling thread__port1 until data
     * is written to Canbus socket or timeout has elapsed.
     *
     * @param timeout how long to wait before giving up, in units of milliseconds
     *
     * @return the head Canbus frame, or null if th specified waiting time elapses before Canbus frame is available
     */
    public CanbusFramePort2 readPort2(long timeout) {

        CanbusFramePort2 frame = null;

        try {
            frame = mQueuej1939Port2.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "readPort2 queue error !!");
            e.printStackTrace();
        }

        return frame;
    }

    @Override
    public void onPacketReceive1939Port1(CanbusFramePort1 frame) {
        Log.e(TAG, "Received a frame from Port1 in the Queue!");
        if(!mQueuej1939Port1.offer(frame))
            Log.e(TAG, "Unable to push frame from CAN Port 1, frame dropping.");

    }

    @Override
    public void onPacketReceive1939Port2(CanbusFramePort2 frame) {
        Log.e(TAG, "Received a frame from Port2 in the Queue!");
        if(!mQueuej1939Port2.offer(frame))
            Log.e(TAG, "Unable to push frame from CAN Port 2, frame dropping.");
    }

    @Override
    public void onPacketReceiveJ1708Port(J1708Frame frame) {
        Log.e(TAG, "Received a frame from J1708 Port in the Queue!");
        if(!mQueueJ1708.offer(frame))
            Log.e(TAG, "Unable to push frame from J1708 Port, frame dropping.");
    }

    /**
     * Sends Canbus frame to port 1 through socket.
     */
    public void write1939Port1(CanbusFramePort1 frame){
        sendJ1939Port1(mSocket1, frame);
    }

    /**
     * Sends Canbus frame to port 2 through socket.
     */
    public void write1939Port2(CanbusFramePort2 frame){
        sendJ1939Port2(mSocket2, frame);
    }

    /**
     * Sends J1708 frame through socket.
     */
    public void writeJ1708Port(J1708Frame frame){
        sendJ1708(mSocket3, frame);
    }

    /**
     * Opens Canbus socket for readPort1/write1939Port1 operations.
     */
    public void openCan1() {
        setPacketListener(this);
    }

    /**
     * Opens Canbus socket for readPort1/write1939Port1 operations.
     */
    public void openCan2() {
        setPacketListenerCanPort2(this);
    }

    /**
     * Opens Canbus socket for readPort1/write1939Port1 operations.
     */
    public void openJ1708() {
        setPacketListenerCanPort2(this);
    }

    /**
     * Closes Canbus socket for port 1
     */
    public void close1939Port1(){
        closeSocketJ1939Port1();
    }

    /**
     * Closes Canbus socket for port 1
     */
    public void close1939Port2(){
        closeSocketJ1939Port2();
    }

    /**
     * Closes Canbus socket for port 1
     */
    public void close1708Port(){closeSocketJ1939Port2();}


    /**
     * Returns Canbus socket id for port CAN1_TTY
     */
    public int getCan1PortId(){
        return mSocket1;
    }

    /**
     * Returns Canbus socket id for port CAN2_TTY
     */
    public int getCan2PortId(){
        return mSocket2;
    }

    /**
     * Returns Canbus socket id for J1708 port
     */
    public int getJ708PortId(){
        return mSocket2;
    }


    private void setPacketListener(CanbusListenerPort1 listener) {
        registerCallback(listener);
    }

    private void setPacketListenerCanPort2(CanbusListenerPort2 listener) {registerCallbackCanPort2(listener);}

    private void setPacketListenerJ1708Port(CanbusListenerJ1708 listener) {registerCallbackJ1708Port(listener);}

    private native int sendJ1939Port1(int socket, CanbusFramePort1 frame);
    private native int sendJ1939Port2(int socket, CanbusFramePort2 frame);
    private native int sendJ1708(int socket, J1708Frame frame);
    private native int registerCallback(CanbusListenerPort1 listener);
    private native int registerCallbackCanPort2(CanbusListenerPort2 listener);
    private native int registerCallbackJ1708Port(CanbusListenerJ1708 listener);
    private native int closeSocketJ1939Port1();
    private native int closeSocketJ1939Port2();
    private native int closeSocketJ1708();




    static
    {
        System.loadLibrary("canbus");
    }

}
