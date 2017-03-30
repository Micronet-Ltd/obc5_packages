package com.ehang.broacastreceiver;

import java.util.List;
import com.ehang.show.ShowActivity;
import com.security.controlrecorder.Main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.os.SystemProperties;
import com.security.db.shezhi;
import com.security.db.UserDaoImpl;
import java.io.File;
import android.content.pm.PackageManager.NameNotFoundException;

public class appUpdate extends BroadcastReceiver {
	private PackageManager mPackageManager;
	private ApplicationInfo localapplicationInfo;

	public appUpdate() {

	}

	public appUpdate(Context content) {
		content = content;

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Log.e("鑳芥帴鏀跺埌骞挎挱鍚�","sadsds");
		boolean boo = context.getSharedPreferences("switch",
				Context.MODE_PRIVATE).getBoolean("open", false);
		// Log.e("寮�鍏崇殑鐘舵�佹槸", ""+boo);
		// TODO Auto-generated method stub
		// 鎺ユ敹瀹夎骞挎挱
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
			String packageName = intent.getDataString();
			// 鎶婂寘鍚嶆埅鍙栧嚭鏉�
			String[] strs = packageName.split("[:]");
			String pname = strs[1];
			// Log.e("杩欐墠鏄寘鍚�", ""+pname);
			// System.out.println("瀹夎浜�:" +pname + "鍖呭悕鐨勭▼搴�");
			// 鍒ゆ柇鏌愪釜搴旂敤鏄惁鏈夊綍闊虫潈闄�
			Main mm = new Main(context);
			List<ApplicationInfo> lo = mm.getAppInfos();
			File file = context.getApplicationContext().getDatabasePath(
					"xinren.db");
			// 寰幆鍑烘墍鏈夊甫鏈夊綍闊虫潈闄愮殑鍖呭悕
			if (file.exists()) {
				for (int i = 0; i < lo.size(); i++) {
					ApplicationInfo localapplicationInfo = (ApplicationInfo) lo
							.get(i);
					// 寰楀埌搴旂敤鍚嶇О锛屾斁鍦ㄦ暟缁勯噷杈�
					String name = localapplicationInfo.packageName;
					if (pname.equals(name)) {
						// Log.e("appupdate", ""+pname);
						int state1 = 0;
						Object param1[] = { state1, pname };
						shezhi sz1 = new shezhi();
						boolean flag1 = sz1.addUser(param1, context);
						SystemProperties.set("persist.sys.switch_thief",
								"false");
						Integer permission = mm.yigepermission(pname);
						SystemProperties
								.set("persist.sys.switch_thief", "true");
						int pp = permission.intValue();
						// Log.e("appupdate", ""+pp);
						UserDaoImpl userdao = new UserDaoImpl();
						// System.out.println("杩欓噷寰楀埌鐨勬槸" + name);
						Object param[] = { pp, pname };
						boolean flag = userdao.addUser(param, context);
						// 璇存槑鏂板畨瑁呯殑搴旂敤鏈夊綍闊冲姛鑳�,閭ｄ箞灏辩鎺夊叾鏉冮檺
						// Log.e("appupdate", "boo:"+boo);
						if (boo) {
							SystemProperties.set("persist.sys.switch_thief",
									"false");
							mm.yige(pname);
							SystemProperties.set("persist.sys.switch_thief",
									"true");
						} else {
							SystemProperties.set("persist.sys.switch_thief",
									"false");
						}

					}

				}
			} else {
				mPackageManager = context.getPackageManager();
				List list4 = mm.getAppInfos();

				for (int j = 0; j < list4.size(); j++) {
					localapplicationInfo = (ApplicationInfo) list4.get(j);
					// 闁告牕鎳庨幃锟�
					String ppname = localapplicationInfo.packageName;
					String yname = null;

					try {
						yname = mPackageManager.getApplicationLabel(
								mPackageManager.getApplicationInfo(ppname,
										PackageManager.GET_META_DATA))
								.toString();
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// 闁哄秷顫夊畵渚�宕犻崨顓熷�崇�电増顨呴崺宀勬儎缁嬭法瀹夐柣銊ュ缁ㄦ煡鎮介妸銉﹀��
					// if ((localapplicationInfo.flags &
					// ApplicationInfo.FLAG_SYSTEM) > 0) {

					// }else{
					int state1 = 0;
					Object param1[] = { state1, ppname };
					shezhi sz1 = new shezhi();
					boolean flag1 = sz1.addUser(param1, context);
					Integer permissions = mm.yigepermission(ppname);
					int pps = permissions.intValue();
					UserDaoImpl userdao = new UserDaoImpl();
					Object param[] = { pps, ppname };
					boolean flag = userdao.addUser(param, context);
				}

				//SystemProperties.set("persist.sys.switch_thief", "false");
				//mm.start();
				//SystemProperties.set("persist.sys.switch_thief", "true");
				
				// System.out.println("这里得到的是" + name);
			
			}
			// Log.e("瀹夎浜嗘柊搴旂敤", ""+packageName);

		}
		// 鎺ユ敹鍗歌浇骞挎挱
		if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
			File file = context.getApplicationContext().getDatabasePath(
					"xinren.db");
			String packageName = intent.getDataString();
			// Log.e("appupdate", "::::"+packageName);
			// System.out.println("鍗歌浇浜�:" + packageName + "鍖呭悕鐨勭▼搴�");
			// 璋冪敤鍔犺浇淇℃伅鐨勬柟娉曪紝鏇存柊鍒楄〃
			Main mm = new Main(context);
			List<ApplicationInfo> lo = mm.getAppInfos();
			// 寰幆鍑烘墍鏈夊甫鏈夊綍闊虫潈闄愮殑鍖呭悕
			if (!file.exists()) {

			} else {
				for (int i = 0; i < lo.size(); i++) {
					ApplicationInfo localapplicationInfo = (ApplicationInfo) lo
							.get(i);
					// 寰楀埌搴旂敤鍚嶇О锛屾斁鍦ㄦ暟缁勯噷杈�
					String name = localapplicationInfo.packageName;
					String[] strs = packageName.split(":");
					String pname = strs[1];
					// if(pname.equals(name)){
					Object param1[] = { pname };
					shezhi sz1 = new shezhi();
					boolean flag1 = sz1.deleteUser(param1, context);

					UserDaoImpl userdao = new UserDaoImpl();
					// System.out.println("杩欓噷寰楀埌鐨勬槸" + name);
					Object param[] = { pname };
					boolean flag = userdao.deleteUser(param, context);

					// }

				}
				// Log.e("搴旂敤琚嵏杞戒簡", ""+packageName);
			}
		}
	}

	public void onepackage() {

	}

}
