package com.qrt.factory.domain;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;

import com.qrt.factory.R;
import com.qrt.factory.TestSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * ********************************************************
 * Name：TestStatusBase.java
 * Author：Wangwenlong
 * Date：2013-09-14
 * Purpose：
 * Declaration：QRT Telecom Technology Co., LTD
 * **********************************************************
 */

public class TestStatusBase {

    private static final String TAG = "QrtFactoryTestStatus";
    private static final int PCBA_TEST_STATUS_INDEX = 66;
    private static final int PHONE_TEST_STATUS_INDEX = 67;
    private Context mContext = null;
    private int defaultModel = -1;
    private byte pcbaTestStatus = 0;
    private byte phoneTestStatus = 0;
    private TestStatusCDMA testStatusCDMA;
    private TestStatusGSM testStatusGSM;
    private TestStatusWCDMA testStatusWCDMA;
    private TestStatusTDSCDMA testStatusTDSCDMA;
    private TestStatusTDDLTE testStatusTDDLTE;
    private TestStatusFDDLTE testStatusFDDLTE;

    public TestStatusBase(Context applicationContext, int defaultModel) {
        mContext = applicationContext;
        this.defaultModel = defaultModel;
        byte[] buffer = loadByteFromTestStatusFile("/persist/.sn.bin");
        switch (defaultModel) {
            case 0: //C+G
                testStatusCDMA = new TestStatusCDMA(buffer);
                testStatusGSM = new TestStatusGSM(buffer);
                break;
            case 1: //W+G
                testStatusWCDMA = new TestStatusWCDMA(buffer);
                testStatusGSM = new TestStatusGSM(buffer);
                break;
            case 2: //C
                testStatusCDMA = new TestStatusCDMA(buffer);
                break;
            case 3: //W
                testStatusWCDMA = new TestStatusWCDMA(buffer);
                break;
            case 4: //GSM+TDSCDMA+TDDLTD
                testStatusGSM = new TestStatusGSM(buffer);
                testStatusTDSCDMA = new TestStatusTDSCDMA(buffer);
                testStatusWCDMA = new TestStatusWCDMA(buffer);//Add by zhangkaikai for WCDMA QL810 20141017
                testStatusTDDLTE = new TestStatusTDDLTE(buffer);
                break;
              //Add by zhangkaikai to delete TDSCDMA QL602_YUSUN SW00081764 20140922 begin
            case 5: //GSM+TDDLTD
                testStatusGSM = new TestStatusGSM(buffer);
                testStatusTDSCDMA = new TestStatusTDSCDMA(buffer);
                testStatusTDDLTE = new TestStatusTDDLTE(buffer);
                break;
              //Add by zhangkaikai to delete TDSCDMA QL602_YUSUN SW00081764 20140922 end
            /*qrt added by xuegang for P2170 Factory Test Mode 20141208 begin*/
            case 6: //GSM+TDDLTD
                testStatusGSM = new TestStatusGSM(buffer);
                testStatusTDSCDMA = new TestStatusTDSCDMA(buffer);
                testStatusWCDMA = new TestStatusWCDMA(buffer);
				testStatusCDMA = new TestStatusCDMA(buffer);
                testStatusTDDLTE = new TestStatusTDDLTE(buffer);
				testStatusFDDLTE = new TestStatusFDDLTE(buffer);
                break; 
            case 7: //GSM+TDDLTD
                testStatusGSM = new TestStatusGSM(buffer);
                testStatusTDSCDMA = new TestStatusTDSCDMA(buffer);
                testStatusWCDMA = new TestStatusWCDMA(buffer);
				testStatusCDMA = new TestStatusCDMA(buffer);
                testStatusTDDLTE = new TestStatusTDDLTE(buffer);
				testStatusFDDLTE = new TestStatusFDDLTE(buffer);
                break;  
            case 8: 
                testStatusGSM = new TestStatusGSM(buffer);
                testStatusWCDMA = new TestStatusWCDMA(buffer);
				testStatusCDMA = new TestStatusCDMA(buffer);
                testStatusTDDLTE = new TestStatusTDDLTE(buffer);
				testStatusFDDLTE = new TestStatusFDDLTE(buffer);
                break; 	
            case 9: 
                testStatusGSM = new TestStatusGSM(buffer);
                testStatusWCDMA = new TestStatusWCDMA(buffer);
				testStatusCDMA = new TestStatusCDMA(buffer);
                testStatusTDDLTE = new TestStatusTDDLTE(buffer);
				testStatusFDDLTE = new TestStatusFDDLTE(buffer);
                break; 	
            case 10: 
                testStatusGSM = new TestStatusGSM(buffer);
                testStatusWCDMA = new TestStatusWCDMA(buffer);
				testStatusTDSCDMA = new TestStatusTDSCDMA(buffer);
                testStatusTDDLTE = new TestStatusTDDLTE(buffer);
				testStatusFDDLTE = new TestStatusFDDLTE(buffer);
                break; 					
			/*qrt added by xuegang for P2170 Factory Test Mode 20141208 end*/	
        }
        if (buffer != null) {
            pcbaTestStatus = buffer[PCBA_TEST_STATUS_INDEX];
            phoneTestStatus = buffer[PHONE_TEST_STATUS_INDEX];
        }
    }

    public static boolean setFactoryTestStatus(boolean allPass, boolean smt) {
        /*SnFileByte snFileByte = loadByteFromTestStatusFile();
        int offset = smt ? PCBA_TEST_STATUS_INDEX : PHONE_TEST_STATUS_INDEX;
        snFileByte.changeTestByte(offset, (byte) (allPass ? 1 : 0));

        return writeByteToTestStatusFile(snFileByte, "/persist/.sn.bin.bak") & writeByteToTestStatusFile(snFileByte, "/persist/.sn.bin");*/

        File file = null;

        if (smt) {
            file = new File("/persist/PCBA.FLG");
        } else {
            file = new File("/persist/PHONE.FLG");
        }

        if (file.exists()) {
            if (!allPass) {
                file.delete();
            }
        } else {
            if (allPass) {
                try {
                    return file.createNewFile();
                } catch (IOException ignored) {}
            }
        }
        return true;
    }

    public String getDisplayString() {
        if (defaultModel == -1) {
            return null;
        }

        StringBuffer stringBuffer = new StringBuffer();

        switch (defaultModel) {
            case 0: //C+G
                stringBuffer.append(testStatusCDMA.getAdjustmentDisplayString())
                        .append(testStatusCDMA.getOnexFinalTestDisplayString())
                        .append(testStatusCDMA.getEvdoFinalTestDisplayString())
                        .append(testStatusGSM.getAdjustmentDisplayString())
                        .append(testStatusGSM.getFinalTestDisplayString());
                getFactoryKitTestStatus(stringBuffer);
                stringBuffer.append("\r\n")
                        .append(mContext.getString(R.string.coupling))
                        .append(getCouplingTestString(testStatusCDMA.getCoupling(),
                                testStatusGSM.getCoupling()));
                break;
            case 1: //W+G
                stringBuffer.append(testStatusWCDMA.getAdjustmentDisplayString())
                        .append(testStatusWCDMA.getFinalTestDisplayString())
                        .append(testStatusGSM.getAdjustmentDisplayString())
                        .append(testStatusGSM.getFinalTestDisplayString());
                getFactoryKitTestStatus(stringBuffer);
                stringBuffer.append("\r\n")
                        .append(mContext.getString(R.string.coupling))
                        .append(getCouplingTestString(testStatusWCDMA.getCoupling(),
                                testStatusGSM.getCoupling()));
                break;
            case 2: //C
                stringBuffer.append(testStatusCDMA.getAdjustmentDisplayString())
                        .append(testStatusCDMA.getOnexFinalTestDisplayString())
                        .append(testStatusCDMA.getEvdoFinalTestDisplayString())
                        .append(testStatusCDMA.getCouplingDisplayString());
                getFactoryKitTestStatus(stringBuffer);
                break;
            case 3: //W
                stringBuffer.append(testStatusWCDMA.getAdjustmentDisplayString())
                        .append(testStatusWCDMA.getFinalTestDisplayString())
                        .append(testStatusWCDMA.getCouplingDisplayString());
                getFactoryKitTestStatus(stringBuffer);
                break;
            case 4: //GSM+TDSCDMA+TDDLTD
                stringBuffer.append(testStatusGSM.getAdjustmentDisplayString())
                        .append(testStatusGSM.getFinalTestDisplayString())
                        .append(testStatusGSM.getCouplingDisplayString())//added by xuegang
                        .append(testStatusTDSCDMA.getAdjustmentDisplayString())
                        .append(testStatusTDSCDMA.getFinalTestDisplayString())
                        .append(testStatusTDDLTE.getAdjustmentDisplayString())
                        .append(testStatusTDDLTE.getFinalTestDisplayString());
              /*Add by zhangkaikai for WCDMA QL810 20141017 begin*/
                if(SystemProperties.get("ro.product.model").equals("ASUS_X003")){
                    stringBuffer.append(testStatusWCDMA.getAdjustmentDisplayString())
                    .append(testStatusWCDMA.getFinalTestDisplayString());
                }
                /*Add by zhangkaikai for WCDMA QL810 20141017 end*/
                getFactoryKitTestStatus(stringBuffer);
                stringBuffer.append("\r\n")
                        .append(mContext.getString(R.string.coupling))
                        .append(getCouplingTestString(testStatusTDSCDMA.getCoupling(),
                                testStatusTDDLTE.getCoupling()));
                break;
                //Add by zhangkaikai to delete TDSCDMA QL602_YUSUN SW00081764 20140922 begin
            case 5: //GSM+TDDLTD
                stringBuffer.append(testStatusGSM.getAdjustmentDisplayString())
                        .append(testStatusGSM.getFinalTestDisplayString())
                        .append(testStatusGSM.getCouplingDisplayString())
                        .append(testStatusTDDLTE.getAdjustmentDisplayString())
                        .append(testStatusTDDLTE.getFinalTestDisplayString());
                getFactoryKitTestStatus(stringBuffer);
                stringBuffer.append("\r\n")
                        .append(mContext.getString(R.string.coupling))
                        .append(getCouplingTestString(testStatusTDSCDMA.getCoupling(),
                                testStatusTDDLTE.getCoupling()));
                break;
              //Add by zhangkaikai to delete TDSCDMA QL602_YUSUN SW00081764 20140922 end
            /*qrt added by xuegang for P2170 Factory Test Mode 20141208 begin*/
            case 6: //GSM+TDDLTD+FDD+CDMA+WCDMA
                stringBuffer.append(testStatusGSM.getAdjustmentDisplayString())
                        .append(testStatusGSM.getFinalTestDisplayString())
                        .append(testStatusWCDMA.getAdjustmentDisplayString())
                        .append(testStatusWCDMA.getFinalTestDisplayString())
                        .append(testStatusCDMA.getAdjustmentDisplayString())
                        .append(testStatusCDMA.getEvdoFinalTestDisplayString())
                        .append(testStatusTDSCDMA.getAdjustmentDisplayString())
                        .append(testStatusTDSCDMA.getFinalTestDisplayString())
                        .append(testStatusTDDLTE.getAdjustmentDisplayString())
                        .append(testStatusTDDLTE.getFinalTestDisplayString())
                        .append(testStatusFDDLTE.getAdjustmentDisplayString())
                        .append(testStatusFDDLTE.getFinalTestDisplayString())
                        .append(testStatusFDDLTE.getCouplingDisplayString());
				
                getFactoryKitTestStatus(stringBuffer);
                break;		
				case 7: //GSM+TDDLTD+FDD+CDMA+WCDMA
					stringBuffer.append(testStatusGSM.getAdjustmentDisplayString())
							.append(testStatusGSM.getFinalTestDisplayString())
							.append(testStatusGSM.getCouplingDisplayString())//added by xuegang
							.append(testStatusWCDMA.getAdjustmentDisplayString())
							.append(testStatusWCDMA.getFinalTestDisplayString())
							.append(testStatusCDMA.getAdjustmentDisplayString())
							.append(testStatusCDMA.getEvdoFinalTestDisplayString())
							.append(testStatusTDSCDMA.getAdjustmentDisplayString())
							.append(testStatusTDSCDMA.getFinalTestDisplayString())
							.append(testStatusTDDLTE.getAdjustmentDisplayString())
							.append(testStatusTDDLTE.getFinalTestDisplayString())
							.append(testStatusFDDLTE.getAdjustmentDisplayString())
							.append(testStatusFDDLTE.getFinalTestDisplayString());
					
					getFactoryKitTestStatus(stringBuffer);
                    stringBuffer.append("\r\n")
                            .append(mContext.getString(R.string.coupling))
                            .append(getCouplingTestString(testStatusTDSCDMA.getCoupling(),
                                    testStatusTDDLTE.getCoupling()));
					break;		
				case 8: //GSM+TDDLTD+FDD+CDMA+WCDMA
						stringBuffer.append(testStatusGSM.getAdjustmentDisplayString())
								.append(testStatusGSM.getFinalTestDisplayString())
								.append(testStatusGSM.getCouplingDisplayString())//added by xuegang
								.append(testStatusWCDMA.getAdjustmentDisplayString())
								.append(testStatusWCDMA.getFinalTestDisplayString())
								.append(testStatusCDMA.getAdjustmentDisplayString())
								.append(testStatusCDMA.getEvdoFinalTestDisplayString())
								.append(testStatusTDDLTE.getAdjustmentDisplayString())
								.append(testStatusTDDLTE.getFinalTestDisplayString())
								.append(testStatusTDDLTE.getCouplingDisplayString())
								.append(testStatusFDDLTE.getAdjustmentDisplayString())
								.append(testStatusFDDLTE.getFinalTestDisplayString());
						getFactoryKitTestStatus(stringBuffer);
						break;		
            case 9: //GSM+TDDLTD+FDD+CDMA+WCDMA
                stringBuffer.append(testStatusGSM.getAdjustmentDisplayString())
                        .append(testStatusGSM.getFinalTestDisplayString())
                        .append(testStatusWCDMA.getAdjustmentDisplayString())
                        .append(testStatusWCDMA.getFinalTestDisplayString())
                        .append(testStatusCDMA.getAdjustmentDisplayString())
                        .append(testStatusCDMA.getEvdoFinalTestDisplayString())
                        .append(testStatusTDDLTE.getAdjustmentDisplayString())
                        .append(testStatusTDDLTE.getFinalTestDisplayString())
                        .append(testStatusFDDLTE.getAdjustmentDisplayString())
                        .append(testStatusFDDLTE.getFinalTestDisplayString())
                        .append(testStatusFDDLTE.getCouplingDisplayString());
				
                getFactoryKitTestStatus(stringBuffer);
                break;	
            case 10: //GSM+TDD+FDD+TDS+WCDMA
                stringBuffer.append(testStatusGSM.getAdjustmentDisplayString())
                        .append(testStatusGSM.getFinalTestDisplayString())
                        .append(testStatusWCDMA.getAdjustmentDisplayString())
                        .append(testStatusWCDMA.getFinalTestDisplayString())
                        .append(testStatusTDSCDMA.getAdjustmentDisplayString())
                        .append(testStatusTDSCDMA.getFinalTestDisplayString())
                        .append(testStatusTDDLTE.getAdjustmentDisplayString())
                        .append(testStatusTDDLTE.getFinalTestDisplayString())
                        .append(testStatusFDDLTE.getAdjustmentDisplayString())
                        .append(testStatusFDDLTE.getFinalTestDisplayString())
                        .append(testStatusFDDLTE.getCouplingDisplayString());
				
                getFactoryKitTestStatus(stringBuffer);
                break;					
			/*qrt added by xuegang for P2170 Factory Test Mode 20141208 end*/	
			
        }
        /*if (defaultModel == 0) {
            stringBuffer.append(testStatusCDMA.getAdjustmentDisplayString())
                    .append(testStatusCDMA.getOnexFinalTestDisplayString())
                    .append(testStatusCDMA.getEvdoFinalTestDisplayString())
                    .append(testStatusGSM.getAdjustmentDisplayString())
                    .append(testStatusGSM.getFinalTestDisplayString());
            getFactoryKitTestStatus(stringBuffer);
            stringBuffer.append("\r\n")
                    .append(mContext.getString(R.string.coupling))
                    .append(getCouplingTestString(testStatusCDMA.getCoupling(),
                            testStatusGSM.getCoupling()));

        } else if (defaultModel == 1) {
            stringBuffer.append(testStatusWCDMA.getAdjustmentDisplayString())
                    .append(testStatusWCDMA.getFinalTestDisplayString())
                    .append(testStatusGSM.getAdjustmentDisplayString())
                    .append(testStatusGSM.getFinalTestDisplayString());
            getFactoryKitTestStatus(stringBuffer);
            stringBuffer.append("\r\n")
                    .append(mContext.getString(R.string.coupling))
                    .append(getCouplingTestString(testStatusWCDMA.getCoupling(),
                            testStatusGSM.getCoupling()));
        }*/

        return stringBuffer.toString();
    }

    /*private void getFactoryKitTestStatus(StringBuffer stringBuffer) {
        stringBuffer.append("\r\n")
                .append(mContext.getString(R.string.smt_test))
                .append(getTestStringByNvBtye(pcbaTestStatus))
                .append("\r\n")
                .append(mContext.getString(R.string.phone_test))
                .append(getTestStringByNvBtye(phoneTestStatus));
    }*/

    private void getFactoryKitTestStatus(StringBuffer stringBuffer) {
        stringBuffer.append("\r\n")
                .append(mContext.getString(R.string.smt_test))
                .append(getTestStringByFile("/persist/PCBA.FLG"))
                .append("\r\n")
                .append(mContext.getString(R.string.phone_test))
                .append(getTestStringByFile("/persist/PHONE.FLG"));
    }

    private String getTestStringByFile (String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return mContext.getString(R.string.nv_pass);
        } else {
            return mContext.getString(R.string.nv_null);
        }
        /*else {
            return mContext.getString(R.string.nv_fail);
        }*/
    }

    /*private static SnFileByte loadByteFromTestStatusFile() {
        SnFileByte snFileByte = loadByteFromTestStatusFile("/persist/.sn.bin");
        try {
            if (checkFileCRC(snFileByte.getTest(), snFileByte.getCrc())) {
                return snFileByte;
            } else {
                SnFileByte snFileByteBak = loadByteFromTestStatusFile("/persist/.sn.bin.bak");
                if (checkFileCRC(snFileByteBak.getTest(), snFileByteBak.getCrc())) {
                    return snFileByteBak;
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "loadByteFromTestStatusFile error : ", e);
        }
        return snFileByte;
    }*/

    private static byte[] loadByteFromTestStatusFile(String path) {
        byte[] buffer = new byte[128];
        InputStream inStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                Log.w(TAG, path + " not exists");
                return null;
            }
            inStream = new FileInputStream(file);

            Log.d(TAG, String.valueOf(inStream.available()));
            inStream.read(buffer);
            inStream.close();

            Log.d(TAG, path + " = " + Arrays.toString(buffer));
        } catch (IOException e) {
            buffer = null;
            Log.w(TAG, "loadByteFromTestStatusFile error : ", e);
        } catch (Exception ex) {
            buffer = null;
            Log.w(TAG, "loadByteFromTestStatusFile error : ", ex);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    inStream = null;
                }
            }
        }
        return buffer;
    }

    /*private static boolean writeByteToTestStatusFile(SnFileByte snFileByte, String path) {
        byte[] buffer = snFileByte.getTest();
        //byte[] crcBytes = snFileByte.getCrc();
        FileOutputStream outputStream = null;
        try {
            File file = new File(path);
            if (buffer == null || !file.exists()) {
                Log.w(TAG, path + " read error");
                return false;
            }
            outputStream = new FileOutputStream(file, false);

            outputStream.write(buffer, 0, buffer.length);
            //outputStream.write(crcBytes, 0, crcBytes.length);
            outputStream.flush();

            //Add By Wangwenlong to avoid bug for save mmi test error issue (8x26) HQ00000000 2013-09-29
            outputStream.getFD().sync();
            outputStream.close();
            Log.d(TAG, path + " write success");
            return true;
        } catch (IOException e) {
            Log.w(TAG, "writeByteToTestStatusFile error : ", e);
        } catch (Exception ex) {
            Log.w(TAG, "loadByteFromTestStatusFile error : ", ex);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    outputStream = null;
                }
            }
        }
        return false;
    }*/

    /*private static int readInt(byte[] bytes, ByteOrder byteOrder) throws IOException {

        byte[] byteBuf = bytes;

        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return
                    (((byteBuf[0] & 0xff) << 24) | ((byteBuf[1] & 0xff) << 16) |
                            ((byteBuf[2] & 0xff) <<  8) | ((byteBuf[3] & 0xff) <<  0));
        } else {
            return
                    (((byteBuf[3] & 0xff) << 24) | ((byteBuf[2] & 0xff) << 16) |
                            ((byteBuf[1] & 0xff) <<  8) | ((byteBuf[0] & 0xff) <<  0));
        }
    }

    private static long readUnsignedInt(byte[] bytes) throws IOException {
        return ((long)readInt(bytes, ByteOrder.LITTLE_ENDIAN)) & 0xffffffffL;
    }

    private static boolean checkFileCRC(byte[] bytes, byte[] crcValue) throws IOException {
        *//*long crcValue1 = getCRC(bytes);

        long fileCRC = readUnsignedInt(crcValue);

        Log.d(TAG, "CRC value : " + String.valueOf(crcValue1) + " File CRC value : " + String.valueOf(fileCRC));
        return crcValue1 == fileCRC;*//*
        return true;
    }

    public static byte[] writeInt(int v, ByteOrder byteOrder) {
        byte[] byteBuf = new byte[4];
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            byteBuf[0] = (byte)(v >>> 24);
            byteBuf[1] = (byte)(v >>> 16);
            byteBuf[2] = (byte)(v >>>  8);
            byteBuf[3] = (byte)(v >>>  0);
        } else {
            byteBuf[0] = (byte)(v >>>  0);
            byteBuf[1] = (byte)(v >>>  8);
            byteBuf[2] = (byte)(v >>> 16);
            byteBuf[3] = (byte)(v >>> 24);
        }
        return byteBuf;
    }

    private static long getCRC(byte[] bytes) {
        CRC32 crc = new CRC32();
        crc.update(bytes);
        return crc.getValue();
    }*/

    protected String getTestStringByNvBtye(byte b) {
        if (TestSettings.NOT_TEST == b) {
            return mContext.getString(R.string.nv_null);
        } else if (TestSettings.TEST_PASS == b) {
            return mContext.getString(R.string.nv_pass);
        } else {
            return mContext.getString(R.string.nv_fail);
        }
    }

    private String getCouplingTestString(byte b1, byte b2) {
        if (TestSettings.TEST_PASS == b1
                || TestSettings.TEST_PASS == b2) {
            return mContext.getString(R.string.nv_pass);
        } else if (TestSettings.NOT_TEST == b1
                && TestSettings.NOT_TEST == b2) {
            return mContext.getString(R.string.nv_null);
        } else {
            return mContext.getString(R.string.nv_fail);
        }
    }

    private class TestStatusCDMA {

        private static final int ADJUSTMENT_INDEX = 68;
        private static final int ONEX_FINAL_TEST_INDEX = 69;
        private static final int EVDO_FINAL_TEST_INDEX = 70;
        private static final int COUPLING_INDEX = 71;
        private byte adjustment;
        private byte onexFinalTest;
        private byte evdoFinalTest;
        private byte coupling;

        protected TestStatusCDMA(byte[] buffer) {
            if (buffer != null) {
                adjustment = buffer[ADJUSTMENT_INDEX];
                onexFinalTest = buffer[ONEX_FINAL_TEST_INDEX];
                evdoFinalTest = buffer[EVDO_FINAL_TEST_INDEX];
                coupling = buffer[COUPLING_INDEX];
            }
        }

        private byte getCoupling() {
            return coupling;
        }

        public String getAdjustmentDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.cdma_adjustment))
                    .append(getTestStringByNvBtye(adjustment)).toString();
        }

        public String getOnexFinalTestDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.cdma_1x_final_test))
                    .append(getTestStringByNvBtye(onexFinalTest)).toString();
        }

        public String getEvdoFinalTestDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.cdma_evdo_final_test))
                    .append(getTestStringByNvBtye(evdoFinalTest)).toString();
        }

        public String getCouplingDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.cdma_coupling))
                    .append(getTestStringByNvBtye(coupling)).toString();
        }
    }

    private class TestStatusGSM {

        private static final int ADJUSTMENT_INDEX = 72;
        private static final int FINAL_TEST_INDEX = 73;
        private static final int GPRS_FINAL_TEST_INDEX = 74;
        private static final int EDGE_FINAL_TEST_INDEX = 75;
        private static final int COUPLING_INDEX = 76;
        private byte adjustment;
        private byte finalTest;
        private byte gprsFinalTest;
        private byte edgeFinalTest;
        private byte coupling;

        protected TestStatusGSM(byte[] buffer) {
            if (buffer != null) {
                adjustment = buffer[ADJUSTMENT_INDEX];
                finalTest = buffer[FINAL_TEST_INDEX];
                gprsFinalTest = buffer[GPRS_FINAL_TEST_INDEX];
                edgeFinalTest = buffer[EDGE_FINAL_TEST_INDEX];
                coupling = buffer[COUPLING_INDEX];
            }
        }

        private byte getCoupling() {
            return coupling;
        }

        public String getAdjustmentDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.gsm_adjustment))
                    .append(getTestStringByNvBtye(adjustment)).toString();
        }

        public String getFinalTestDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.gsm_final_test))
                    .append(getTestStringByNvBtye(finalTest)).toString();
        }

        public String getCouplingDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.gsm_coupling))
                    .append(getTestStringByNvBtye(coupling)).toString();
        }
    }

    private class TestStatusWCDMA {

        private static final int ADJUSTMENT_INDEX = 77;
        private static final int FINAL_TEST_INDEX = 78;
        private static final int HSPA_FINAL_TEST_INDEX = 79;
        private static final int COUPLING_INDEX = 80;
        private byte adjustment;
        private byte finalTest;
        private byte hspaFinalTest;
        private byte coupling;

        protected TestStatusWCDMA(byte[] buffer) {
            if (buffer != null) {
                adjustment = buffer[ADJUSTMENT_INDEX];
                finalTest = buffer[FINAL_TEST_INDEX];
                hspaFinalTest = buffer[HSPA_FINAL_TEST_INDEX];
                coupling = buffer[COUPLING_INDEX];
            }
        }

        private byte getCoupling() {
            return coupling;
        }

        public String getAdjustmentDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.wcdma_adjustment))
                    .append(getTestStringByNvBtye(adjustment)).toString();
        }

        public String getFinalTestDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.wcdma_final_test))
                    .append(getTestStringByNvBtye(finalTest)).toString();
        }

        public String getCouplingDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.wcdma_coupling))
                    .append(getTestStringByNvBtye(coupling)).toString();
        }
    }
/*
    private static class SnFileByte {
        byte[] test;
        byte[] crc;

        public byte[] getTest() {
            return test;
        }

        public void setTest(byte[] test) {
            this.test = test;
        }

        public byte[] getCrc() {
            return crc;
        }

        public void setCrc(byte[] crc) {
            this.crc = crc;
        }

        public void changeTestByte(int offset, byte b) {
            test[offset] = b;
            *//*long newCRC = getCRC(test);
            crc = writeInt(new Long(newCRC).intValue(), ByteOrder.LITTLE_ENDIAN);
            Log.d(TAG, "CRC btyes value LITTLE_ENDIAN: " + Arrays.toString(crc));*//*
        }
    }*/

    private class TestStatusTDSCDMA {

        private static final int ADJUSTMENT_INDEX = 92;
        private static final int FINAL_TEST_INDEX = 93;
        private static final int COUPLING_INDEX = 94;
        private byte adjustment;
        private byte finalTest;
        private byte coupling;

        protected TestStatusTDSCDMA(byte[] buffer) {
            if (buffer != null) {
                adjustment = buffer[ADJUSTMENT_INDEX];
                finalTest = buffer[FINAL_TEST_INDEX];
                coupling = buffer[COUPLING_INDEX];
            }
        }

        private byte getCoupling() {
            return coupling;
        }

        public String getAdjustmentDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.tdscdma_adjustment))
                    .append(getTestStringByNvBtye(adjustment)).toString();
        }

        public String getFinalTestDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.tdscdma_final_test))
                    .append(getTestStringByNvBtye(finalTest)).toString();
        }
    }

    private class TestStatusTDDLTE {

        private static final int ADJUSTMENT_INDEX = 86;
        private static final int FINAL_TEST_INDEX = 87;
        private static final int COUPLING_INDEX = 88;
        private byte adjustment;
        private byte finalTest;
        private byte coupling;

        protected TestStatusTDDLTE(byte[] buffer) {
            if (buffer != null) {
                adjustment = buffer[ADJUSTMENT_INDEX];
                finalTest = buffer[FINAL_TEST_INDEX];
                coupling = buffer[COUPLING_INDEX];
            }
        }

        private byte getCoupling() {
            return coupling;
        }

        public String getAdjustmentDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.tddlte_adjustment))
                    .append(getTestStringByNvBtye(adjustment)).toString();
        }

        public String getFinalTestDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.tddlte_final_test))
                    .append(getTestStringByNvBtye(finalTest)).toString();
        }

        public String getCouplingDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.tddlte_coupling))
                    .append(getTestStringByNvBtye(coupling)).toString();
        }			
    }

    private class TestStatusFDDLTE {

        private static final int ADJUSTMENT_INDEX = 89;
        private static final int FINAL_TEST_INDEX = 90;
        private static final int COUPLING_INDEX = 91;
        private byte adjustment;
        private byte finalTest;
        private byte coupling;

        protected TestStatusFDDLTE(byte[] buffer) {
            if (buffer != null) {
                adjustment = buffer[ADJUSTMENT_INDEX];
                finalTest = buffer[FINAL_TEST_INDEX];
                coupling = buffer[COUPLING_INDEX];
            }
        }

        private byte getCoupling() {
            return coupling;
        }

        public String getAdjustmentDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.fddlte_adjustment))
                    .append(getTestStringByNvBtye(adjustment)).toString();
        }

        public String getFinalTestDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.fddlte_final_test))
                    .append(getTestStringByNvBtye(finalTest)).toString();
        }

        public String getCouplingDisplayString() {
            return new StringBuffer().append("\r\n")
                    .append(mContext.getString(R.string.fddlte_coupling))
                    .append(getTestStringByNvBtye(coupling)).toString();
        }		
    }
}
