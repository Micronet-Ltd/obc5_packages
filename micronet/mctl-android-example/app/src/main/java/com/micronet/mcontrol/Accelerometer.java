package com.micronet.mcontrol;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.sql.Time;

import static android.os.SystemClock.sleep;

/**
 * Created by abid.esmail on 11/7/2016.
 */

public class Accelerometer implements AccelerometerInterface {

    public float accelData[] = new float[]{0.0f, 0.0f, 0.0f};
    boolean running = true;
    //BufferedInputStream reader = new BufferedInputStream(new FileInputStream( "/dev/vaccel" ) );

    Time timestamp;

    public Accelerometer() throws FileNotFoundException {
        this.accelData[0] = 0.0f;
        this.accelData[1] = 0.0f;
        this.accelData[2] = 0.0f;
    }

    //parse timestamp and accelData
    /*
    * Output format:
    *   Data * 10  (timestamp per sample)
    *     <u64> timestamp
    *     <u16> <u16> <u16> x,y,z sample (Bytes swapped for compatability)
    *     <u16>=0 padding
    */
    @Override
    public void getAccel() throws IOException {
        readAccelData();
    }
    private void readAccelData() throws FileNotFoundException {
        short accel_temp;
        InputStream inputStream = new FileInputStream("/dev/vaccel");
        try {
            int bufferSize = 16;
            byte[] accel_data = new byte[bufferSize];

            int len = 0;
            while ((len = inputStream.read(accel_data)) != -1) {
                break;
            }
//            len = readInputStreamWithTimeout(inputStream, accel_data, 5000);
//            if (len == 0){
//                this.accelData[0] = 0;
//                this.accelData[1] = 0;
//                this.accelData[2] = 0;
//                return;
//            }

            accel_temp = (short)(((accel_data[9]<<8) | accel_data[8]));
            this.accelData[0] = get_g_val(accel_temp);

            accel_temp = (short)(((accel_data[11]<<8) | accel_data[10]));
            this.accelData[1] = get_g_val(accel_temp);

            accel_temp = (short)(((accel_data[13]<<8) | accel_data[12]));
            this.accelData[2] = get_g_val(accel_temp);

        }catch (FileNotFoundException e){
            Log.e("mctl accel", " File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("mctl accel", " Cannot read file: " + e.toString());
        }
    }

    public static int readInputStreamWithTimeout(InputStream is, byte[] b, int timeoutMillis)
            throws IOException  {
        int bufferOffset = 0;
        long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < maxTimeMillis && bufferOffset < b.length) {
            int readLength = java.lang.Math.min(is.available(),b.length-bufferOffset);
            // can alternatively use bufferedReader, guarded by isReady():
            int readResult = is.read(b, bufferOffset, readLength);
            //if (readResult == -1) break;
            bufferOffset += readResult;
        }
        return bufferOffset;
    }

    private final int FRAC_2d1 = 5000;
    private final int FRAC_2d2 = 2500;
    private final int FRAC_2d3 = 1250;
    private final int FRAC_2d4 = 625;
    private final int FRAC_2d5 = 313;
    private final int FRAC_2d6 = 156;
    private final int FRAC_2d7 = 78;
    private final int FRAC_2d8 = 39;
    private final int FRAC_2d9 = 20;
    private final int FRAC_2d10 = 10;
    private final int FRAC_2d11 = 5;
    private final int FRAC_2d12 = 2;

    float get_fraction(short val) {
        int fraction = 0;
        float SCALE = 10000;

        if((val & 0x8000) == 0x8000) fraction += FRAC_2d1;
        if((val & 0x4000) == 0x4000) fraction += FRAC_2d2;
        if((val & 0x2000) == 0x2000) fraction += FRAC_2d3;
        if((val & 0x1000) == 0x1000) fraction += FRAC_2d4;

        if((val & 0x0800) == 0x0800) fraction += FRAC_2d5;
        if((val & 0x0400) == 0x0400) fraction += FRAC_2d6;
        if((val & 0x0200) == 0x0200) fraction += FRAC_2d7;
        if((val & 0x0100) == 0x0100) fraction += FRAC_2d8;

        if((val & 0x0080) == 0x0080) fraction += FRAC_2d9;
        if((val & 0x0040) == 0x0040) fraction += FRAC_2d10;

        return ((float) fraction / SCALE);
    }

    float get_g_val(short val) {
        int hi_byte = 0;
        short temp = 0;
        float gVal = 0.0f;

        hi_byte = ((val&0xfffc) & 0xff00) >> 8;
        temp = val;
        if(hi_byte > 0x7f) {
            temp = (short) ((~temp & 0xffff)+1);
            hi_byte = (temp & 0xff00) >> 8;
            gVal = (hi_byte & 0x70) >> 4;
            gVal += get_fraction((short) (temp << 4));
            gVal *= -1;
        }
        else {
            gVal = (hi_byte & 0x70) >> 4;
            gVal += get_fraction((short) (temp << 4));
        }

        return gVal;
    }
}
