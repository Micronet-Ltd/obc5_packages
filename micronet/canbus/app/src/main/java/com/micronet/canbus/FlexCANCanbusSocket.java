package com.micronet.canbus;

import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by eemaan.siddiqi on 2/11/2017.
 */

public class FlexCANCanbusSocket extends CanbusSocket implements CanbusListener{
    private static final String TAG = "CanbusSocket";

    private int mSocket;
    //TODO: how many elements ??
    // Needs enough to prevent blocking insertion.
    BlockingQueue<CanbusFrame> mQueue = new LinkedBlockingQueue<CanbusFrame>(2000);
    /*BlockingQueue<J1708Frame> mQueueJ1708 = new LinkedBlockingQueue<J1708Frame>(2000);*/

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


    /**
     * Reads J1708 frame. Will block the calling thread until data
     * is received from J1708 bus.
     */
/*    public J1708Frame readJ1708()
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
    }*/


    /**
     * Reads J1708 frame. Will block the calling thread until data
     * is written to Canbus socket or timeout has elapsed.
     *
     * @param timeout how long to wait before giving up, in units of milliseconds
     *
     * @return the head J1708 frame, or null if th specified waiting time elapses before J1708 frame is available
     */
   /* public J1708Frame readJ1708(long timeout)
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
    }*/

    @Override
    public void onPacketReceive(CanbusFrame frame) {
        if(!mQueue.offer(frame))
            Log.e(TAG, "Unable to put frame, dropping.");
    }

    /*@Override
    public void onPacketReceiveJ1708(J1708Frame frame ) {
        if(!mQueueJ1708.offer(frame))
            Log.e(TAG, "Unable to put frame, dropping.");
    }*/

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

    /**
     * Adds software filters to be apply only to the this socket.
    */
    /*public void setFilters(CanbusSoftwareFilter[] filters){
        throw new IllegalArgumentException("Software filter not supported");
    }
*/

    private native int send(int socket, CanbusFrame frame);
    private native int sendJ1708(int socket, J1708Frame frame);
    private native int registerCallback(CanbusListener listener);
    private native int closeSocket();

    static
    {
        System.loadLibrary("canbus");
    }

}
