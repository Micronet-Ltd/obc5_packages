package com.yihang.thememgr;

import android.content.pm.PackageManager;
import android.net.Uri;

public class ThemeUtils {

	public final static String THEME_PACKAGE_TOKEN = "com.yihang.launcherEx.theme.";
	public final static String DEFAULT_THEME_PACKAGENAME = "theme_defalt";
	
	public final static String ACTION_PICK_THEME = "com.yihang.launcherEx.PICK_THEME";

	public final static String ACTION_APPLY_THEME = "com.yihang.launcherEx.applytheme";
	public final static String ACTION_APPLY_ICONTHEME = "com.yihang.launcherEx.applyicontheme";
	public final static String ACTION_APPLY_WALLPAPERTHEME = "com.yihang.launcherEx.applywallpapertheme";
	public final static String ACTION_APPLY_LOCKWALLPAPERTHEME = "com.yihang.launcherEx.applylockscreenwptheme";
	public final static String ACTION_APPLY_LOCKSCREENTHEME = "com.yihang.launcherEx.applylockscreentheme";
	
	public final static String APPLY_DEFAULT_RESULT = "com.yihang.launcherEx.apply_default";
	
	public final static String PARAM_THME_NAME = "themeName";
	
	public final static String INTENT_CATEGORY_THEME = "com.yihang.launcherEx.category.THEME";
	public final static String INTENT_CATEGORY_WALLPAPER = "com.yihang.launcherEx.category.WALLPAPER";
	public final static String INTENT_CATEGORY_LAUNCHERICON = "com.yihang.launcherEx.category.LAUNCHERICON";
	public final static String INTENT_CATEGORY_LOCKWALLPAPER = "com.yihang.launcherEx.category.LOCKWALLPAPER";
	public final static String INTENT_CATEGORY_LOCKSCREEN = "com.yihang.launcherEx.category.LOCKSCREEN";

	public final static int APP_ICON_BG_MAX_COUNT = 10;
	
	public enum ThemeType {
		THEME_DEFAULT,
		THEME_CUSTOM,
		THEME_INVALIDE
	};
	
	public final static int CATEGORY_THEME = 100;
	public final static int CATEGORY_WALLPAPER = 101;
	public final static int CATEGORY_LAUNCHERICON = 102;
	public final static int CATEGORY_LOCKWALLPAPER = 103;
	public final static int CATEGORY_LOCKSCREEN = 104;
	public final static int CATEGORY_LAUNCHER = 105;//Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01
	
	public final static int ACTIVITY_RESULT_CANCEL = 200; 
	public final static int ACTIVITY_RESULT_APPLAY = 201;
	public final static int ACTIVITY_RESULT_DELETE = 202; 
	
	//theme preview image array name
	public final static String ARRAY_PRE_THEME         = "theme_preview";
	public final static String ARRAY_PRE_LAUNCHERICON  = "theme_icon_preview";
	public final static String ARRAY_PRE_LOCKSCREEN    = "theme_lock_preview";
	public final static String ARRAY_PRE_WALLPAPER     = "theme_wallpaper_preview";
	public final static String ARRAY_PRE_LOCKWALLPAPER = "theme_lock_wallpaper_preview";
	
	//theme provider
	public final static String THEME         = "theme";
	public final static String LAUNCHERICON  = "launcherIcon";
	public final static String LOCKSCREEN    = "lockscreen";
	public final static String WALLPAPER     = "wallpaper";
	public final static String LOCKWALLPAPER = "lockWallpaper";
	
	public final static String URI_THEMES    = "themes";
	public final static String URI_THEMEITEM = "theme_item";
	public final static String URI_THEMELOCK = "theme_lock";
	public final static String URI_THEMELAUNCHER = "theme_launcher";//Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01
	
	public static final String AUTHORITY      = "com.yihang.thememgr.Settings";
	public static final Uri    CONTENT_URI    = Uri.parse("content://"+ AUTHORITY + "/theme");
	public static final String THEME_CATEGORY = "category";
	public static final String THEME_TYPE     = "type";
	public static final String PACKAGE_NAME   = "package_name";
	public static final String THEME_NAME     = "theme_name";
	public static final String AUTHOR         = "author";
	public static final String SUPPORTVER     = "support_version";
	public static final String PARAMETER_NOTIFY = "notify";
	
	public static final String THUMBIMG_THEME         = "thumb_theme";
	public static final String THUMBIMG_LAUNCHERICON  = "thumb_launcher";
	public static final String THUMBIMG_WALLPAPER     = "thumb_wallpaper";
	public static final String THUMBIMG_LOCKSCREEN    = "thumb_lockscreen";
	public static final String THUMBIMG_LOCKWALLPAPER = "thumb_lock_wallpaper";
	

	public static boolean findPackage(String packageName, PackageManager packageManager){
    	if (packageName == null || packageName.equals("")) {
    		  return false; 
    	}
    	try {
			packageManager.getPackageInfo(packageName,PackageManager.PERMISSION_GRANTED);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
    }
	
	public static int getThemeType(ThemeType type){
		int value = 0;
		switch (type) {
		case THEME_DEFAULT:
			value = 10;
			break;
		case THEME_CUSTOM:
			value = 100;
			break;
		default:
			break;
		}
		return value;
	}
	
	public static ThemeType getThemeType(int type){
		ThemeType value = ThemeType.THEME_INVALIDE;
		switch (type) {
		case 10:
			value = ThemeType.THEME_DEFAULT;
			break;
		case 100:
			value = ThemeType.THEME_CUSTOM;
			break;
		default:
			break;
		}
		return value;
	}
	
	public static String getThemeIntentAction(int category){
		String value = null;
		switch (category) {
		case CATEGORY_THEME:
			value = ACTION_APPLY_THEME;
			break;
		case CATEGORY_LAUNCHERICON:
			value = ACTION_APPLY_ICONTHEME;
			break;
		case CATEGORY_LOCKSCREEN:
			value = ACTION_APPLY_LOCKSCREENTHEME;
			break;
		case CATEGORY_WALLPAPER:
			value = ACTION_APPLY_WALLPAPERTHEME;
			break;
		case CATEGORY_LOCKWALLPAPER:
			value = ACTION_APPLY_LOCKWALLPAPERTHEME;
			break;			
		default:
			break;
		}
		return value;
	}
	
	public static String getThemeIntentCategary(int category){
		String value = null;
		switch (category) {
		case CATEGORY_THEME:
			value = INTENT_CATEGORY_THEME;
			break;
		case CATEGORY_LAUNCHERICON:
        case CATEGORY_LAUNCHER:    //Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01
			value = INTENT_CATEGORY_LAUNCHERICON;
			break;
		case CATEGORY_LOCKSCREEN:
			value = INTENT_CATEGORY_LOCKSCREEN;
			break;
		case CATEGORY_WALLPAPER:
			value = INTENT_CATEGORY_WALLPAPER;
			break;
		case CATEGORY_LOCKWALLPAPER:
			value = INTENT_CATEGORY_LOCKWALLPAPER;
			break;			
		default:
			break;
		}
		return value;
	}
	
	public static Uri getThemeUri(int category){
		String value = null;
		switch (category) {
		case CATEGORY_THEME:
			value = URI_THEMES;
			break;
        /* Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01 begin */
		case CATEGORY_LAUNCHER:
			value = URI_THEMELAUNCHER;
			break;
		/* Add By liyichong to add switch theme activity (QL1001) SW00061906 2014-07-01 end */
		case CATEGORY_LOCKSCREEN:
			value = URI_THEMELOCK;
			break;
			
		case CATEGORY_LAUNCHERICON:
		case CATEGORY_WALLPAPER:
		case CATEGORY_LOCKWALLPAPER:
			value = URI_THEMEITEM;
			break;

		default:
			break;
		}
 
		return Uri.parse("content://" + AUTHORITY + "/" + ThemeProvider.TABLE_NAME + "/" + 
				value + "?" + ThemeUtils.PARAMETER_NOTIFY + "=" + true);
		
	}
	
	public static  String[] getThemeSelArgs(int category){
		String[] value = null;
		switch (category) {
		case CATEGORY_LAUNCHERICON:
			value = new String[]{LAUNCHERICON};
			break;
//		case CATEGORY_LOCKSCREEN:
//			value = new String[]{LOCKSCREEN};
//			break;
		case CATEGORY_WALLPAPER:
			value = new String[]{WALLPAPER};
			break;
		case CATEGORY_LOCKWALLPAPER:
			value = new String[]{LOCKWALLPAPER};
			break;			
		default:
			break;
		}
		return value;
	}
}