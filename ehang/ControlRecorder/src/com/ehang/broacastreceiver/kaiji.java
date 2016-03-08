package com.ehang.broacastreceiver;

import com.ehang.show.ShowActivity;
import com.security.controlrecorder.Main;
import java.io.File;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.os.SystemProperties;
import android.content.pm.PackageManager;
import java.util.List;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import com.security.db.shezhi;
import android.os.Environment;
import java.util.ArrayList;
import com.security.db.UserDaoImpl;
public class kaiji extends BroadcastReceiver {
	List<Integer> ll;
	private static ArrayList localArrayList;
	private PackageManager mPackageManager;
	
	private static ApplicationInfo localapplicationInfo;

	public kaiji() {

	}

	public kaiji(Context content) {
		content = content;

	}

	static final String action_boot = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		Main m = new Main(context);
		// 拿到开关的状态
		boolean boo = context.getSharedPreferences("switch",
				Context.MODE_PRIVATE).getBoolean("open", false);
		File file = context.getApplicationContext()
				.getDatabasePath("xinren.db");
		// Log.e("kaiji", "file:"+file);
		// String path=Environment.getExternalStorageDirectory().getPath()+"//";
		// File f = new File(path
		// +"/data/data/com.example.controlrecorder/databases/xinren.db");
		// Log.e("kaiji", "file:"+file);
		// Log.e("kaiji", "boo:"+boo);
		// System.out.println("com.example.kaiji");
		// Log.e("kaiji", "welcome to kaijijijiji");
		// Log.e("kaiji", "file.exists():"+file.exists());
		// 刚开机的时候，那么就执行程序应有的功能(如果开关是打开的，那么就把没选的全部禁止，把信任的设为信任)
		if (boo) {
			if (file.exists()) {
				SystemProperties.set("persist.sys.switch_thief", "false");
				// 把未选中的设为禁止
				m.shi();
				// Log.e("com.example。kaiji", ""+"运行的怎么样");
				// 把状态是1的选择为信任
				m.dange();
				SystemProperties.set("persist.sys.switch_thief", "true");
			} else {
				mPackageManager = context.getPackageManager();
				List list4 = m.getAppInfos();

				for (int i = 0; i < list4.size(); i++) {
					localapplicationInfo = (ApplicationInfo) list4.get(i);
					// 閸栧懎鎮�
					String pname = localapplicationInfo.packageName;
					String yname = null;

					try {
						yname = mPackageManager.getApplicationLabel(
								mPackageManager.getApplicationInfo(pname,
										PackageManager.GET_META_DATA))
								.toString();
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// 閺嶈宓侀崠鍛倳瀵版鍩岄惄绋跨安閻ㄥ嫬绨查悽銊ユ倳
					// if ((localapplicationInfo.flags &
					// ApplicationInfo.FLAG_SYSTEM) > 0) {

					// }else{
					int state1 = 0;
					Object param1[] = { state1, pname };
					shezhi sz1 = new shezhi();
					boolean flag1 = sz1.addUser(param1, context);
					// }
				}
				// 缁楊兛绔村▎陇绻橀弶銉︽閿涘本濡哥敮锔芥箒瑜版洟鐓堕惃鍕閺堝绨查悽銊ф畱瑜版洟鐓堕弶鍐鐏炲繗鏂�閹猴拷

				SystemProperties.set("persist.sys.switch_thief", "false");
				m.start();
				SystemProperties.set("persist.sys.switch_thief", "true");

			}

		}/*else{
				m.checkOp();
		// System.out.println("长度到底是多少?" + ll.size());
		for (int i = 0; i < ll.size(); i++) {
			boolean aa = true;
			while (localArrayList != null && aa) {
				localapplicationInfo = (ApplicationInfo) localArrayList.get(i);
				String name = localapplicationInfo.packageName;
				int oo = ll.get(i);
				// if ((localapplicationInfo.flags &
				// ApplicationInfo.FLAG_SYSTEM) > 0) {

				// }else{
				UserDaoImpl userdao = new UserDaoImpl();
				// System.out.println("这里得到的是" + name);
				Object param[] = { oo, name };
				//
				boolean flag = userdao.addUser(param, context);

				// }
				aa = false;
				// System.out.println("插入数据库是否成功" + flag);
			}
		}
		}*/

	}
}
