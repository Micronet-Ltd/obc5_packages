package com.qrt.factory.util;

import com.qrt.factory.R;
import com.qrt.factory.domain.AutoTestItem;
import com.qrt.factory.domain.TestItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import java.io.IOException;
import 	java.lang.Exception;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA. Owner: wangwenlong Date: 2012:09:26 Time: 18:08
 */
public class XmlUtil {
	private static final String TAG = "XmlUtil";
	private static HashSet<String> mBlackList = new HashSet<String>();

    public static List<TestItem>[] loadTestItems(Context context, int workType, boolean isMicronet)
            throws XmlPullParserException, IOException,
            PackageManager.NameNotFoundException {
        List<TestItem> autoTestItems = new ArrayList<TestItem>();
        List<TestItem> singleTestItems = new ArrayList<TestItem>();
		
		

        int xmlId = getItemResId(workType);
		if (isMicronet) {
			fillBlackListFromXml(context, R.xml.item_black_list_micronet, mBlackList);
		}
		fillTestItemsFromXml(context, xmlId, autoTestItems, singleTestItems, mBlackList);
		if (isMicronet) {
			fillTestItemsFromXml(context, R.xml.item_config_micronet, 
								 autoTestItems, singleTestItems, null);
		}
		return new List[] { autoTestItems, singleTestItems };
    }
	
	private static void fillTestItemsFromXml(Context context,  
											 int xmlId, 
											 List<TestItem> autoTestItems, 
											 List<TestItem> singleTestItems,
											 HashSet<String> blackList) 
											 throws XmlPullParserException, IOException,
													PackageManager.NameNotFoundException{
		boolean auto = false;
		TestItem testItem = null;
		PackageManager mPackageManager = context.getPackageManager();
        XmlPullParser mXmlPullParser = context.getResources().getXml(xmlId);
        int mEventType = mXmlPullParser.getEventType();
		
        while (mEventType != XmlPullParser.END_DOCUMENT) {
            if (mEventType == XmlPullParser.START_TAG) {

                String tagName = mXmlPullParser.getName();
				if ("Auto".equals(tagName)) {
                    auto = true;
                } else if ("Single".equals(tagName)) {
                    auto = false;
                } else if ("TestItem".equals(tagName)) {
                    if (auto) {
                        testItem = new AutoTestItem();
                    } else {
                        testItem = new TestItem();
                    }

                    String enable = mXmlPullParser
                            .getAttributeValue(null, "enable");
                    if ("false".equals(enable)) {
                        mEventType = mXmlPullParser.nextTag();
                        continue;
                    }
                    String name = mXmlPullParser
                            .getAttributeValue(null, "name");
					testItem.setName(name);
					

                } else if ("Intent".equals(tagName)) {
                    String packageName = mXmlPullParser.getAttributeValue(null, "packageName");
					String className = mXmlPullParser.getAttributeValue(null, "className");
                    if (testItem != null) {
                        Intent intent = new Intent();
                        intent.setClassName(packageName, className);
                        testItem.setIntent(intent);
                        ActivityInfo activityInfo = mPackageManager.getActivityInfo(new ComponentName(packageName, className), 0);
                        testItem.setTitle((String) activityInfo.loadLabel(mPackageManager));
                    }
                }
            } else if (mEventType == XmlPullParser.END_TAG) {
                String tagName = mXmlPullParser.getName();

                if ("Auto".equals(tagName)) {
                    auto = false;
                } else if ("TestItem".equals(tagName) && testItem != null) {
					if (blackList != null && blackList.contains(testItem.getName())) {
						// this test item is black listed 
						try {Utilities.logd(TAG, "====================not edded to list because it is in the black list: " 
											+ testItem.getName()); } catch (Exception e) { }
						Utilities.logd("black listed TestItem", testItem.toString());
					} else {
						// included test item
						if (auto) {
							autoTestItems.add(testItem);
							try {Utilities.logd(TAG, "=================added new auto test activity, name: " + testItem.getName()); } catch (Exception e) { }
						} else {
							singleTestItems.add(testItem);
							try {Utilities.logd(TAG, "=================added new single test activity, name: " + testItem.getName()); } catch (Exception e) { }
						}
						Utilities.logd("included TestItem", testItem.toString());
					}
				}
            }
            mEventType = mXmlPullParser.next();
        }
	}

	private static void fillBlackListFromXml(Context context, int xmlId, HashSet<String> blackList)
						throws XmlPullParserException, IOException,
								PackageManager.NameNotFoundException {
		String blackListItem = null;
		boolean isBlaclList = false;
		PackageManager mPackageManager = context.getPackageManager();
        XmlPullParser mXmlPullParser = context.getResources().getXml(xmlId);
        int mEventType = mXmlPullParser.getEventType();
		try {Utilities.logd(TAG, "====================in fillBlackListFromXml"); } catch (Exception e) { }
        while (mEventType != XmlPullParser.END_DOCUMENT) {
            if (mEventType == XmlPullParser.START_TAG) {
				try {Utilities.logd(TAG, "====================in fillBlackListFromXml start tag"); } catch (Exception e) { }
                String tagName = mXmlPullParser.getName();
				try {Utilities.logd(TAG, "====================in fillBlackListFromXml tag: " + tagName); } catch (Exception e) { }
				if ("BlackList".equals(tagName)) {
					
                    isBlaclList = true;
				} else if ("TestItem".equals(tagName)) {
					try {Utilities.logd(TAG, "====================in fillBlackListFromXml tag: " + tagName); } catch (Exception e) { }
					String name = mXmlPullParser.getAttributeValue(null, "name");
					if (isBlaclList) {
						try {Utilities.logd(TAG, "====================added to black list: " + name); } catch (Exception e) { }
						blackList.add(name);
					
					}
                } 
                
            } else if (mEventType == XmlPullParser.END_TAG) {
				try {Utilities.logd(TAG, "====================in fillBlackListFromXml start tag"); } catch (Exception e) { }
                String tagName = mXmlPullParser.getName();

                if ("BlackList".equals(tagName)) {
                    isBlaclList = false;
				}
			}
            mEventType = mXmlPullParser.next();
        }
	}
	
    private static int getItemResId(int workType) {
        switch (workType) {
            case 1:
                return R.xml.item_config_boot_test;
            case 2:
                return R.xml.item_config_attachment;
            case 0:
                return R.xml.item_config_default;
			case 3:
				return R.xml.item_config_default_for_dsds;
			case 4:
				return R.xml.item_config_default_bd;
			case 5:
				return R.xml.item_config_default_for_dsds_bd;
			case 6:
				return R.xml.item_config_default_for_dsds_gpsbd;
			case 7:
				return R.xml.item_config_default_gpsbd;
			
            default:
                return R.xml.item_config_default;
        }
    }
}
