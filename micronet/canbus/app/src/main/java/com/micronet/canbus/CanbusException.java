package com.micronet.canbus;

public class CanbusException extends Exception{
    /**
     * The specific error code.
     */
    private int errorCode;

    public CanbusException(String message, int error){
        super(message);
        this.errorCode = error;
    }

    /**
     * Get the specific error code.
     * @return error code
     */
    public int getErrorCode() {
        return errorCode;
    }
}
