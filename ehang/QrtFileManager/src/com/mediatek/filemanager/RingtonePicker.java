package com.mediatek.filemanager;

import com.mediatek.filemanager.utils.FileUtils;
import com.mediatek.filemanager.utils.RingtoneUtils;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * @author joez
 *
 */
public class RingtonePicker extends Activity {

    private static final int REQUEST_CODE = 0;
    
    private int mType = RingtoneManager.TYPE_RINGTONE;
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, -1);
        if (type != -1) mType = type;
        
        Intent intent = new Intent(this, FileManagerSelectFileActivity.class);
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("audio/*");
        
        startActivityForResult(intent, REQUEST_CODE);
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_CODE) {  
            if (resultCode == RESULT_OK) {
                Intent result = new Intent();
                
                Uri uri = data.getData();
                if (uri != null && uri.getScheme().equals("file")) {
                    // translate to the media store's uri
                    uri = RingtoneUtils.setAsRingtone(this, FileUtils.getFileFromUri(uri), mType);
                }
                if (uri != null) {
                    result.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, uri);
                    
                    setResult(resultCode, result);
                }
            } else {
                setResult(resultCode);
            }
            
            finish();
        }
    }
}
