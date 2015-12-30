package com.qrt.factory.activity;
import com.qrt.factory.R;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Bundle;

/**
 * ********************************************************
 * Name: Nfc.java
 * Author: Wangwenlong
 * Date: 2013-08-05
 * Purpose: run NFC test
 * Declaration: QRT Telecom Technology Co., LTD
 * **********************************************************
 */

public class Nfc extends AbstractActivity {

    private static final String TAG = "NfcTest";
    private NfcAdapter mNfcAdapter;
    private IntentFilter[] intentFiltersArray;
    private PendingIntent pendingIntent;
    private TextView mTestResultView;
    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNfcAdapter = null;
        NfcManager nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
		setContentView(R.layout.nfc);   //add by bwq to add teset notice 20140718
        if (nfcManager != null) {
            mNfcAdapter = nfcManager.getDefaultAdapter();
        } else {
            mNfcAdapter = NfcAdapter.getNfcAdapter(Nfc.this);
        }

        if (mNfcAdapter == null) {
            loge("NfcAdapter is null");
            fail();
        } else {
            if (!mNfcAdapter.isEnabled()) {
                mNfcAdapter.enable();
            }

            pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            try {
                IntentFilter ndefIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED, "*/*");
                IntentFilter techIntentFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
                IntentFilter tagIntentFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
                intentFiltersArray = new IntentFilter[] {ndefIntentFilter, techIntentFilter, tagIntentFilter};
            }
            catch (IntentFilter.MalformedMimeTypeException e) {
                loge(e);
            }
		/*add by bwq to add teset notice 20140718 begin*/	
		mTestResultView = (TextView) findViewById(R.id.nfc_test_result);

        Button cancel = (Button) findViewById(R.id.nfc_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mResultBuffer.append(getString(R.string.user_canceled));
                fail();
            }
        });
        }
		/*add by bwq to add teset notice 20140718 end*/	
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent,
                    intentFiltersArray, null);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            logd("NDEF_DISCOVERED");
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            logd("TECH_DISCOVERED");
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            logd("TAG_DISCOVERED");
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            logd(tagFromIntent);
        }
		/*add by bwq to add teset notice 20140718 begin*/	
		mTestResultView.setText(R.string.nv_pass);
        mTestResultView.setTextColor(android.graphics.Color.GREEN);
		/*add by bwq to add teset notice 20140718 end*/	
        pass();
    }
}
