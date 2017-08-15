/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.util.Utilities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.widget.Toast;

/*
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
*/

public class Cradle extends AbstractActivity {

    private static final String TAG = "Cradle Test";

	private int mTestStage;
	private Message msg;
	private int mMsgWhat;


	private static final String BATTERY_STATUS_FILE = "/sys/class/power_supply/battery/status";
	private static final String CRADLE_INDICATOR_FILE = "/sys/class/switch/dock/state";
	private static final String IGNITION_INDICATOR_FILE = "/sys/class/switch/dock/power";


	//private static final int HANDLER_MSG_START_ALL = 0;
	private static final int HANDLER_MSG_START_STAGE = 1;
	//private static final int HANDLER_MSG_FIRST_TEST = 2;
	//private static final int HANDLER_MSG_SECOND_TEST = 3;
	private static final int HANDLER_MSG_END_STAGE = 4;
	private static final int HANDLER_MSG_TEST_REPEATED = 5;
	private static final int HANDLER_MSG_TEST_ONCE = 6;
	private static final int HANDLER_MSG_COUNTDOWN = 7;
	private static final int HANDLER_MSG_DISPLAY_FOR_TEST_ONCE = 8;
	private static final int HANDLER_MSG_STOP_TESTING = 9;

	private static final int STAGE_I = 0;
	private static final int STAGE_II = 1;
	private static final int STAGE_III = 2;
	private static final int MAX_STAGES = 3;

	private static final int NONE = 0;
	private static final int TEXT_PLUS_COUNT_DOWN = 1;
	private static final int TEXT_PLUS_BUTTON = 2;

	private static final int CRADLE_STATE = 0;
	private static final int IGNITION_STATE = 1;
	private static final int BATTERY_STATE = 2;
	private static final int MAX_STATES = 3;
	private static final String[] STAGE_NAMES = {"NO CRADLE", "CRADEL ONLY", "CRADLE + IGNI"};
	private static final boolean[] int2Bool = {false, true};

	//private enum Stages {STAGE_I, STAGE_II, STAGE_III, MAX_STAGES};
	//protected enum States {BUTTERY, CRADLE, IGNITION, MAX_STATES};
	
	private CradleStageStates[] mExpectedStates;
	private boolean[] mResultPass = new boolean[3];
	private TextView mTextView;
	private Button mButton;
	private TextView mDebugText;
	private int mCountdown = 0;
	private int mViewState = NONE;
	private String mWhenDone;

	// names for savedInstanceState 
	static final String TEST_STAGE = "TestStage";
	static final String RESULT_PASS_0 = "ResultPass_0";
	static final String RESULT_PASS_1 = "ResultPass_1";
	static final String RESULT_PASS_2 = "ResultPass_2";
	static final String MSG_WHAT = "MsgWhat";
	static final String COUNT_DOWN = "CountDown";
	static final String VIEW_STATE = "ViewState";
	
	
	int[] countdownMaxes = null;
	String[] firstActions = null;
	String[] secondActions = null;

	@Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mExpectedStates = new CradleStageStates[MAX_STAGES];
		mExpectedStates[STAGE_I] = new CradleStageStates(false, false, false);
		mExpectedStates[STAGE_II] = new CradleStageStates(true, false, false);
		mExpectedStates[STAGE_III] = new CradleStageStates(true, true, true);
		
        setContentView(R.layout.cradle);
		Resources res = getResources();
		//get parameters arrays
		countdownMaxes = res.getIntArray(R.array.CountDownMax);
		firstActions = res.getStringArray(R.array.cradle_first_action);
		secondActions = res.getStringArray(R.array.cradle_second_action);
		mWhenDone = res.getString(R.string.cradle_when_done);
		
		mTextView = (TextView) findViewById(R.id.cradle_msg);
		mDebugText = (TextView) findViewById(R.id.debug_msg);
		mButton = (Button) findViewById(R.id.cradle_done);
		mTextView.setVisibility(View.INVISIBLE);
		mButton.setVisibility(View.INVISIBLE);
		
		if (savedInstanceState != null) {
			// Restore value of members from saved state
			try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>> onCreate loading from savedInstanceState <<<<<<<<<<<<<<<<<<"); } catch (Exception e) { }
			mTestStage = savedInstanceState.getInt(TEST_STAGE);
			mResultPass[STAGE_I] = int2Bool[savedInstanceState.getInt(RESULT_PASS_0)];
			mResultPass[STAGE_II] = int2Bool[savedInstanceState.getInt(RESULT_PASS_1)];
			mResultPass[STAGE_III] = int2Bool[savedInstanceState.getInt(RESULT_PASS_2)];
			mMsgWhat = savedInstanceState.getInt(MSG_WHAT);
			mCountdown = savedInstanceState.getInt(COUNT_DOWN);
			mViewState = savedInstanceState.getInt(VIEW_STATE);
			try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>> saved values: TEST_STAGE: " + mTestStage +
							" MSG_WHAT: " + mMsgWhat + " COUNT_DOWN: " + mCountdown + "RESULT_PASS(0): " + 
							mResultPass[STAGE_I] + " (1): " + mResultPass[STAGE_II]+ " (2): "  + 
							mResultPass[STAGE_III]); } catch (Exception e) { }
			if (mViewState == TEXT_PLUS_COUNT_DOWN) {
				mHandler.sendEmptyMessage(HANDLER_MSG_START_STAGE);
			} else if (mViewState == TEXT_PLUS_BUTTON) {
				mHandler.sendEmptyMessage(HANDLER_MSG_DISPLAY_FOR_TEST_ONCE);
			}

		} else {
			// Probably initialize members with default values for a new instance
			try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>> onCreate no savedInstanceState - initializing <<<<<<<<<<<<<<<<<<"); } catch (Exception e) { }
			mTestStage = 0;
			mResultPass[STAGE_I] = false;
			mResultPass[STAGE_II] = false;
			mResultPass[STAGE_III] = false;
			mMsgWhat = HANDLER_MSG_START_STAGE;
			mCountdown = 0;
			mCountdown = countdownMaxes[mTestStage];
			mViewState = NONE;
			try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>> saved values: TEST_STAGE: " + mTestStage +
							" MSG_WHAT: " + mMsgWhat + " COUNT_DOWN: " + mCountdown + "RESULT_PASS(0): " + 
							mResultPass[STAGE_I] + " (1): " + mResultPass[STAGE_II]+ " (2): "  + 
							mResultPass[STAGE_III]); } catch (Exception e) { }
		}
		
		try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>> in cradle onCreate <<<<<<<<<<<<<<<<<<"); } catch (Exception e) { }
		mHandler.sendEmptyMessage(mMsgWhat);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the user's current game state
		try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>> saving TEST_STAGE: " + mTestStage +
							" MSG_WHAT: " + mMsgWhat + " COUNT_DOWN: " + mCountdown + "RESULT_PASS(0): " + 
							mResultPass[STAGE_I] + " (1): " + mResultPass[STAGE_II]+ " (2): "  + 
							mResultPass[STAGE_III]); } catch (Exception e) { }
		savedInstanceState.putInt(TEST_STAGE, mTestStage);
		savedInstanceState.putInt(MSG_WHAT, mMsgWhat);
		savedInstanceState.putInt(COUNT_DOWN, mCountdown);
		savedInstanceState.putInt(VIEW_STATE, mViewState);
		savedInstanceState.putInt(RESULT_PASS_0, (mResultPass[STAGE_I]) ? 1 : 0);
		savedInstanceState.putInt(RESULT_PASS_1, (mResultPass[STAGE_II]) ? 1 : 0);
		savedInstanceState.putInt(RESULT_PASS_2, (mResultPass[STAGE_III]) ? 1 : 0);

		super.onSaveInstanceState(savedInstanceState);
	}



	private boolean doTheTest() {
		boolean[] cradleState = getCradleAndIgnStates();
		boolean isCradle = cradleState[0];
		boolean isIgn = cradleState[1];
		
		boolean isCharging =  getBatteryState();
		try {Utilities.logd(TAG, ">>>>>>>>>>>> inCradle: " + isCradle + " ignOn: " + 
							isIgn + " isCharging: " + isCharging); } catch (Exception e) { }
		mDebugText.setText("testing stage: " + STAGE_NAMES[mTestStage] + 
						   " isCradle: " + isCradle + " isIgn: " + isIgn + " isCharging: " + isCharging);
		
		if (isCradle == mExpectedStates[mTestStage].getState(CRADLE_STATE) &&
			isIgn == mExpectedStates[mTestStage].getState(IGNITION_STATE) && 
			(mTestStage == STAGE_II || // for testing the cradle may be connected to USB and/or to DC
				isCharging == mExpectedStates[mTestStage].getState(BATTERY_STATE))) {
			return true;
		} else {
			return false;
		}		
	}

	// to show countdown
	private Message buildCountdownMessage(int inCountdown) {
		Message message = new Message();
        message.what = HANDLER_MSG_COUNTDOWN;
        Bundle data = new Bundle();
		data.putInt("countdown", inCountdown);
        message.setData(data);
		return message;
  }

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mMsgWhat = msg.what;
			try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>> current values: TEST_STAGE: " + mTestStage +
							" MSG_WHAT: " + mMsgWhat + " COUNT_DOWN: " + mCountdown + "RESULT_PASS(0): " + 
							mResultPass[STAGE_I] + " (1): " + mResultPass[STAGE_II]+ " (2): "  + 
							mResultPass[STAGE_III]); } catch (Exception e) { }
			switch (msg.what) {
				
			case HANDLER_MSG_START_STAGE:
				if (mTestStage == STAGE_I) {
					// no count, test once
					mHandler.sendEmptyMessage(HANDLER_MSG_TEST_ONCE);
				} else {
					//send to show countdown
					//send to retest within 500 milis
			
					try {Utilities.logd(TAG, ">>>>>>>>>>>>cont: HANDLER_MSG_COUNTDOWN"); } catch (Exception e) { }
					mHandler.sendMessage(buildCountdownMessage(mCountdown));
					try {Utilities.logd(TAG, ">>>>>>>>>>>>cont: HANDLER_MSG_TEST_REPEATED"); } catch (Exception e) { }
					mHandler.sendEmptyMessage(HANDLER_MSG_TEST_REPEATED);
				}
				
				break;

			case HANDLER_MSG_COUNTDOWN: //countdown loop
				try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>>HANDLER_MSG_COUNTDOWN<<<<<<<<<<<<<<<<<<"); } catch (Exception e) { }
				if (!isFinishing()) {
					mTextView.setVisibility(View.VISIBLE);
					mButton.setVisibility(View.INVISIBLE);
					mViewState = TEXT_PLUS_COUNT_DOWN;
					mTextView.setText(String.format(firstActions[mTestStage], msg.getData().getInt("countdown")));
					if (mCountdown == 0) {
						// timeout, this test failed, start the second test
						try {Utilities.logd(TAG, ">>>>>>>>>>>>cont: HANDLER_MSG_DISPLAY_FOR_SECOND_TEST"); } catch (Exception e) { } 
						mHandler.removeMessages(HANDLER_MSG_COUNTDOWN);
						mHandler.removeMessages(HANDLER_MSG_TEST_REPEATED);
						sendEmptyMessage(HANDLER_MSG_DISPLAY_FOR_TEST_ONCE);
						} 
					else {
						try {Utilities.logd(TAG, ">>>>>>>>>>>>cont: HANDLER_MSG_COUNTDOWN"); } catch (Exception e) { } 
						mHandler.sendMessageDelayed(buildCountdownMessage(--mCountdown), 1000);
					}
                }
				break;

			case HANDLER_MSG_TEST_REPEATED: // repeated tests
				try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>>HANDLER_MSG_TEST_REPEATED<<<<<<<<<<<<<<<<<<"); } catch (Exception e) { }
				mResultPass[mTestStage] = doTheTest();
				if (mResultPass[mTestStage]) {
					// this stage ended
					try {Utilities.logd(TAG, ">>>>>>>>>>>>cont: HANDLER_MSG_END_STAGE"); } catch (Exception e) { }
					mHandler.removeMessages(HANDLER_MSG_COUNTDOWN);
					mHandler.removeMessages(HANDLER_MSG_TEST_REPEATED);
				    mHandler.sendEmptyMessage(HANDLER_MSG_END_STAGE);
                } else {
					//test again in 500 milies     
					try {Utilities.logd(TAG, ">>>>>>>>>>>>cont: HANDLER_MSG_TEST_REPEATED"); } catch (Exception e) { }             
                    mHandler.sendEmptyMessageDelayed(HANDLER_MSG_TEST_REPEATED, 500);
                }
				break;

			case HANDLER_MSG_TEST_ONCE: // one time test
				try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>>HANDLER_MSG_TEST_ONCE<<<<<<<<<<<<<<<<<<"); } catch (Exception e) { }
				mResultPass[mTestStage] = doTheTest();
				if (mResultPass[mTestStage]) {
					// this stage ended
					try {Utilities.logd(TAG, ">>>>>>>>>>> cont: HANDLER_MSG_END_STAGE"); } catch (Exception e) { }
					//remove any pending messages
					mHandler.removeMessages(HANDLER_MSG_COUNTDOWN);
					mHandler.removeMessages(HANDLER_MSG_TEST_REPEATED);
				    mHandler.sendEmptyMessage(HANDLER_MSG_END_STAGE);
                } else {
					try {Utilities.logd(TAG, ">>>>>>>>>>>>> cont: HANDLER_MSG_DISPLAY_FOR_TEST_ONCE"); } catch (Exception e) { }
					mHandler.sendEmptyMessage(HANDLER_MSG_DISPLAY_FOR_TEST_ONCE);
                }
				break;

			case HANDLER_MSG_DISPLAY_FOR_TEST_ONCE: //display second msg and wait on button 
				try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>>HANDLER_MSG_DISPLAY_FOR_SECOND_TEST<<<<<<<<<<<<<<<<<<"); } catch (Exception e) { }
				if (!isFinishing()) {
					mTextView.setVisibility(View.VISIBLE);
					mButton.setVisibility(View.VISIBLE);
					mViewState = TEXT_PLUS_BUTTON;
					String text = secondActions[mTestStage] + " " + mWhenDone;
					mTextView.setText(text);
					mButton.setOnClickListener(new Button.OnClickListener() {
						public void onClick(View arg0) {
							//mResultPass[mTestStage] = doTheTest();
							//whatever the result - end the stage
							try {Utilities.logd(TAG, ">>>>>>>>>>>> cont: HANDLER_MSG_END_STAGE"); } catch (Exception e) { }
							mHandler.removeMessages(HANDLER_MSG_COUNTDOWN);
							mHandler.removeMessages(HANDLER_MSG_TEST_REPEATED);
							mHandler.sendEmptyMessage(HANDLER_MSG_END_STAGE);
						}
					});
				}
				break;

			case HANDLER_MSG_END_STAGE: //end stage
				try {Utilities.logd(TAG, ">>>>>>>>>>>>>>>>>>>HANDLER_MSG_COUNTDOWN<<<<<<<<<<<<<<<<<<"); } catch (Exception e) { }
				
				mResultPass[mTestStage] = doTheTest();
				mTextView.setVisibility(View.INVISIBLE);
				mButton.setVisibility(View.INVISIBLE);
				// new
				if (mResultPass[mTestStage] == true) {
					if (mTestStage == STAGE_III) {
						//last stage passed - end the test with pass
						try {Utilities.logd(TAG, ">>>>>>>>>>>> end test with PASS"); } catch (Exception e) { }
						pass();
					} else {
						// continue to the next stage
						mTestStage++;
						mCountdown = countdownMaxes[mTestStage];
						try {Utilities.logd(TAG, ">>>>>>>>>>>> cont: HANDLER_MSG_START_STAGE"); } catch (Exception e) { }
						mHandler.sendEmptyMessage(HANDLER_MSG_START_STAGE);
					}
				} else {
					// stage failed => test failed
					try {Utilities.logd(TAG, ">>>>>>>>>>>> end test with FAIL"); } catch (Exception e) { }
					fail();
				}
				break;
			}
		}
			/*end :modified by tianfangzhou for battery test ,2013.10.14*/
	};
	
	/*
     * get the current docking state
     * from the last ACTION_DOCK_EVENT steaky intent
     */
    public boolean[] getCradleAndIgnStates() {
		boolean inCradle;
		boolean ignOn;
        int currentDockState = -1;
        /*
         * Receiving the current docking state
         */
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
        Intent intent = registerReceiver(null, ifilter);
        if (intent != null) {
            currentDockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
            Log.d(TAG, "in getCurrentDockState: " + intent.toString() + " DOCK STATE: " + currentDockState);
        }

		switch (currentDockState) {
		case Intent.EXTRA_DOCK_STATE_UNDOCKED:
			try {Utilities.logd(TAG, ">>>>>>>>>>>> Intent.EXTRA_DOCK_STATE_UNDOCKED"); } catch (Exception e) { }
                inCradle = false;
				ignOn = false;
                break;
            case Intent.EXTRA_DOCK_STATE_DESK:
            case Intent.EXTRA_DOCK_STATE_LE_DESK:
		case Intent.EXTRA_DOCK_STATE_HE_DESK:
			try {Utilities.logd(TAG, ">>>>>>>>>>>> EXTRA_DOCK_STATE_DESK"); } catch (Exception e) { }
                inCradle = true;
				ignOn = false;
				break;
		case Intent.EXTRA_DOCK_STATE_CAR:
			try {Utilities.logd(TAG, ">>>>>>>>>>>> EXTRA_DOCK_STATE_CAR"); } catch (Exception e) { }
				inCradle = true;
				ignOn = true;
				break;
		default:
			try {Utilities.logd(TAG, ">>>>>>>>>>>> default"); } catch (Exception e) { }
                inCradle = false;
				ignOn = false;
                break;
        }
		try {Utilities.logd(TAG, ">>>>>>>>>>>> inCradle: " + inCradle + " ignOn: " + ignOn); } catch (Exception e) { }

        return new boolean[] {inCradle, ignOn};
    }

	public boolean getBatteryState() {

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
						status == BatteryManager.BATTERY_STATUS_FULL;
		return isCharging;
	}

	public class CradleStageStates {
		boolean[] states;
		CradleStageStates(boolean a, boolean b, boolean c) {
			states = new boolean[MAX_STATES];
			this.states[CRADLE_STATE] = a;
			this.states[IGNITION_STATE] = b;
			this.states[BATTERY_STATE] = c;
		}
		public boolean getState(int index) {
			return states[index]; 
		}
	}
}
