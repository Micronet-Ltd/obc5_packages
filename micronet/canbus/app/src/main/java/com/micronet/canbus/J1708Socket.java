package com.micronet.canbus;

public class J1708Socket implements J1708Listener {

    private static final String TAG = "J1708Socket";

    protected J1708Socket(){
        //Mandatory empty constructor
    }

    /**
     * For internal use only.
     */
    @Override
    public void onPacketReceiveJ1708Port(J1708Frame frame) {
    }

    /**
     * Reads J1708 frame. Will block the calling thread until data
     * is written to J1708 socket.
     */
    public J1708Frame readJ1708Port(){
        throw new IllegalArgumentException("Not implemented");
    }

    /**
     * Reads J1708 frame. Will block the calling thread until data
     * is written to J1708 socket or timeout has elapsed.
     *
     * @param timeout how long to wait before giving up, in units of milliseconds
     *
     * @return the head J1708 frame, or null if th specified waiting time elapses before J1708 frame is available
     */
    public J1708Frame readJ1708Port(long timeout){
        throw new IllegalArgumentException("Not implemented");
    }

    /**
     * Sends J1708 frame through socket.
     */
    public void writeJ1708Port(J1708Frame frame){
    }

    /**
     * Opens J1708 socket for read1708/write1708Port operations.
     */
    public void openJ1708() {
    }

    /**
     * Closes J1708 Socket for J1708 information
     */
    public void close1708Port(){
    }

    /**
     * Returns Canbus socket id for J1708 port
     */
    public int getJ708PortId(){
        return -1;
    }


}
