package com.ehangtoolkit;

import com.ehangtoolkit.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;


public class AboutView extends Activity {
	
	private ImageButton btnClose;
	private TextView textCopyright;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//Delete titlebar
		setContentView(R.layout.about_activity);
		
		textCopyright = (TextView) findViewById(R.id.copyright);
		textCopyright.setText(getString(R.string.about_ehang) + "\n" + getString(R.string.copyright_ehang));
		
		btnClose = (ImageButton) findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	
	
}