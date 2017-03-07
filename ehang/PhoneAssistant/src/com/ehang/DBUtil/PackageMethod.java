package com.ehang.dbutil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class PackageMethod {
	Context mcontext;

	public PackageMethod(Context context) {

		mcontext = context;

	}

	private ArrayList localArrayList;

	public List<ApplicationInfo> getAppInfos() {
		localArrayList = new ArrayList();
		localArrayList.clear();
		PackageManager mPackageManager = mcontext.getPackageManager();

		Iterator localIterator = mPackageManager.getInstalledPackages(
				PackageManager.GET_GIDS).iterator();

		if (!localIterator.hasNext())
			return null;
		PackageInfo localPackageInfo = null;
		while (localIterator.hasNext()) {
			localPackageInfo = (PackageInfo) localIterator.next();
			if ((localPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {

			} else {

				localArrayList.add(localPackageInfo.applicationInfo);
			}
		}

		return localArrayList;
	}

}
