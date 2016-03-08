package com.ehang.sos;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PreMms_Mod extends Activity {

	Button mmsok, mmsno;
	EditText preMmsEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.premmsdlg_mod);

		mmsok = (Button) findViewById(R.id.mmsok);
		mmsno = (Button) findViewById(R.id.mmsno);
		preMmsEdit = (EditText) findViewById(R.id.preMmsEdit);
		mmsok.setOnClickListener(preMmsClickListener);
		mmsno.setOnClickListener(preMmsClickListener);

		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay();
		android.view.WindowManager.LayoutParams p = getWindow().getAttributes();
		p.height = (int) (d.getHeight() * 0.5);
		p.width = (int) (d.getWidth() * 0.9);
		getWindow().setAttributes(p);

		preMmsEdit.setText(Storage.getInstance().getoutStorage(this,
				"preMmsMsg"));
	}

	public OnClickListener preMmsClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.mmsok:
				Storage.getInstance().putinStorage(PreMms_Mod.this,
						"preMmsMsg", preMmsEdit.getText().toString());
				finish();
				break;
			case R.id.mmsno:
				finish();
				break;
			default:
				break;
			}
		}
	};

}
