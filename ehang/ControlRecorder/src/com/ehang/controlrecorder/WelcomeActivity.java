package com.ehang.controlrecorder;

import java.util.Timer;
import java.util.TimerTask;

import com.ehang.show.ShowActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class WelcomeActivity extends Activity {
	private TextView mTxtName;
	private ImageView mImgLogo;
	private TextView load;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		// 创建时调用initview,即初始化控件
		initViews();
		// 倒计时3秒之后，执行task中的任务
		myTimeTask task = new myTimeTask();
		Timer time = new Timer();
		// 设置时间为3秒
		time.schedule(task, 2 * 1000);
	}

	// 初始化UI各个控件
	private void initViews() {
		mTxtName = (TextView) findViewById(R.id.text_name);
		mImgLogo = (ImageView) findViewById(R.id.img_logo);
		load = (TextView) findViewById(R.id.load);
	}

	// 创建myTimeTask(定时任务)
	private class myTimeTask extends TimerTask {

		@Override
		public void run() {
			// 调用跳到主页的方法
			startMainPage();
			// 当欢迎页面执行3秒结束
			WelcomeActivity.this.finish();

		}

	}

	// 打开主页的方法
	private void startMainPage() {
		Intent i = new Intent();
		i.setClass(this, ShowActivity.class);
		WelcomeActivity.this.startActivity(i);

	}
}
