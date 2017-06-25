/*
 * Copyright 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.telecom;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.android.internal.util.Preconditions;

//{{begin,mod by chenqi 2016-02-22 17:05
//reason:incoming ui too late to tone
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
//}}end,mod by chenqi

/**
 * Plays the default ringtone. Uses {@link Ringtone} in a separate thread so that this class can be
 * used from the main thread.
 */
class AsyncRingtonePlayer {
    // Message codes used with the ringtone thread.
    private static final int EVENT_PLAY = 1;
    private static final int EVENT_STOP = 2;
    private static final int EVENT_REPEAT = 3;
	
//{{begin,mod by chenqi 2016-02-22 17:07
//reason:incoming ui too late to tone
    private static final int EVENT_START_PLAY = 4;
    private static final int EVENT_STOP_PLAY = 5;
	private static final long PLAY_TIMEOUT_MILLIS = 5000;
    private Handler mPlayHandler;
	volatile boolean play_sended=false;//for pairing the postmessage of "play" and "stop"
										//if "false",the postmessage for "play" is invalid.Because,the "play" process is stoped by the "stop".--
										//--This postmessage for "play" just be from the "other threed locked(broadcast,play delay,and so on)"
//}}end,mod by chenqi

    // The interval in which to restart the ringer.
    private static final int RESTART_RINGER_MILLIS = 3000;

    /** Handler running on the ringtone thread. */
    private Handler mHandler;

    /** The current ringtone. Only used by the ringtone thread. */
    private Ringtone mRingtone;

    private int mPhoneId = 0;

    void setPhoneId(int phoneId) {
        mPhoneId = phoneId;
    }

    /**
     * The context.
     */
    private final Context mContext;

    AsyncRingtonePlayer(Context context) {
        mContext = context;
		
    }
	
//{{begin,mod by chenqi 2016-02-22 17:08
//reason:incoming ui too late to tone
	//-------------sub func,for the broadcast from incoming call---------
	//send a delay message to mPlayHandler(for broadcast safe)
    private void sendMessageDelay(int messageCode, boolean shouldCreateHandler, Uri ringtone){

        if(mPlayHandler==null){
            mPlayHandler = getNewPlayHandler();
        }

        Message message=mPlayHandler.obtainMessage(EVENT_START_PLAY, ringtone);
        mPlayHandler.sendMessageDelayed(message, PLAY_TIMEOUT_MILLIS);
    }
	
	//clear the handler for safe(start play,if receive a broadcast msg,clear)
	private void clear_handler_Start_play(){
	
		synchronized(AsyncRingtonePlayer.this) {
	        Log.i(this, "clear_handler_Start_play.");

	        // At the time that STOP is handled, there should be no need for repeat messages in the
	        // queue.
	        if(mPlayHandler!=null){
		        mPlayHandler.removeMessages(EVENT_START_PLAY);
		        mPlayHandler.getLooper().quit();
		        mPlayHandler = null;
	    	}
        }

	}
	//regist the broadcast for incoming ui
	private void registerBroadcastReceiver_incoming_ui(){
	
		Log.i(this, "registerBroadcastReceiver_incoming_ui.");
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_EHANG_INCOMING_UI);//msg_shown:true,false
		intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);//increase the Priority,for a short time,mod by chenqi,2016-02-29
		mContext.registerReceiver(mbroadcastIncomingUI,intentFilter);

	}
	//unregist the broadcast for incoming ui
	private void unregisterBroadcastReceiver_incoming_ui(){
	
		Log.i(this, "unregisterBroadcastReceiver_incoming_ui.");
		try{
			mContext.unregisterReceiver(mbroadcastIncomingUI);
		} catch (IllegalArgumentException e) {  
			//if unregisted,and do that again;will thow a err
			Log.i(this,"Erro:unregisterBroadcastReceiver_incoming_ui:"+e);
		}  

	}

	//broadcast:ACTION_EHANG_INCOMING_UI,send from InCallPresenter(when incoming call)
    public static final String ACTION_EHANG_INCOMING_UI = "android.intent.action.EHANG_INCOMING_UI";
    private final BroadcastReceiver mbroadcastIncomingUI = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {

            Log.i(this, "mbroadcastIncomingUI onReceive.");
            String action = intent.getAction();
            if(ACTION_EHANG_INCOMING_UI.equals(action)){
                Log.i(this, "mbroadcastIncomingUI onReceive action ok");
                postMessage(EVENT_PLAY, true , uriRingtone);//real play
				
				//clear
                clear_handler_Start_play();					//clear.when called "play()",we will restart the handler
				unregisterBroadcastReceiver_incoming_ui();	//we had not need this broadcast,because,the safetiem
															//is passed
				uriRingtone=null;
            }
        }
    };
    private Handler getNewPlayHandler() {
        Preconditions.checkState(mPlayHandler == null);

        HandlerThread thread = new HandlerThread("play-start");
        thread.start();

        return new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
	            Log.i(this, "getNewPlayHandler:"+msg.what);
                switch(msg.what) {
                    case EVENT_START_PLAY:
						//safe time passed
                        postMessage(EVENT_PLAY, true , (Uri) msg.obj);
						//clear
                        clear_handler_Start_play();					//clear.when called "play",we will restart the handler
						unregisterBroadcastReceiver_incoming_ui();	//we had not need this broadcast,because,the safetiem
																	//is passed
						uriRingtone=null;
                        break;
                    case EVENT_STOP_PLAY:
                        break;
                }
            }
        };
    }

    /** Plays the ringtone. */
	private Uri uriRingtone=null;
//}}end,mod by chenqi
    void play(Uri ringtone) {
        //{{begin,mod by chenqi 2016-02-22 17:09
        //reason:incoming ui too late to tone
        synchronized(AsyncRingtonePlayer.this) {
            Log.d(this, "Posting play.");
			play_sended=true;
            //postMessage(EVENT_PLAY, true /* shouldCreateHandler */, ringtone);//removed,incoming ui too late to tone,2016-02-22-17:09

            uriRingtone=ringtone;
            registerBroadcastReceiver_incoming_ui();//regist a broadcast for whether the incoming call activity is showing
            sendMessageDelay(EVENT_START_PLAY,true,ringtone);//for safe,if we can't get the broad in the delay time,
            //we will play the tone whith this way
            //and unregist the broadcast
        }
        //}}end,mod by chenqi
    }


	
    /** Stops playing the ringtone. */
    void stop() {
        Log.d(this, "Posting stop.");
        postMessage(EVENT_STOP, false /* shouldCreateHandler */, null);
    }

    /**
     * Posts a message to the ringtone-thread handler. Creates the handler if specified by the
     * parameter shouldCreateHandler.
     *
     * @param messageCode The message to post.
     * @param shouldCreateHandler True when a handler should be created to handle this message.
     */
    private void postMessage(int messageCode, boolean shouldCreateHandler, Uri ringtone) {
        synchronized(AsyncRingtonePlayer.this) {
			
			//{{begin,mod by chenqi 2016-04-14 11:09
			//reason:incoming ui too late to tone.Do this step for sopted the delay ring
			if(EVENT_STOP == messageCode
				&&mPlayHandler!=null)
			{
				clear_handler_Start_play();
				unregisterBroadcastReceiver_incoming_ui();
				uriRingtone=null;
				play_sended=false;
			}
			else if(EVENT_PLAY== messageCode){
				if(play_sended==true){
					play_sended=false;
				}
				else{
					return;
				}
				
			}
			//}}end,mod by chenqi
			
            if (mHandler == null && shouldCreateHandler) {
                mHandler = getNewHandler();
            }

            if (mHandler == null) {

                Log.d(this, "Message %d skipped because there is no handler.", messageCode);
            } else {
                mHandler.obtainMessage(messageCode, ringtone).sendToTarget();
            }
        }
    }

    /**
     * Creates a new ringtone Handler running in its own thread.
     */
    private Handler getNewHandler() {
        Preconditions.checkState(mHandler == null);

        HandlerThread thread = new HandlerThread("ringtone-player");
        thread.start();

        return new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case EVENT_PLAY:
                        handlePlay((Uri) msg.obj);
                        break;
                    case EVENT_REPEAT:
                        handleRepeat();
                        break;
                    case EVENT_STOP:
                        handleStop();
                        break;
                }
            }
        };
    }

    /**
     * Starts the actual playback of the ringtone. Executes on ringtone-thread.
     */
    private void handlePlay(Uri ringtoneUri) {
        // don't bother with any of this if there is an EVENT_STOP waiting.
        if (mHandler.hasMessages(EVENT_STOP)) {
            return;
        }

        ThreadUtil.checkNotOnMainThread();
        Log.i(this, "Play ringtone.");

        if (mRingtone == null) {
            mRingtone = getRingtone(ringtoneUri);

            // Cancel everything if there is no ringtone.
            if (mRingtone == null) {
                handleStop();
                return;
            }
        }

        handleRepeat();
    }

    private void handleRepeat() {
        if (mRingtone == null) {
            return;
        }

        if (mRingtone.isPlaying()) {
            Log.d(this, "Ringtone already playing.");
        } else {
            mRingtone.play();
            Log.i(this, "Repeat ringtone.");
        }

        // Repost event to restart ringer in {@link RESTART_RINGER_MILLIS}.
        synchronized(AsyncRingtonePlayer.this) {
            if (!mHandler.hasMessages(EVENT_REPEAT)) {
                mHandler.sendEmptyMessageDelayed(EVENT_REPEAT, RESTART_RINGER_MILLIS);
            }
        }
    }

    /**
     * Stops the playback of the ringtone. Executes on the ringtone-thread.
     */
    private void handleStop() {
    
	synchronized(AsyncRingtonePlayer.this) {//moved here,incoming ui too late to tone,2016-02-22-17:09
        ThreadUtil.checkNotOnMainThread();
        Log.i(this, "Stop ringtone.");

        if (mRingtone != null) {
            Log.d(this, "Ringtone.stop() invoked.");
            mRingtone.stop();
            mRingtone = null;
        }
		
		//{{begin,mod by chenqi 2016-02-22 17:16
		//reason:incoming ui too late to tone
		//broadcast,safe handler clear
		clear_handler_Start_play();
		unregisterBroadcastReceiver_incoming_ui();
		uriRingtone=null;
		//}}end,mod by chenqi
		
        //synchronized(this) {//removed,incoming ui too late to tone,2016-02-22-17:09
        // At the time that STOP is handled, there should be no need for repeat messages in the
        // queue.
        mHandler.removeMessages(EVENT_REPEAT);
/*
            if (mHandler.hasMessages(EVENT_PLAY)) {
                Log.v(this, "Keeping alive ringtone thread for subsequent play request.");
            } else {*/
        mHandler.removeMessages(EVENT_STOP);
        mHandler.getLooper().quitSafely();
        mHandler = null;
        Log.v(this, "Handler cleared.");
            //}//removed,incoming ui too late to tone,2016-02-22-17:09
        }
    }

    private Ringtone getRingtone(Uri ringtoneUri) {
        if (ringtoneUri == null) {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                ringtoneUri = RingtoneManager.getActualRingtoneUriBySubId(mContext, mPhoneId);
                if (ringtoneUri == null) {
                    return null;
                }
            } else {
                ringtoneUri = Settings.System.DEFAULT_RINGTONE_URI;
            }
        }

        Ringtone ringtone = RingtoneManager.getRingtone(mContext, ringtoneUri);
        if (ringtone != null) {
            ringtone.setStreamType(AudioManager.STREAM_RING);
        }
        return ringtone;
    }
}
