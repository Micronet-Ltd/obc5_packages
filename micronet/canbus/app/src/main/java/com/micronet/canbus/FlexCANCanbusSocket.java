package com.micronet.canbus;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FlexCANCanbusSocket extends CanbusSocket implements CanbusListener{
    private static final String TAG = "CanbusSocket";

    private int mSocket;
    //TODO: how many elements ??
    // Needs enough to prevent blocking insertion.
    BlockingQueue<CanbusFrame> mQueue = new LinkedBlockingQueue<CanbusFrame>(2000);
    BlockingQueue<J1708Frame> mQueueJ1708 = new LinkedBlockingQueue<J1708Frame>(2000);

    /**
     * Creates Canbus socket.
     */
    protected FlexCANCanbusSocket(int fd) {
        mSocket = fd;
    }

    /**
     * Reads Canbus frame. Will block the calling thread until data
     * is written to Canbus socket.
     */
    public CanbusFrame read(){

        CanbusFrame frame = null;
        try {
            frame = mQueue.take();
        } catch (InterruptedException e) {
            Log.e(TAG, "read queue error !!");
            e.printStackTrace();
        }

        return frame;
    }

    /**
     * Reads Canbus frame. Will block the calling thread until data
     * is written to Canbus socket or timeout has elapsed.
     *
     * @param timeout how long to wait before giving up, in units of milliseconds
     *
     * @return the head Canbus frame, or null if th specified waiting time elapses before Canbus frame is available
     */
    public CanbusFrame read(long timeout) {

        CanbusFrame frame = null;

        try {
            frame = mQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "read queue error !!");
            e.printStackTrace();
        }

        return frame;
    }

    @Override
    public void onPacketReceive(CanbusFrame frame) {
        if(!mQueue.offer(frame))
            Log.e(TAG, "Unable to put frame, dropping.");

    }

    /**
     * Sends Canbus frame through socket.
     */
    public void write(CanbusFrame frame){
        send(mSocket, frame);
    }

    /**
     * Sends J1708 frame through socket.
     */
    public void writeJ1708(J1708Frame frame){
        sendJ1708(mSocket, frame);
    }

    /**
     * Opens Canbus socket for read/write operations.
     */
    public void open() {
        setPacketListener(this);
    }

    /**
     * Closes Canbus socket.
     */
    public void close(){
        closeSocket();
    }

    /**
     * Returns Canbus socket id.
     */
    public int getId(){
        return mSocket;
    }

    private void setPacketListener(CanbusListener listener) {
        registerCallback(listener);
    }

    private native int send(int socket, CanbusFrame frame);
    private native int sendJ1708(int socket, J1708Frame frame);
    private native int registerCallback(CanbusListener listener);
    private native int closeSocket();

    static
    {
        System.loadLibrary("canbus");
    }

}
