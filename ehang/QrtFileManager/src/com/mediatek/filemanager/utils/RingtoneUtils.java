package com.mediatek.filemanager.utils;

import com.mediatek.filemanager.R;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Audio.Media;
import android.widget.Toast;

import java.io.File;

/**
 * @author joez
 *
 */
public class RingtoneUtils {
	
	public static final int SIM2 = 100;
    public static Uri setAsRingtone(Context context, File file, int type) {
        // verify first
        if (FileUtils.isHidden(file.getAbsolutePath())) {
            Toast.makeText(context, R.string.error_hidden_file, Toast.LENGTH_SHORT).show();
            return null;
        }
        
        // request media scan to update the information of the target file
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, FileUtils.getUriFromFile(file)));
        
        ContentResolver cr = context.getContentResolver();
        String path = file.getAbsolutePath();
        String name = FileUtils.getBaseName(file.getName());
        Uri contentUri = Media.getContentUriForPath(path);
        
        Cursor cursor = cr.query(contentUri, new String[] {
            MediaColumns._ID
        }, MediaColumns.DATA + "=?", new String[] {
            path
        }, null);
        
        Uri itemUri = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                itemUri = ContentUris.withAppendedId(contentUri, cursor.getLong(0));
            }
        }
        
        ContentValues values = new ContentValues();
        //QRT delete by wangwenlong for selected ringtone bug id 192172
        switch (type) {
        	default:
            case RingtoneManager.TYPE_RINGTONE:
                values.put(Media.IS_RINGTONE, true);
                break;
            case RingtoneManager.TYPE_NOTIFICATION:
                values.put(Media.IS_NOTIFICATION, true);
                break;
              //Qrt : Begin modify by huliang 20120420 add ringtone 2 data
            //case RingtoneManager.TYPE_RINGTONE_2:
			case SIM2:
            	 values.put(Media.IS_RINGTONE, true);
                break;
              //Qrt : End modify by huliang 20120420
            case RingtoneManager.TYPE_ALARM:
                values.put(Media.IS_ALARM, true);
                break;
        }
        
        //QRT modify by wangwenlong for selected ringtone bug id 192172
        if (itemUri != null) {
            cr.update(itemUri, values, null, null);
        } else {

            values.put(MediaColumns.DATA, path);
            values.put(MediaColumns.TITLE, name);
            
            itemUri = cr.insert(contentUri, values);
        }
        
        return itemUri;
    }
	
	
}
