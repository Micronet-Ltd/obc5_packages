package com.qrt.factory.activity;

import com.qrt.factory.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Speaker extends AbstractActivity {

    private static final String TAG = "Speaker Test";

    MediaPlayer mMediaPlayer = new MediaPlayer();

    boolean isPlaying = false;

    Button playButton = null;

    Button failButton = null;

    AudioManager mAudioManager;

    Context mContext;

    private int mDefaultMode;

    private Map<Integer, Integer> mDefaultAudioVolume
            = new HashMap<Integer, Integer>();

    private TextView mSpeakerRemoveHeadsetView = null;

    @Override
    protected String getTag() {
        return TAG;
    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.speaker);

        mContext = this;
        isPlaying = false;

        getService();
        bindView();

        getDefaultAudioVolume();

        mSpeakerRemoveHeadsetView = (TextView) findViewById(
                R.id.speaker_remove_headset);
        if (mAudioManager.isWiredHeadsetOn()) {
            mSpeakerRemoveHeadsetView.setVisibility(View.VISIBLE);
        } else {
            startPlay();
            mSpeakerRemoveHeadsetView.setVisibility(View.GONE);
        }
    }

    private void startPlay() {
        setAudio();
        try {
            play();
        } catch (Exception e) {
            loge(e);
        }
    }

    @Override
    protected void onResume() {
        registerReceiver(headsetRecevier,
                new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(headsetRecevier);
        super.onPause();
    }

    private final BroadcastReceiver headsetRecevier = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                int state = intent.getIntExtra("state", 0);

                if (state == 1) { // 插入耳机
                    mSpeakerRemoveHeadsetView.setVisibility(View.VISIBLE);
                } else { // 拔出耳机
                    startPlay();
                    mSpeakerRemoveHeadsetView.setVisibility(View.GONE);
                }
            }
        }
    };

    void play() throws IllegalArgumentException, IllegalStateException,
            IOException {

        final TextView mTextView = (TextView) findViewById(R.id.speaker_hint);
        mTextView.setText(getString(R.string.speaker_playing));
        isPlaying = true;

        try {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.reset();
                mMediaPlayer = MediaPlayer.create(mContext, R.raw.bordeaux_2);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();
                if (!isFinishing()) {
                    showPassOrFailDialog(Speaker.this,
                            getString(R.string.speaker_confirm),
                            getString(R.string.yes),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                        int which) {

                                    pass();
                                }
                            }, getString(R.string.no),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                        int which) {

                                    fail();
                                }
                            }
                    );
                }
            }
        } catch (Exception e) {
            loge(e);
        }
    }

    void stop() {
        if (isPlaying == true) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            isPlaying = false;
        }
    }

    @Override
    public void finish() {
        stop();
        setDefaultAudioVolume();
        super.finish();
    }

    private void setDefaultAudioVolume() {
        mAudioManager.setMode(mDefaultMode);
        for (Map.Entry<Integer, Integer> entry : mDefaultAudioVolume
                .entrySet()) {
            mAudioManager.setStreamVolume(entry.getKey(), entry.getValue(), 0);
        }
    }

    private void getDefaultAudioVolume() {

        mDefaultMode = mAudioManager.getMode();

        mDefaultAudioVolume.clear();
        mDefaultAudioVolume.put(AudioManager.STREAM_ALARM, mAudioManager
                .getStreamVolume(AudioManager.STREAM_ALARM));
        mDefaultAudioVolume.put(AudioManager.STREAM_MUSIC, mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));
        mDefaultAudioVolume.put(AudioManager.STREAM_VOICE_CALL, mAudioManager
                .getStreamVolume(AudioManager.STREAM_VOICE_CALL));
        mDefaultAudioVolume.put(AudioManager.STREAM_DTMF, mAudioManager
                .getStreamVolume(AudioManager.STREAM_DTMF));
        mDefaultAudioVolume.put(AudioManager.STREAM_NOTIFICATION, mAudioManager
                .getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        mDefaultAudioVolume.put(AudioManager.STREAM_RING, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_RING));
        mDefaultAudioVolume.put(AudioManager.STREAM_SYSTEM, mAudioManager
                .getStreamVolume(AudioManager.STREAM_SYSTEM));
    }


    public void setAudio() {

        mAudioManager.setMode(AudioManager.MODE_RINGTONE);

        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_DTMF), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
                0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_SYSTEM), 0);
    }

    void bindView() {

//        playButton = (Button) findViewById(R.id.speaker_play);
        failButton = (Button) findViewById(R.id.speaker_fail);
        final TextView mTextView = (TextView) findViewById(R.id.speaker_hint);
        mTextView.setText(getString(R.string.speaker_to_play));

        failButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                fail();
            }
        });
    }

    void getService() {

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            fail();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            pass();
            return true;
        }
        return super.onKeyDown(keyCode,
                event);
    }
}
