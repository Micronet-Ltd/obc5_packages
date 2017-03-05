
package com.mediatek.filemanager.utils;

import android.webkit.MimeTypeMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author joez
 *
 */
public class MimeTypes {
	static public String TYPE_UNKNOWN = "unknown/unknown";
	
    private Map<String, String> mUserMap;
    
    private MimeTypeMap mSystemMap;

    public MimeTypes() {
        init();
    }
    
    public void loadEntry(String type, String extension) {
        mUserMap.put(type, extension.toLowerCase());
    }

    public String getMimeType(String filename) {
        String mimetype = null;
        
        String extension = FileUtils.getExtension(filename).toLowerCase();
        
    	// Be sure to remove the first character from the extension, which is the "."
        if (extension.length() > 0) {
        	extension = extension.substring(1);
        }

        if (extension.length() > 0) {
            if (mimetype == null) {
                mimetype = mUserMap.get(extension);
            }
            
	        if (mimetype == null) {
	            mimetype = mSystemMap.getMimeTypeFromExtension(extension);
	        }
        }
        
        if (mimetype == null) {
            // FIXME: how to handle unknow type?
            // mimetype = "application/octet-stream";
            mimetype = TYPE_UNKNOWN;
        }

        return mimetype;
    }
    
    private void init() {
        mUserMap = new HashMap<String, String>();
        mSystemMap = MimeTypeMap.getSingleton();
    }
}
