package com.yihang.thememgr;

import java.util.ArrayList;

import com.yihang.thememgr.ThemeUtils.ThemeType;
import com.yihang.thememgr.helper.ThemeInfo;
import com.yihang.thememgr.helper.ThemeInfoLoader;
import com.yihang.thememgr.helper.ThemeLoaderHelper;
import com.yihang.thememgr.helper.ThemeResourceLoader;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public final class ThemeManager {

	static final String TAG = "ThemeManager";
	static final boolean DEBUG = true;
	
	private static ThemeManager sInstance;
	private final Context mContext;

	private String mThemeName;
	private String mThemeLauncherName;
	private String mThemeWallpaperName;
	private String mThemeLockscreenName;
	private String mThemeLockWallpaperName;

	
	public ThemeManager(Context context) {
		mContext = context;
		
		if(DEBUG) Log.e(TAG, "ThemeManager on create");
		ContentResolver cr = context.getContentResolver();
		final Cursor c = cr.query(ThemeUtils.CONTENT_URI, null, null, null, null);
		
    	if (c != null) {
            final int typeIndex = c.getColumnIndexOrThrow(ThemeUtils.THEME_CATEGORY);
            final int pkgIndex = c.getColumnIndexOrThrow(ThemeUtils.PACKAGE_NAME);
            
    		while (c.moveToNext()) {
    			String type = c.getString(typeIndex);
    			String packageName = c.getString(pkgIndex);
				if(type.equals(ThemeUtils.THEME)){
					mThemeName = packageName;
				}else if(type.equals(ThemeUtils.LAUNCHERICON)){
					mThemeLauncherName = packageName;
				}else if(type.equals(ThemeUtils.LOCKSCREEN)){
					mThemeLockscreenName = packageName;
				}else if(type.equals(ThemeUtils.WALLPAPER)){
					mThemeWallpaperName = packageName;
				}else if(type.equals(ThemeUtils.LOCKWALLPAPER)){
					mThemeLockWallpaperName = packageName;
				}
    		}
    		c.close();
    	}else{
    		Log.w(TAG, "theme qury error! no data");
    	}
		
    	checkCurrentTheme(ThemeUtils.CATEGORY_THEME, mThemeName);
    	checkCurrentTheme(ThemeUtils.CATEGORY_LAUNCHERICON, mThemeLauncherName);
    	checkCurrentTheme(ThemeUtils.CATEGORY_LOCKSCREEN, mThemeLockscreenName);
    	checkCurrentTheme(ThemeUtils.CATEGORY_WALLPAPER, mThemeWallpaperName);
    	checkCurrentTheme(ThemeUtils.CATEGORY_LOCKWALLPAPER, mThemeLockWallpaperName);
    	if(DEBUG ) Log.i(TAG, "create ThemeManager, theme="+mThemeName+", icon="+mThemeLauncherName+", wp="+
    			mThemeWallpaperName+", lock="+mThemeLockscreenName+", lock wp="+mThemeLockWallpaperName);
	}
	
	public static ThemeManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ThemeManager(context);
		}
		return sInstance;
	}

	public ThemeInfo getThemeInfoByName(String themePkgName, ThemeType themeType) {
		// TODO Auto-generated method stub
		return ThemeLoaderHelper.getThemeInfoLoader(themeType).loadThemeInfo(mContext, 
				themePkgName);
	}
	
	private String checkCurrentTheme(int category, String themePkgName){
		String themeName = themePkgName;
		PackageManager packageManager = mContext.getPackageManager();
		if(!themeName.equals(ThemeUtils.DEFAULT_THEME_PACKAGENAME) && 
				!ThemeUtils.findPackage(themeName, packageManager)){
			Log.w(TAG, "checkCurrentTheme, theme package not foud! packagename="+themePkgName+", category"+category);
			themeName = new String(ThemeUtils.DEFAULT_THEME_PACKAGENAME);
			
			//apply to default theme
			ThemeInfo info = getThemeInfoByName(themeName, ThemeType.THEME_DEFAULT);
			applyTheme(category, info);
		}
		
		return themeName;
	}
	
	public String getCurrentTheme(int category){
		String themePkgName = null;
		switch (category) {
		case ThemeUtils.CATEGORY_THEME:
			themePkgName = mThemeName;
			break;
		case ThemeUtils.CATEGORY_LAUNCHERICON:
			themePkgName = mThemeLauncherName;
			break;
		case ThemeUtils.CATEGORY_WALLPAPER:
			themePkgName = mThemeWallpaperName;
			break;
		case ThemeUtils.CATEGORY_LOCKSCREEN:
			themePkgName = mThemeLockscreenName;
			break;
		case ThemeUtils.CATEGORY_LOCKWALLPAPER:
			themePkgName = mThemeLockWallpaperName;
			break;
		default:
			break;
		}
		if (themePkgName != null){
		    themePkgName = checkCurrentTheme(category, themePkgName);
		}
		return themePkgName;
	}
	
	public ArrayList<ThemeInfo> getAllTheme(int category) {
        String currentTheme = getCurrentTheme(category);
        String intentCategory = ThemeUtils.getThemeIntentCategary(category);
        ArrayList<ThemeInfo> arrayAllTheme = new ArrayList<ThemeInfo>();
        ThemeInfoLoader defaultLoader = ThemeLoaderHelper.getThemeInfoLoader(ThemeType.THEME_DEFAULT);
        ThemeInfoLoader customLoader = ThemeLoaderHelper.getThemeInfoLoader(ThemeType.THEME_CUSTOM);
        ArrayList<ThemeInfo> arrayDef = defaultLoader.loadInstalledThemes(mContext, intentCategory);
        ArrayList<ThemeInfo> arrayCus = customLoader.loadInstalledThemes(mContext, intentCategory);

        if(DEBUG) Log.d(TAG, "getAllTheme category="+category+", currentTheme="+currentTheme);
        //add default theme
        if(arrayDef != null){
	        for (ThemeInfo item:arrayDef) {
	        	ThemeInfo installInfo = new ThemeInfo(item); 
	            arrayAllTheme.add(installInfo);            
	        }
        }
        //add custom theme
        if(arrayCus != null){
	        for (ThemeInfo item:arrayCus) {
	        	ThemeInfo installInfo = new ThemeInfo(item); 
	            arrayAllTheme.add(installInfo);            
	        }
        }
        
        //mark up the current theme
        for (ThemeInfo item:arrayAllTheme) {
        	if(item.mPkgName != null && item.mPkgName.equals(currentTheme)){
        		item.mIsCurrent = true;
        	}
        }
        
        return arrayAllTheme;        
    }
	/* Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01 begin */
	public ThemeInfo getNextThemeInfo(int category) {
        String currentTheme = getCurrentTheme(category);
		return getNextThemeInfo(category, currentTheme);
	}
	
	public ThemeInfo getNextThemeInfo(int category, String themePkgName) {
		ArrayList<ThemeInfo> themes = getAllTheme(category);
		int nextIndex = 0;
		int size = themes.size();
		if(size <= 0){
		    return null;
		}
	    for (int i=0; i<size; i++) {
        	ThemeInfo item = themes.get(i);
        	if(item.mPkgName != null && item.mPkgName.equals(themePkgName)){
        		nextIndex = i+1;
        		break;
        	}
        }
	        
	    if(nextIndex < 0 || nextIndex > size-1){
	    	nextIndex = 0;
	    }
	    
	    return themes.get(nextIndex);
	}
    /* Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01 end */
	public boolean applyTheme(int category, ThemeInfo info) {
		
		if(DEBUG) Log.d(TAG, "applyTheme  category="+category + ", pkg="+info.mPkgName);
		ContentResolver cr = mContext.getContentResolver();
		final ContentValues values = new ContentValues();
		final Uri uri = ThemeUtils.getThemeUri(category);
		 Log.e(TAG, "applyTheme,uri:"+uri);//Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01
		final String[] sel = ThemeUtils.getThemeSelArgs(category);
		 Log.e(TAG, "applyTheme,sel:"+sel);//Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01
		int count = 0;
	
		values.put(ThemeUtils.THEME_NAME, info.mThemeName);//Modified By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01
		values.put(ThemeUtils.PACKAGE_NAME, info.mPkgName);
		values.put(ThemeUtils.AUTHOR, info.mThemeAuthor);
		values.put(ThemeUtils.SUPPORTVER, info.mThemeSupportVer);
		values.put(ThemeUtils.THEME_TYPE, String.valueOf(ThemeUtils.getThemeType(info.mThemeType)));
		 Log.e(TAG, "applyTheme,values:"+values);//Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01
		count = cr.update(uri, values, null, sel);
		 Log.e(TAG, "applyTheme,count:"+count);//Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01
	
		if(count > 0){
			updateCurrentTheme(category, info);
			return true;
		}
		
		return false;
	}
	
	private void updateCurrentTheme(int category, ThemeInfo info){
		switch (category) {
		case ThemeUtils.CATEGORY_THEME:
			mThemeName = new String(info.mPkgName);
			mThemeLauncherName = new String(info.mPkgName);
			mThemeLockscreenName = new String(info.mPkgName);
			mThemeWallpaperName = new String(info.mPkgName);
			mThemeLockWallpaperName = new String(info.mPkgName);
			break;
        /* Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01 begin */
        case ThemeUtils.CATEGORY_LAUNCHER:
            mThemeName = new String(info.mPkgName);
            mThemeLauncherName = new String(info.mPkgName);
            mThemeLockscreenName = new String(info.mPkgName);
            mThemeWallpaperName = new String(info.mPkgName);
            mThemeLockWallpaperName = new String(info.mPkgName);
            break;
        /* Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01 end */
		case ThemeUtils.CATEGORY_LAUNCHERICON:
			mThemeLauncherName = new String(info.mPkgName);
			break;
		case ThemeUtils.CATEGORY_LOCKSCREEN:
			mThemeLockscreenName = new String(info.mPkgName);
			mThemeLockWallpaperName = new String(info.mPkgName);
			break;
		case ThemeUtils.CATEGORY_WALLPAPER:
			mThemeWallpaperName = new String(info.mPkgName);
			break;
		case ThemeUtils.CATEGORY_LOCKWALLPAPER:
			mThemeLockWallpaperName = new String(info.mPkgName);
			break;
		default:
			break;
		}
		
		if(DEBUG)  Log.i(TAG, "updateCurrentTheme, theme="+mThemeName+", icon="+mThemeLauncherName+", wp="+
    			mThemeWallpaperName+", lock="+mThemeLockscreenName+", lock wp="+mThemeLockWallpaperName);
	}
	
	public Bitmap loadBitmap(ThemeInfo info, String resName) {
		// TODO Auto-generated method stub
		final ThemeResourceLoader loader = ThemeLoaderHelper.getThemeResLoader(mContext, info);
		Bitmap bmp = loader.loadBitmap(resName);

		return bmp;
	}
	
	public Drawable loadDrawable(ThemeInfo info, String resName) {
		// TODO Auto-generated method stub
		final ThemeResourceLoader loader = ThemeLoaderHelper.getThemeResLoader(mContext, info);
		Drawable bmp = loader.loadDrawable(resName);

		return bmp;
	}
	
	public String[] loadStringArray(ThemeInfo info, String resName) {
		// TODO Auto-generated method stub
		final ThemeResourceLoader loader = ThemeLoaderHelper.getThemeResLoader(mContext, info);
		String[] array = loader.loadStringArray(resName);

		return array;
	}
}