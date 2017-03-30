package com.ehang.show;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;
import com.ehang.broacastreceiver.appUpdate;
import com.ehang.controlrecorder.R;
import com.ehang.entity.item;
import com.security.controlrecorder.Main;
import com.security.controlrecorder.PhoneBroadcastReceiver;
import com.security.db.UserDaoImpl;
import com.security.db.kaiguan;
import com.security.db.kaiguanzhi;
import com.security.db.shezhi;
import com.security.db.shujuku;
import android.graphics.Color;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.Loader.ForceLoadContentObserver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.SystemProperties;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ToggleButton;
import android.view.WindowManager;
import android.view.Display;
import android.view.KeyEvent;
import android.graphics.Color;

public class ShowActivity extends Activity {
	ListView listview;
	List<item> cc;
	Main dd;
	item ii;
	List<String> list;
	PackageInfo localPackageInfo;
	List<String> list2 = new ArrayList<String>();
	private appUpdate clipBoard;
	private static ArrayList localArrayList;
	private static ApplicationInfo localapplicationInfo;
	private Iterator localIterator;
	private PackageManager mPackageManager;
	private TextView ts;
	public static SharedPreferences sp;
	public ProgressDialog dialog;
	public String[] arrayOfString = null;
	boolean kaiguan = false;
	private boolean ss;
	private CheckBox choose;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);
		ts = (TextView) super.findViewById(R.id.ToggleButtonTitle);
		ts.setText(R.string.switch_close);
		dd = new Main(this);
		File file = getApplicationContext().getDatabasePath("xinren.db");
		ToggleButton bt = (ToggleButton) super.findViewById(R.id.bt);
		sp = this.getSharedPreferences("switch", Context.MODE_PRIVATE);
		ss = sp.getBoolean("open", false);
		bt.setChecked(ss);
		if (file.exists()) {
			kaiguan = ss;
			if (kaiguan) {

				// 閹跺tste閺勶拷1閸滐拷0閻ㄥ嫭娼堥梽鎰箻鐞涘瞼娴夋惔鏃傛畱閹垮秳缍�
				/*
				 * SystemProperties.set("persist.sys.switch_thief", "false");
				 * dd.dange(); dd.shi();
				 * SystemProperties.set("persist.sys.switch_thief", "true");
				 */
			}
			// 缁楊兛绔村▎陇绻橀弶锟�
		} else {
			// 缁楊兛绔村▎陇绻橀弶銉礉缂佹瑥绱戦崗瀹狀啎閸婏拷
			kaiguan = true;
			int state = 1;
			Object param[] = { state };
			kaiguanzhi sz = new kaiguanzhi();
			boolean flag = sz.addUser(param, ShowActivity.this);
			mPackageManager = getPackageManager();
			// 缂佹瑦鏆熼幑顔肩氨濞ｈ濮炴穱鈩冧紖
			List list4 = dd.getAppInfos();

			for (int i = 0; i < list4.size(); i++) {
				localapplicationInfo = (ApplicationInfo) list4.get(i);
				// 閸栧懎鎮�
				String pname = localapplicationInfo.packageName;
				String yname = null;

				try {
					yname = mPackageManager.getApplicationLabel(
							mPackageManager.getApplicationInfo(pname,
									PackageManager.GET_META_DATA)).toString();
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 閺嶈宓侀崠鍛倳瀵版鍩岄惄绋跨安閻ㄥ嫬绨查悽銊ユ倳
				int state1 = 0;
				Object param1[] = { state1, pname };
				shezhi sz1 = new shezhi();
				boolean flag1 = sz1.addUser(param1, ShowActivity.this);
			}
			// 缁楊兛绔村▎陇绻橀弶銉︽閿涘本濡哥敮锔芥箒瑜版洟鐓堕惃鍕閺堝绨查悽銊ф畱瑜版洟鐓堕弶鍐鐏炲繗鏂�閹猴拷
			SystemProperties.set("persist.sys.switch_thief", "false");
			dd.start();
			SystemProperties.set("persist.sys.switch_thief", "true");
		}

		// 瀵拷閸氼垯绔存稉顏勵嚠鐠囨繃顢嬮弰鍓с仛濮濓絽婀崝鐘烘祰閺佺増宓�
		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 鐠佸墽鐤嗘潻娑樺閺夛紕娈戣ぐ銏犵础娑撳搫娓捐ぐ銏ｆ祮閸斻劎娈戞潻娑樺閺夛拷
		dialog.setCancelable(true);// 鐠佸墽鐤嗛弰顖氭儊閸欘垯浜掗柅姘崇箖閻愮懓鍤瓸ack闁款喖褰囧☉锟�
		dialog.setCanceledOnTouchOutside(false);// 鐠佸墽鐤嗛崷銊у仯閸戠眹ialog婢舵牗妲搁崥锕�褰囧☉鍦杋alog鏉╂稑瀹抽弶锟�
		dialog.setMessage("濮濓絽婀崝鐘烘祰閺佺増宓侀敍宀冾嚞缁嬪秴锟斤拷...");
		// 閺勫墽銇氱�电鐦藉锟�
		dialog.show();
		cc = new ArrayList<item>();
		listview = (ListView) super.findViewById(R.id.listview);

		// 瀵よ櫣鐝汱istView閻ㄥ嫮鍋ｉ崙璁崇皑娴狅拷

		if (ss) {
			listview.setClickable(true);
			listview.setEnabled(true);
		} else {
			listview.setEnabled(false);
			listview.setClickable(false);
		}

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				choose = (CheckBox) arg1.findViewById(R.id.choose);
				String packagenamep = cc.get(arg2).getName0();
				List<ApplicationInfo> LocaList1 = getAppInfos();
				List<String> lname = new ArrayList<String>();
				for (int i = 0; i < LocaList1.size(); i++) {
					localapplicationInfo = (ApplicationInfo) localArrayList
							.get(i);
					String name = localapplicationInfo.packageName;
					lname.add(name);
				}

				if (!lname.contains(packagenamep)) {
					cc.get(arg2).setChoose(false);
					ImageView picture = (ImageView) arg1
							.findViewById(R.id.picture);
					TextView name = (TextView) arg1.findViewById(R.id.name);
					picture.setImageResource(R.drawable.ic_launcher2);
					name.setText(packagenamep);
					choose.setEnabled(false);
					arg1.setBackgroundColor(Color.GRAY);

					Toast.makeText(ShowActivity.this, R.string.unload,
							Toast.LENGTH_SHORT).show();

				}

				if (choose.isChecked()) {
					if (!lname.contains(packagenamep)) {
						choose.setEnabled(false);
					} else {
						// 选中
						choose.setChecked(false);
					}
					String packagename = cc.get(arg2).getName0();
					// 閺�鐟板綁閺佺増宓佹惔鎾茶厬閻拷 閻樿埖锟斤拷
					int state = 0;
					shezhi sz = new shezhi();
					Object param[] = { state, packagename };
					boolean flag = sz.updateUser(param, ShowActivity.this);
					// 婵″倹鐏夊锟介崗铏Ц閹垫挸绱戦惃锟�
					if (kaiguan) {
						SystemProperties.set("persist.sys.switch_thief",
								"false");
						dd.shi();
						SystemProperties
								.set("persist.sys.switch_thief", "true");
					}
				} else if (!choose.isChecked()) {
					// 闁鑵�
					if (!lname.contains(packagenamep)) {
						choose.setEnabled(false);
					} else {
						// 选中
						choose.setChecked(true);
					}

					// 闁鑵戦惃鍕樈閿涘矁顔�閻樿埖锟戒浇顔曠純顔昏礋1
					int state = 1;
					// 婵″倹鐏夊▽鈩冩箒闁鑵戦惃鍕樈閿涘矂鍋呮稊鍫㈠仯閸戣崵娈戦弮璺猴拷娆愬Ω鐎电懓绨查惃鍕瘶閸氬秵鏂侀崚浼存肠閸氬牅鑵戦敍宀�娴夎ぐ鎾茬艾閻ц棄鎮曢崡锟�

					// 閺嶈宓乸osition瀵版鍩岀�电懓绨查惃鍕安閻€劌鎮曠粔锟�,濮濄倕瀵橀崥宥嗘Ц闁鑵戦惃鍕瘶閸氬稄绱濈粋浣诡剾閺勵垯绗夐懗鍊熸崳娴ｆ粎鏁ら惃锟�
					String packagename = cc.get(arg2).getName0();
					list2.add(packagename);

					// 闁鑵戦敍灞芥皑閹跺﹦濮搁幀浣革拷鑹邦啎娑擄拷1
					shezhi sz = new shezhi();
					Object param[] = { state, packagename };
					boolean flag = sz.updateUser(param, ShowActivity.this);
					if (kaiguan) {
						SystemProperties.set("persist.sys.switch_thief",
								"false");
						dd.dange();
						SystemProperties
								.set("persist.sys.switch_thief", "true");
					}
				}
			}
		});

		// 閸掓稑缂撴稉锟芥稉顏呮箛閸旓拷
		class MyService extends Service {

			@Override
			public IBinder onBind(Intent intent) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void onCreate() {
				// TODO Auto-generated method stub
				super.onCreate();
				// 閸掓稑缂撻崥顖氬Зservice閻ㄥ埇ntent
				Intent intent = new Intent();
				intent.setAction("com.example.service");
			}
		}
		// Switch bt=(Switch)super.findViewById(R.id.bt);
		// 缂佹獨witch瀵拷閸忓疇顔曠純顔炬磧閸氾拷
		bt.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			// 閸掆晝鏁haredpreference娣囨繂鐡ㄥ锟介崗宕囨畱閻樿埖锟斤拷
			SharedPreferences sp = getSharedPreferences("switch",
					Context.MODE_PRIVATE);

			// 瀵拷閸忓磭濮搁幀浣规暭閸欐ɑ妞�
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// 瀵拷閸忓啿绱戦崥顖ょ礉楠炴湹绗栧▽鈩冩箒闁鑵�
				if (isChecked) {
					ts.setText(R.string.switch_open);
					SystemProperties.set("persist.sys.switch_thief", "false");
					// 瀵拷閸忓磭濮搁幀浣规Ц闁鑵戦惃锟�
					int state = 1;
					Object param[] = { state };
					kaiguanzhi sz = new kaiguanzhi();

					listview.setClickable(true);
					listview.setEnabled(true);
					boolean flag = sz.updateUser(param, ShowActivity.this);
					// 瀵拷閸忚櫕妲搁柅澶夎厬閻拷

					// Log.e("com.example.ShowActivity",
					// ""+SystemProperties.set("persist.sys.switch_thief",
					// "false"));
					celectischeck(true);
					kaiguan = true;
					// 闁絼绠為崗鍫熺叀娑擄拷娑撳鏆熼幑顔肩氨娑撶挮heckbox閺勵垰鎯侀柅澶夎厬閿涘苯顩ч弸婊堬拷澶夎厬閿涘矂鍋呮稊鍫濇皑閺勵垯淇婃禒锟�
					dd.dange();
					dd.shi();
					SystemProperties.set("persist.sys.switch_thief", "true");
				} else {

					listview.setClickable(false);
					listview.setEnabled(false);
					ts.setText(R.string.switch_close);
					SystemProperties.set("persist.sys.switch_thief", "false");
					// Log.e("com.example.ShowActivity",
					// ""+SystemProperties.set("persist.sys.switch_thief",
					// "false"));
					// boolean persist.sys.switch=false;

					// 瀵拷閸忓磭濮搁幀浣圭梾閺堝锟藉鑵�
					int state = 0;
					Object param[] = { state };
					kaiguanzhi sz = new kaiguanzhi();
					boolean flag = sz.updateUser(param, ShowActivity.this);
					celectischeck(false);
					kaiguan = false;
					dd.close();

				}
			}
		});

	}

	public void goBack(View v) {
		Builder b = new AlertDialog.Builder(ShowActivity.this,
				AlertDialog.THEME_HOLO_LIGHT);
		b.setTitle(R.string.tuichu);

		// 设置确定
		b.setPositiveButton(R.string.queding,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						ShowActivity.this.finish();
						// TODO Auto-generated method stub

					}
				});
		// 设置取消
		b.setNegativeButton(R.string.quxiao,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});

		b.setCancelable(false);
		b.show();

	}

	public void showDetails(View v1) {
		Builder b = new AlertDialog.Builder(ShowActivity.this,
				AlertDialog.THEME_HOLO_LIGHT);
		b.setTitle(R.string.help);
		b.setMessage(R.string.guanyu);
		Dialog dialog = b.create();

		dialog.show();

	}

	@Override
	protected void onResume() {
		cc.clear();
		// 璋冪敤寰楀埌甯︽湁鏉冮檺鐨勫簲鐢ㄧ殑鏂规硶
		List<ApplicationInfo> LocaList1 = getAppInfos();
		for (int i = 0; i < LocaList1.size(); i++) {
			ii = new item();
			localapplicationInfo = (ApplicationInfo) localArrayList.get(i);
			// 寰楀埌搴旂敤鍚嶇О锛屾斁鍦ㄦ暟缁勯噷杈�
			String name = localapplicationInfo.packageName;
			// 鏍规嵁鍖呭悕鑾峰緱鐩稿簲鐨刟pp鍚�
			String appname = null;
			int leng = 0;
			try {
				PackageInfo packinfo = mPackageManager.getPackageInfo(name,
						PackageManager.GET_PERMISSIONS);
				String quanxian[] = null;
				quanxian = packinfo.requestedPermissions;
				leng = quanxian.length;
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				appname = mPackageManager.getApplicationLabel(
						mPackageManager.getApplicationInfo(name,
								PackageManager.GET_META_DATA)).toString();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 寰楀埌鍏剁浉搴旂殑鍥炬爣
			Drawable picture = localapplicationInfo.loadIcon(mPackageManager);
			// 寰楀埌鍏剁殑uid
			int uid = localapplicationInfo.uid;
			// 杩囨护鎺夌郴缁熺殑搴旂敤
			if ((localapplicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {

			} else {
				// 闈炵郴缁熷簲鐢�
				// 鍖呭悕
				ii.setName0(name);
				// 搴旂敤鍚嶇О
				ii.setName(appname);
				// 鍥剧墖
				ii.setPicture(picture);
				// 鏉冮檺闀垮害
				ii.setleng(leng);
				cc.add(ii);
			}
		}
		listview.setAdapter(new base());
		// 鏁版嵁鍔犺浇瀹屾瘯锛屽彇娑堝璇濇
		dialog.dismiss();
		super.onResume();
	}

	private void celectischeck(boolean ss) {

		Editor edit = sp.edit();
		edit.putBoolean("open", ss);
		edit.commit();
		// System.out.println("濞夈劌鍞藉▽鈩冩箒" + ss);

	}

	// 濮濄倖鏌熷▔鏇炲讲娴犮儱鐤勯悳鐗堝瘻鏉╂柨娲栭柨顔款唨鎼存梻鏁ら張锟界亸蹇撳閼板奔绗夐弰顖烇拷锟介崙锟�
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// moveTaskToBack(false);
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }
	// 閸掓稑缂撴稉锟芥稉顏勫岸閸氬秴鍞撮柈銊ц閿涳拷
	class base extends BaseAdapter {
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return cc.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		// 瀵邦亞骞嗗妤�鍩屾穱鈩冧紖
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inf = LayoutInflater.from(ShowActivity.this);
			// 閹跺﹤鐡欑敮鍐ㄧ湰鏉烆剚宕查幋鎰娑撶崁iew鐎电钖�
			View view = inf.inflate(R.layout.list_all, null);
			TextView name = (TextView) view.findViewById(R.id.name);
			TextView name0 = (TextView) view.findViewById(R.id.yincang);
			name0.setText(cc.get(position).getName0());
			name0.setVisibility(convertView.GONE);
			TextView tishi = (TextView) view.findViewById(R.id.tishi);
			CheckBox choose = (CheckBox) view.findViewById(R.id.choose);
			ImageView picture = (ImageView) view.findViewById(R.id.picture);

			TextView uid = (TextView) view.findViewById(R.id.uid);
			name.setText(cc.get(position).getName());
			// tishi.setText(@string/quan);
			picture.setImageDrawable(cc.get(position).getPicture());
			uid.setText(cc.get(position).getleng() + "");
			shezhi sz = new shezhi();
			List<Map<String, Object>> oo = sz.list(ShowActivity.this);

			// System.out.println("sadasdasdas" + oo);
			for (Map<String, Object> m : oo) {
				for (String k : m.keySet()) {
					String name2 = (String) m.get(k);
					// System.out.println("瀹曗晜绨濇禍锟�" + name2);
					if (name2.equals(cc.get(position).getName0())) {
						// System.out.println("閼冲�熺箻閺夈儻绱�");
						// 娴犲孩鏆熼幑顔肩氨娑擃厽鐓＄拠銏犲毉閺夈儲妲搁崥锕傦拷澶夎厬
						choose.setChecked(true);
						// 閸愬秴鍨介弬顓濈濞嗏�崇磻閸忓磭濮搁幀锟�,婵″倹鐏夐柅澶夎厬閻樿埖锟戒緤绱濋獮鏈电瑬瀵拷閸忓磭濮搁幀浣风瘍鐟曚焦妲稿锟介惃鍕剰閸愬吀绗呴幍宥堟崳娴ｆ粎鏁�
						// kaiguanzhi kk=new kaiguanzhi();
						// String param[]={"1"};
						// int aa= kk.cc(param, ShowActivity.this);
						// Log.e("瀵拷閸忚櫕鏆熼幑顔肩氨娑擄拷", ""+aa);
						// 鐠囧瓨妲戝锟介崗铏Ц閹垫挸绱戦惃锟�
						if (kaiguan) {
							SystemProperties.set("persist.sys.switch_thief",
									"false");
							dd.dange();
							SystemProperties.set("persist.sys.switch_thief",
									"true");
						} else {
							SystemProperties.set("persist.sys.switch_thief",
									"false");
							dd.shi();
						}

					}
				}
			}
			return view;
		}
	}

	static class ViewHolder {
		ImageView iv_icon;
		TextView tv_packname;
		TextView iv_id;
		CheckBox choose;
	}

	public List<ApplicationInfo> getAppInfos() {
		localArrayList = new ArrayList();
		// 閼惧嘲褰嘝ackageManager鐎电钖勯敍灞界暊娑撴槒顩﹂弰顖濆箯瀵版鍑＄�瑰顥婄粙瀣碍閻ㄥ嫪淇婇幁锟�
		mPackageManager = getPackageManager();
		// 瀵版鍩屽鎻掔暔鐟佸懐娈戠敮锔芥箒閺夊啴妾洪惃鍕安閻€劎鈻兼惔锟�
		localIterator = mPackageManager.getInstalledPackages(
				PackageManager.GET_PERMISSIONS).iterator();
		// 婵″倹鐏夊▽鈩冩箒鐢附婀佹惔鏃傛暏閺夊啴妾洪惃鍕安閻㈩煉绱濋崚娆掔箲閸ョ偟鈹�
		if (!localIterator.hasNext())
			return null;
		while (localIterator.hasNext()) {
			// 婵″倹鐏夐張澶嬵劃鎼存梻鏁ら惃鍕樈閿涘苯鍨幎濠傛倗娑擃亜绨查悽銊ュ帗缁辩姴褰囬崙鐑樻降
			localPackageInfo = (PackageInfo) localIterator.next();
			// System.out.println("瀵版鍩岄惃鍕倗娑擃亜绨查悽銊︽Ц閿涳拷" + localPackageInfo);
			arrayOfString = localPackageInfo.requestedPermissions;
			// System.out.println("瀵版鍩岀拠銉ョ安閻€劎娈戦惃鍕綀闂勬劖妲搁敍锟�" +
			// Arrays.toString(arrayOfString));
			// 婵″倹鐏夋惔鏃傛暏閺堝娼堥梽鎰畱鐠囨繐绱濋崘宥嗗⒔鐞涘奔绗呮潏鍦畱閹垮秳缍�
			if (arrayOfString != null) {
				int leng = arrayOfString.length;
				// System.out.println("閺夊啴妾洪惃鍕毐鎼达附妲搁敍锟�" + leng);
				for (int j = 0; j < leng; ++j) {
					// 閺佹壆绮嶆稉顓犳畱閸忓啰绀屽▽鈩冩箒閸滃苯缍嶉棅铏綀闂勬劕灏柊宥囨畱閿涘矂鍋呮稊鍫濇皑缂佈呯敾瀵邦亞骞�
					if (!arrayOfString[j]
							.equals("android.permission.RECORD_AUDIO"))
						// 閸︺劍婀板▎鈥虫儕閻滎垯鑵戠紒褏鐢诲顏嗗箚閿涘矁锟藉奔绗夐幍褑顢戝顏嗗箚閸氬海娈戦柈銊ュ瀻
						continue;
					// 閹跺﹤鐢張澶婄秿闂婅櫕娼堥梽鎰畱鎼存梻鏁ら弨鎯ф躬localArrayList闁插矁绔�
					if ((localPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {

					} else {
						localArrayList.add(localPackageInfo.applicationInfo);
					}

				}
			}
		}
		return localArrayList;
	}
/* 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Builder b = new AlertDialog.Builder(ShowActivity.this,
					AlertDialog.THEME_HOLO_LIGHT);
			b.setTitle(R.string.tuichu);

			// 设置确定
			b.setPositiveButton(R.string.queding,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							ShowActivity.this.finish();
							// TODO Auto-generated method stub

						}
					});
			// 设置取消
			b.setNegativeButton(R.string.quxiao,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

						}
					});

			b.setCancelable(false);
			b.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	*/
}
