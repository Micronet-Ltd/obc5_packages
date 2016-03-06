package com.security.controlrecorder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.security.db.DBUtil;
import com.security.db.UserDaoImpl;
import com.security.db.shezhi;

import android.R.array;
import android.R.integer;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.JetPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Main {
	Context mcontext;
	public static final int OP_RECORD_AUDIO = 27;
	private Button start;
	public static final int MODE_ALLOWED = 0;
	public static final int MODE_IGNORED = 1;
	public static final int MODE_ERRORED = 2;
	private static final int MODE_ASK = 3;
	private Button close;
	private static AppOpsManager mAppOpsm;
	private Iterator localIterator;
	private static ArrayList localArrayList;
	private static ApplicationInfo localapplicationInfo;
	private int m;
	private List LocaList1;
	List<Integer> ll;
	private PackageManager mPackageManager;

	public Main(Context context) {
		// 获取AppOpsManager的服务
		mAppOpsm = (AppOpsManager) context
				.getSystemService(Context.APP_OPS_SERVICE);
		mcontext = context;

		// start();
		// close();
		// SetPermission() ;
		// 分别给两个按钮设置监听
		// start.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// // 找出系统中带有录音权限的应用程序
		// // 设置权限不可用
		// checkOp();
		// System.out.println("长度到底是多少?" + ll.size());
		// for(int i = 0; i < ll.size(); i++) {
		// boolean aa=true;
		// while (localArrayList != null&&aa) {
		// localapplicationInfo = (ApplicationInfo) localArrayList
		// .get(i);
		// String name = localapplicationInfo.packageName;
		// int oo = ll.get(i);
		// UserDaoImpl userdao = new UserDaoImpl();
		// System.out.println("这里得到的是"
		// + name);
		// Object param[] = {oo,name};
		// boolean flag = userdao
		// .addUser(param,MainActivity.this);
		// aa=false;
		// System.out.println("插入数据库是否成功" + flag);
		// }
		// }
		// setAppRecorderMode(true);
		// System.out.println("录音权限已屏蔽，请放心通话" + MODE_IGNORED);
		// }
		// });
		// 录音权限恢复正常，则此时的权限值是o,
		// close.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// // 恢复原来的状态
		// setAppRecorderMode(false);
		// System.out.println("录音权限恢复正常" + MODE_ALLOWED);
		// //把“state”值是1的应用查询出来，，。把其权限设置成1对应的状态，即可达到恢复原来状态的效果
		// UserDaoImpl userdao=new UserDaoImpl();
		// List<Map<String, Object>> op=userdao.list(MainActivity.this);
		// System.out.println("原始权限是1的应用有："+op.size());
		// //把这个集合里边的应用的权限设置成禁止就可以实现恢复，再次利用反射去实现
		//
		// for (Map<String, Object> m : op) {
		//
		// for (String k : m.keySet()) {
		// int uid = 0;
		// String packageName= (String) m.get(k);
		// ApplicationInfo ai;
		// try {
		// ai = mPackageManager.getApplicationInfo(packageName,
		// PackageManager.GET_ACTIVITIES);
		// uid=ai.uid;
		// System.out.println("uid到底是对少？"+uid);
		// } catch (NameNotFoundException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		//
		// System.out.println("njkdsfjhsdfhjsdfhsdhfkhsdfhjkdhskjfhadfjkshdfhdjsfks"+packageName);
		//
		// Class mAppOpsManagerClass = mAppOpsm.getClass();
		// try {
		// Class[] cc = new Class[4];
		// cc[0] = Integer.TYPE;
		// cc[1] = Integer.TYPE;
		// cc[2] = String.class;
		// cc[3] = Integer.TYPE;
		// Method method;
		// method = mAppOpsManagerClass.getDeclaredMethod("setMode", cc);
		// AppOpsManager localAppOpsManager = MainActivity.this.mAppOpsm;
		// Object[] arrayOfObject = new Object[4];
		// arrayOfObject[0] = Integer.valueOf(27);
		// arrayOfObject[1] = Integer.valueOf(uid);
		// arrayOfObject[2] = packageName;
		// arrayOfObject[3] = Integer.valueOf(1);
		// method.invoke(localAppOpsManager, arrayOfObject);
		// //Toast.makeText(MainActivity.this, "恢复成功",
		// Toast.LENGTH_SHORT).show();
		// } catch (NoSuchMethodException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalArgumentException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		//
		// }
		//
		// }
		// });
	}

	// 写两个方法，用来代替开启和关闭开关
	public void start() {
		// 找出系统中带有录音权限的应用程序
		// 设置权限不可用
		checkOp();
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
				boolean flag = userdao.addUser(param, mcontext);

				// }
				aa = false;
				// System.out.println("插入数据库是否成功" + flag);
			}
		}
		setAppRecorderMode(true);
		// System.out.println("录音权限已屏蔽，请放心通话" + MODE_IGNORED);
	}

	public void close() {
		int a = 0;
		// 恢复原来的状态
		// setAppRecorderMode(false);
		// System.out.println("录音权限恢复正常" + MODE_ALLOWED);
		// 把“state”值是1,3的应用查询出来，，。把其权限设置成1对应的状态，即可达到恢复原来状态的效果
		UserDaoImpl userdao = new UserDaoImpl();

		List<List<Map<String, Object>>> op = userdao.listall(mcontext);
		List<Map<String, Object>> zt = op.get(0);
		List<Map<String, Object>> pkg = op.get(1);

		// Log.e("main","  packageName= "+op.size()+"  zt="+zt.size());

		// 把这个集合里边的应用的权限设置成禁止就可以实现恢复，再次利用反射去实现
		for (int i = 0; i < zt.size(); i++) {
			int ztz = (int) (zt.get(i).get("state"));
			String packageName = (String) (pkg.get(i).get("pkg"));
			// Log.e("main","ztz:"+ztz+"  packageName= "+packageName);
			int uid = 0;

			ApplicationInfo ai;
			try {
				ai = mcontext.getPackageManager().getApplicationInfo(
						packageName, PackageManager.GET_ACTIVITIES);
				uid = ai.uid;
				// System.out.println("uid到底是对少？" + uid);
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Class mAppOpsManagerClass = mAppOpsm.getClass();
			try {
				Class[] cc = new Class[4];
				cc[0] = Integer.TYPE;
				cc[1] = Integer.TYPE;
				cc[2] = String.class;
				cc[3] = Integer.TYPE;
				Method method;
				method = mAppOpsManagerClass.getDeclaredMethod("setMode", cc);
				AppOpsManager localAppOpsManager = Main.this.mAppOpsm;
				Object[] arrayOfObject = new Object[4];
				arrayOfObject[0] = Integer.valueOf(27);
				arrayOfObject[1] = Integer.valueOf(uid);
				arrayOfObject[2] = packageName;
				arrayOfObject[3] = Integer.valueOf(ztz);
				method.invoke(localAppOpsManager, arrayOfObject);
				// Toast.makeText(MainActivity.this, "恢复成功",
				// Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	// 查询状态是1的
	/*
	 * List<Map<String, Object>> op = userdao.list(mcontext); //
	 * 把这个集合里边的应用的权限设置成禁止就可以实现恢复，再次利用反射去实现 for (Map<String, Object> m : op) {
	 * for (String k : m.keySet()) { int uid = 0; String packageName = (String)
	 * m.get(k); ApplicationInfo ai; try { ai =
	 * mcontext.getPackageManager().getApplicationInfo(packageName,
	 * PackageManager.GET_ACTIVITIES); uid = ai.uid;
	 * //System.out.println("uid到底是对少？" + uid); } catch (NameNotFoundException
	 * e1) { // TODO Auto-generated catch block e1.printStackTrace(); } Class
	 * mAppOpsManagerClass = mAppOpsm.getClass(); try { Class[] cc = new
	 * Class[4]; cc[0] = Integer.TYPE; cc[1] = Integer.TYPE; cc[2] =
	 * String.class; cc[3] = Integer.TYPE; Method method; method =
	 * mAppOpsManagerClass.getDeclaredMethod("setMode", cc); AppOpsManager
	 * localAppOpsManager = Main.this.mAppOpsm; Object[] arrayOfObject = new
	 * Object[4]; arrayOfObject[0] = Integer.valueOf(27); arrayOfObject[1] =
	 * Integer.valueOf(uid); arrayOfObject[2] = packageName; arrayOfObject[3] =
	 * Integer.valueOf(1); method.invoke(localAppOpsManager, arrayOfObject); //
	 * Toast.makeText(MainActivity.this, "恢复成功", // Toast.LENGTH_SHORT).show();
	 * } catch (NoSuchMethodException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (IllegalAccessException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } catch
	 * (IllegalArgumentException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (InvocationTargetException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } } } // 查询状态是3的,并回复到原来的值
	 * List<Map<String, Object>> op1 = userdao.list2(mcontext); //
	 * 把这个集合里边的应用的权限设置成禁止就可以实现恢复，再次利用反射去实现 for (Map<String, Object> m : op1) {
	 * for (String k : m.keySet()) { int uid = 0; String packageName = (String)
	 * m.get(k); ApplicationInfo ai; try { ai =
	 * mcontext.getPackageManager().getApplicationInfo(packageName,
	 * PackageManager.GET_ACTIVITIES); uid = ai.uid;
	 * //System.out.println("uid到底是对少？" + uid); } catch (NameNotFoundException
	 * e1) { // TODO Auto-generated catch block e1.printStackTrace(); }
	 * 
	 * Class mAppOpsManagerClass = mAppOpsm.getClass(); try { Class[] cc = new
	 * Class[4]; cc[0] = Integer.TYPE; cc[1] = Integer.TYPE; cc[2] =
	 * String.class; cc[3] = Integer.TYPE; Method method; method =
	 * mAppOpsManagerClass.getDeclaredMethod("setMode", cc); AppOpsManager
	 * localAppOpsManager = Main.this.mAppOpsm; Object[] arrayOfObject = new
	 * Object[4]; arrayOfObject[0] = Integer.valueOf(27); arrayOfObject[1] =
	 * Integer.valueOf(uid); arrayOfObject[2] = packageName; arrayOfObject[3] =
	 * Integer.valueOf(3); method.invoke(localAppOpsManager, arrayOfObject); //
	 * Toast.makeText(MainActivity.this, "恢复成功", // Toast.LENGTH_SHORT).show();
	 * } catch (NoSuchMethodException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (IllegalAccessException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } catch
	 * (IllegalArgumentException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (InvocationTargetException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } } }
	 * 
	 * // 一次程序运行完后，把数据库中的信息删除 UserDaoImpl userdao1 = new UserDaoImpl(); ;
	 * boolean flag1 = userdao1.deleteUser(mcontext);
	 * //System.out.println("是否删除成功" + flag1); // } // }
	 */

	// }
	/*
	 * 找出带有录音权限的应用
	 */
	// applicationinfo是从一个特定的应用得到信息，得到<application>标签中的信息
	public List<ApplicationInfo> getAppInfos() {
		localArrayList = new ArrayList();
		// 获取PackageManager对象，它主要是获得已安装程序的信息
		mPackageManager = mcontext.getPackageManager();
		// 得到已安装的带有权限的应用程序
		localIterator = mPackageManager.getInstalledPackages(
				PackageManager.GET_PERMISSIONS).iterator();
		// 如果没有带有应用权限的应用，则返回空
		if (!localIterator.hasNext())
			return null;
		String[] arrayOfString = null;
		while (localIterator.hasNext()) {
			// 如果有此应用的话，则把各个应用元素取出来
			PackageInfo localPackageInfo = null;

			localPackageInfo = (PackageInfo) localIterator.next();

			// System.out.println("得到的各个应用是：" + localPackageInfo);
			arrayOfString = localPackageInfo.requestedPermissions;
			// System.out.println("得到该应用的的权限是：" +
			// Arrays.toString(arrayOfString));
			// 如果应用有权限的话，再执行下边的操作
			if (arrayOfString != null) {
				int leng = arrayOfString.length;
				// System.out.println("权限的长度是：" + leng);
				for (int j = 0; j < leng; ++j) {
					// 数组中的元素没有和录音权限匹配的，那么就继续循环
					if (!arrayOfString[j]
							.equals("android.permission.RECORD_AUDIO"))
						// 在本次循环中继续循环，而不执行循环后的部分
						continue;
					// 把带有录音权限的应用放在localArrayList里边
					if ((localPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {

					} else {
						localArrayList.add(localPackageInfo.applicationInfo);
					}
					// System.out.println("带有录音权限的应用" + localArrayList.size());
					// System.out.println("===============================");
				}
			}
		}
		return localArrayList;
	}

	/*
	 * 查询出带有录音功能的应用
	 */
	public void query() {
		for (int m = 0; m < localArrayList.size(); m++) {
			Object value = localArrayList.get(m);
			// System.out.println("带有录音权限的应用信息" + value);
		}
	}

	public void setAppRecorderMode(boolean paramBoolean) {
		if (paramBoolean == true) {
			// i=1说明权限是禁止的
			int i = 1;
			set(i);
		} else if (paramBoolean == false) {
			// 权限是允许的
			int i = 0;
			set(i);
		}
	}

	public void set(int i) {
		// 调用getApplicationInfo的方法,该方法
		List LocaList = getAppInfos();
		for (int m = 0; m < localArrayList.size(); m++) {
			localapplicationInfo = (ApplicationInfo) localArrayList.get(m);

			// Object param[] = {};
			Class mAppOpsManagerClass = this.mAppOpsm.getClass();
			try {
				Class[] cc = new Class[4];
				cc[0] = Integer.TYPE;
				cc[1] = Integer.TYPE;
				cc[2] = String.class;
				cc[3] = Integer.TYPE;
				Method method;
				method = mAppOpsManagerClass.getDeclaredMethod("setMode", cc);
				AppOpsManager localAppOpsManager = this.mAppOpsm;
				Object[] arrayOfObject = new Object[4];
				arrayOfObject[0] = Integer.valueOf(27);
				arrayOfObject[1] = Integer.valueOf(localapplicationInfo.uid);
				arrayOfObject[2] = localapplicationInfo.packageName;
				arrayOfObject[3] = Integer.valueOf(i);

				method.invoke(localAppOpsManager, arrayOfObject);
				// Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 判断带有录音的应用权限状态，如果m是0就代表有权限，是1的话则没有权限，是2代表权限错误，是3代表权限总是询问
	public void checkOp() {
		List LocaList1 = getAppInfos();
		ll = new ArrayList<Integer>();
		for (int i = 0; i < LocaList1.size(); i++) {
			localapplicationInfo = (ApplicationInfo) localArrayList.get(i);
			// 首先判断Android的版本号
			final int version = Build.VERSION.SDK_INT;
			if (version >= 19) {
				Class c = mAppOpsm.getClass();
				Class[] cArg = new Class[3];
				cArg[0] = Integer.TYPE;
				cArg[1] = Integer.TYPE;
				cArg[2] = String.class;
				try {
					Method method1 = c.getDeclaredMethod("checkOp", cArg);
					// System.out.println("可以不" + localapplicationInfo);
					m = (Integer) method1.invoke(mAppOpsm, Integer.valueOf(27),
							localapplicationInfo.uid,
							localapplicationInfo.packageName);
					// Log.e("main","m:"+m);
					// Log.e("main","packageName:"+localapplicationInfo.packageName);
					// Log.e("main","uid:"+localapplicationInfo.uid);
					ll.add(m);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void dange() {
		// 从数据库中查询出状态是1 的应用
		shezhi sz = new shezhi();
		List<Map<String, Object>> dd = sz.list(mcontext);
		for (Map<String, Object> m : dd) {
			for (String k : m.keySet()) {
				int uid = 0;
				String packageName = (String) m.get(k);
				// System.out.println("????????????????????????"+packageName);
				ApplicationInfo ai = new ApplicationInfo();
				// 从数据库中读出选中的应用
				try {
					ai = mcontext.getPackageManager().getApplicationInfo(
							packageName, PackageManager.GET_ACTIVITIES);
					uid = ai.uid;
				} catch (NameNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Class mAppOpsManagerClass = mAppOpsm.getClass();
				try {
					Class[] cc = new Class[4];
					cc[0] = Integer.TYPE;
					cc[1] = Integer.TYPE;
					cc[2] = String.class;
					cc[3] = Integer.TYPE;
					Method method;
					method = mAppOpsManagerClass.getDeclaredMethod("setMode",
							cc);
					AppOpsManager localAppOpsManager = Main.this.mAppOpsm;
					Object[] arrayOfObject = new Object[4];
					arrayOfObject[0] = Integer.valueOf(27);
					arrayOfObject[1] = Integer.valueOf(uid);
					arrayOfObject[2] = packageName;
					arrayOfObject[3] = Integer.valueOf(0);
					method.invoke(localAppOpsManager, arrayOfObject);
					// Toast.makeText(MainActivity.this, "恢复成功",
					// Toast.LENGTH_SHORT).show();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	public void shi() {
		// 从数据库中查询出状态是0的应用
		shezhi df = new shezhi();
		List<Map<String, Object>> d = df.lo(mcontext);
		for (Map<String, Object> m : d) {
			for (String k : m.keySet()) {
				int uid = 0;
				// 得到的是应用名
				String packageName = (String) m.get(k);
				// System.out.println("shishishshishsihssisisssssssssssssssssssssss");
				ApplicationInfo ai;
				// 从数据库中读出选中的应用
				try {
					ai = mcontext.getPackageManager().getApplicationInfo(
							packageName, PackageManager.GET_ACTIVITIES);
					uid = ai.uid;
				} catch (NameNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Class mAppOpsManagerClass = mAppOpsm.getClass();
				try {
					Class[] cc = new Class[4];
					cc[0] = Integer.TYPE;
					cc[1] = Integer.TYPE;
					cc[2] = String.class;
					cc[3] = Integer.TYPE;
					Method method;
					method = mAppOpsManagerClass.getDeclaredMethod("setMode",
							cc);
					AppOpsManager localAppOpsManager = Main.this.mAppOpsm;
					Object[] arrayOfObject = new Object[4];
					arrayOfObject[0] = Integer.valueOf(27);
					arrayOfObject[1] = Integer.valueOf(uid);
					arrayOfObject[2] = packageName;
					arrayOfObject[3] = Integer.valueOf(1);
					method.invoke(localAppOpsManager, arrayOfObject);
					// Toast.makeText(MainActivity.this, "恢复成功",
					// Toast.LENGTH_SHORT).show();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	// 禁掉单个应用的方法
	public void yige(String name) {
		// PackageManager pm = mcontext.getPackageManager();
		ApplicationInfo ai;
		int uid = 0;
		try {
			ai = mPackageManager.getApplicationInfo(name,
					PackageManager.GET_ACTIVITIES);
			uid = ai.uid;
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Log.e("到这里没有", "打log");
		Class mAppOpsManagerClass = mAppOpsm.getClass();
		try {
			Class[] cc = new Class[4];
			cc[0] = Integer.TYPE;
			cc[1] = Integer.TYPE;
			cc[2] = String.class;
			cc[3] = Integer.TYPE;
			Method method;
			method = mAppOpsManagerClass.getDeclaredMethod("setMode", cc);
			AppOpsManager localAppOpsManager = Main.this.mAppOpsm;
			Object[] arrayOfObject = new Object[4];
			arrayOfObject[0] = Integer.valueOf(27);
			arrayOfObject[1] = Integer.valueOf(uid);
			arrayOfObject[2] = name;
			arrayOfObject[3] = Integer.valueOf(1);
			method.invoke(localAppOpsManager, arrayOfObject);
			// Toast.makeText(MainActivity.this, "恢复成功",
			// Toast.LENGTH_SHORT).show();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void kk() {
		boolean boo = mcontext.getSharedPreferences("switch",
				Context.MODE_PRIVATE).getBoolean("open", true);
	}

	public Integer yigepermission(String name) {
		Integer mm = 0;
		// PackageManager pm = mcontext.getPackageManager();
		ApplicationInfo ai;
		int uid = 0;
		try {
			ai = mPackageManager.getApplicationInfo(name,
					PackageManager.GET_ACTIVITIES);
			uid = ai.uid;
			// Log.e("main","name:"+name);
			// Log.e("main","ai:"+ai);
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Log.e("到这里没有", "打log");

		// 首先判断Android的版本号
		final int version = Build.VERSION.SDK_INT;

		if (version >= 19) {

			try {
				Class c = mAppOpsm.getClass();
				Class[] cArg = new Class[3];
				cArg[0] = Integer.TYPE;
				cArg[1] = Integer.TYPE;
				cArg[2] = String.class;

				Method method = c.getDeclaredMethod("checkOp", cArg);
				mm = (Integer) method.invoke(mAppOpsm, Integer.valueOf(27),
						uid, name);
				// Log.e("main","mm:"+mm);

				// Log.e("main","uid:"+uid);
				// Log.e("main","name:"+name);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mm;

	}

}
