package com.micronet.canbus;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Handles the read/write operations for J1708 communication via J1708Socket
 * FlexCANJ1708 can be created when a J1708 interface is available.
 */
class FlexCANJ1708Socket extends J1708Socket implements J1708Listener {

//    private int mSocket_write;
//    private int mSocket_read;
    private int mSocket_J1708 = 0;
    private static final String TAG = "FlexCANJ1708Socket";

    // Needs enough to prevent blocking insertion.
    BlockingQueue<J1708Frame> mQueueJ1708 = new LinkedBlockingQueue<J1708Frame>(2000);

    /**
     * Creates Canbus socket.
     */
    /*protected FlexCANJ1708Socket(int fd_read, int fd_write){
        mSocket_read = fd_read;
        mSocket_write = fd_write;
    }*/
    protected FlexCANJ1708Socket(int fd_J1708){
        mSocket_J1708 = fd_J1708;
    }

    /**
     * Reads J1708 frame. Will block the calling thread until data
     * is received from J1708 bus.
     */
    public J1708Frame readJ1708Port()
    {
        J1708Frame frame = null;
        try {
            frame = mQueueJ1708.take();
        } catch (InterruptedException e)
        {
            Log.e(TAG, "read j1708 queue error");
            e.printStackTrace();
        }

        return frame;
    }

    /**
     * Reads J1708 frame. Will block the calling thread until data
     * is written to Canbus socket or timeout has elapsed.
     *
     * @param timeout how long to wait before giving up, in units of milliseconds
     *
     * @return the head J1708 frame, or null if th specified waiting time elapses before J1708 frame is available
     */
    public J1708Frame readJ1708Port(long timeout)
    {
        J1708Frame frame = null;
        try {
            frame = mQueueJ1708.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e)
        {
            Log.e(TAG, "read j1708 queue error");
            e.printStackTrace();
        }

        return frame;
    }


    /**
     * Sends J1708 frame through socket.
     */
    public void writeJ1708Port(J1708Frame frame){
        sendJ1708(mSocket_J1708, frame);
    }

    @Override
    public void onPacketReceiveJ1708Port(J1708Frame frame) {
        Log.d(TAG, "Received a frame from J1708 Port in the Queue!");
        if(!mQueueJ1708.offer(frame))
            Log.e(TAG, "Unable to push frame from J1708 Port, frame dropping.");
    }

    /**
     * Opens Canbus socket for readPort1/write1939Port1 operations.
     */
    public void openJ1708() {
        setPacketListenerJ1708Port(this);
    }

    /**
     * Closes J1708 socket for the port
     */
    public void close1708Port(){closeSocketJ1708();}

    /**
     * Returns Canbus socket id for J1708 port
     */
    public int getJ708PortId(){
        return mSocket_J1708;
    }

    private void setPacketListenerJ1708Port(J1708Listener listener) {registerCallbackJ1708Port(listener);}

    private native int sendJ1708(int socket, J1708Frame frame);
    private native int closeSocketJ1708();
    private native int registerCallbackJ1708Port(J1708Listener listener);
}


