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
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. Owner: wangwenlong Date: 2012:09:26 Time: 18:08
 */
public class XmlUtil {

    public static List<TestItem>[] loadTestItems(Context context, int workType)
            throws XmlPullParserException, IOException,
            PackageManager.NameNotFoundException {

        boolean auto = false;
        List<TestItem> autoTestItem = new ArrayList<TestItem>();
        List<TestItem> singleTestItem = new ArrayList<TestItem>();

        PackageManager mPackageManager = context.getPackageManager();

        int xmlId = getItemResId(workType);
        XmlPullParser mXmlPullParser = context.getResources().getXml(xmlId);

        int mEventType = mXmlPullParser.getEventType();

        TestItem testItem = null;

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
                    String packageName = mXmlPullParser
                            .getAttributeValue(null, "packageName");
                    String className = mXmlPullParser
                            .getAttributeValue(null, "className");
                    if (testItem != null) {
                        Intent intent = new Intent();
                        intent.setClassName(packageName, className);
                        testItem.setIntent(intent);
                        ActivityInfo activityInfo = mPackageManager
                                .getActivityInfo(
                                        new ComponentName(packageName,
                                                className), 0);
                        testItem.setTitle(
                                (String) activityInfo
                                        .loadLabel(mPackageManager));
                    }
                }
            } else if (mEventType == XmlPullParser.END_TAG) {
                String tagName = mXmlPullParser.getName();

                if ("Auto".equals(tagName)) {
                    auto = false;
                } else if ("TestItem".equals(tagName) && testItem != null) {
                    if (auto) {
                        autoTestItem.add(testItem);
                    } else {
                        singleTestItem.add(testItem);
                    }
                    Utilities.logd("TestItem", testItem.toString());
                }
            }
            mEventType = mXmlPullParser.next();
        }
        return new List[]{autoTestItem, singleTestItem};
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

            default:
                return R.xml.item_config_default;
        }
    }
}
