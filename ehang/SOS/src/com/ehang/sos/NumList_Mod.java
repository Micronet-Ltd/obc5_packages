package com.ehang.sos;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;

public class NumList_Mod extends Activity {
	Button ok, no;
	ImageButton btn1, btn2, btn3, btn4, btn5;
	EditText num1, num2, num3, num4, num5;
	Intent intent;
	static String num1value, num2value, num3value, num4value, num5value;
	static String num1string, num2string, num3string, num4string, num5string;

	int displayNameColumn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.numlist_mod);

		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay();
		android.view.WindowManager.LayoutParams p = getWindow().getAttributes();
		p.height = (int) (d.getHeight() * 0.55);
		p.width = (int) (d.getWidth() * 0.9);
		getWindow().setAttributes(p);

		btn1 = (ImageButton) findViewById(R.id.btn1);
		btn2 = (ImageButton) findViewById(R.id.btn2);
		btn3 = (ImageButton) findViewById(R.id.btn3);
		btn4 = (ImageButton) findViewById(R.id.btn4);
		btn5 = (ImageButton) findViewById(R.id.btn5);
		num1 = (EditText) findViewById(R.id.num1);
		num2 = (EditText) findViewById(R.id.num2);
		num3 = (EditText) findViewById(R.id.num3);
		num4 = (EditText) findViewById(R.id.num4);
		num5 = (EditText) findViewById(R.id.num5);
		ok = (Button) findViewById(R.id.ok);
		no = (Button) findViewById(R.id.no);

		btn1.setOnClickListener(contactsClickListener);
		btn2.setOnClickListener(contactsClickListener);
		btn3.setOnClickListener(contactsClickListener);
		btn4.setOnClickListener(contactsClickListener);
		btn5.setOnClickListener(contactsClickListener);
		ok.setOnClickListener(contactsClickListener);
		no.setOnClickListener(contactsClickListener);
		
		if (Storage.getInstance().getoutStorage(this, "num1Name") != "") {
			textShow(num1,Storage.getInstance().getoutStorage(this, "num1Name"));
		} else
			num1.setText(Storage.getInstance().getoutStorage(this, "num1"));
		if (Storage.getInstance().getoutStorage(this, "num2Name") != "") {
			textShow(num2,Storage.getInstance().getoutStorage(this, "num2Name"));
		} else
			num2.setText(Storage.getInstance().getoutStorage(this, "num2"));
		if (Storage.getInstance().getoutStorage(this, "num3Name") != "") {
			textShow(num3,Storage.getInstance().getoutStorage(this, "num3Name"));
		} else
			num3.setText(Storage.getInstance().getoutStorage(this, "num3"));
		if (Storage.getInstance().getoutStorage(this, "num4Name") != "") {
			textShow(num4,Storage.getInstance().getoutStorage(this, "num4Name"));
		} else
			num4.setText(Storage.getInstance().getoutStorage(this, "num4"));
		if (Storage.getInstance().getoutStorage(this, "num5Name") != "") {
			textShow(num5,Storage.getInstance().getoutStorage(this, "num5Name"));
		} else
			num5.setText(Storage.getInstance().getoutStorage(this, "num5"));
	}

	public OnClickListener contactsClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);//ContactsContract.Contacts.CONTENT_URI);
			switch (v.getId()) {
				case R.id.btn1:
					startActivityForResult(intent, 1);
					break;
				case R.id.btn2:
					startActivityForResult(intent, 2);
					break;
				case R.id.btn3:
					startActivityForResult(intent, 3);
					break;
				case R.id.btn4:
					startActivityForResult(intent, 4);
					break;
				case R.id.btn5:
					startActivityForResult(intent, 5);
					break;
				case R.id.ok:
					// if manual change the numlist,storage numbers(or "") and set name empty("")
					if (judge(num1, "num1Name")) {// availuable number changed or empty
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num1", num1.getText().toString());
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num1Name", "");
					} else if (!num1.getText().toString().equals(Storage.getInstance().getoutStorage(
									NumList_Mod.this, "num1Name"))) {
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num1", Storage.getInstance().getoutStorage(NumList_Mod.this,
										"num1value"));
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num1Name", Storage.getInstance().getoutStorage(NumList_Mod.this,
										"num1string"));
					}
					if (judge(num2, "num2Name")) {
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num2", num2.getText().toString());
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num2Name", "");
					} else if (!num2.getText().toString().equals(Storage.getInstance().getoutStorage(
							NumList_Mod.this, "num2Name"))) {
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num2", Storage.getInstance().getoutStorage(NumList_Mod.this,
										"num2value"));
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num2Name", Storage.getInstance().getoutStorage(NumList_Mod.this,
										"num2string"));
					}
					if (judge(num3, "num3Name")) {
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num3", num3.getText().toString());
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num3Name", "");
					} else if (!num3.getText().toString().equals(Storage.getInstance().getoutStorage(
							NumList_Mod.this, "num3Name"))) {
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num3", Storage.getInstance().getoutStorage(NumList_Mod.this,
										"num3value"));
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num3Name", Storage.getInstance().getoutStorage(NumList_Mod.this,
										"num3string"));
					}
					if (judge(num4, "num4Name")) {
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num4", num4.getText().toString());
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num4Name", "");
					} else if (!num4.getText().toString().equals(Storage.getInstance().getoutStorage(
							NumList_Mod.this, "num4Name"))) {
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num4", Storage.getInstance().getoutStorage(NumList_Mod.this,
										"num4value"));
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num4Name", Storage.getInstance().getoutStorage(NumList_Mod.this,
										"num4string"));
					}
					if (judge(num5, "num5Name")) {
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num5", num5.getText().toString());
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num5Name", "");
					} else if (!num5.getText().toString().equals(Storage.getInstance().getoutStorage(
							NumList_Mod.this, "num5Name"))) {
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num5", Storage.getInstance().getoutStorage(NumList_Mod.this,
										"num5value"));
						Storage.getInstance().putinStorage(NumList_Mod.this,
								"num5Name", Storage.getInstance().getoutStorage(NumList_Mod.this,
										"num5string"));
					}
					finish();
					break;
				case R.id.no:
					finish();
				default:
					break;
			}

		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null) {
			Uri contactData = data.getData();
			Cursor cursor = managedQuery(contactData, null, null, null, null);
			cursor.moveToFirst();
			String Name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			String num = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			String showStr = Name + "(" + num + ")" ;
			Log.d("SOS","Name= " +Name + "  num= " + num);

			switch (requestCode) {
			case 1:
				if (resultCode == RESULT_OK) {
					if (showStr.equals(num2.getText().toString())) {
						alertMsg(R.string.dif2);
					} else if (showStr.equals(num3.getText().toString())) {
						alertMsg(R.string.dif3);
					} else if (showStr.equals(num4.getText().toString())) {
						alertMsg(R.string.dif4);
					} else if (showStr.equals(num5.getText().toString())) {
						alertMsg(R.string.dif5);
					} else {
						textShow(num1, showStr);
						num1value = num;
						num1string = num1.getText().toString();
						 Storage.getInstance().putinStorage(NumList_Mod.this,"num1value",num1value);
						 Storage.getInstance().putinStorage(NumList_Mod.this,"num1string",num1string);
					}
				}
				break;
			case 2:
				if (resultCode == RESULT_OK) {
					if (showStr.equals(num1.getText().toString())) {
						alertMsg(R.string.dif1);
					} else if (showStr.equals(num3.getText().toString())) {
						alertMsg(R.string.dif3);
					} else if (showStr.equals(num4.getText().toString())) {
						alertMsg(R.string.dif4);
					} else if (showStr.equals(num5.getText().toString())) {
						alertMsg(R.string.dif5);
					} else {
						textShow(num2, showStr);
						num2value = num;
						num2string = num2.getText().toString();
						 Storage.getInstance().putinStorage(NumList_Mod.this,"num2value",num2value);
						 Storage.getInstance().putinStorage(NumList_Mod.this,"num2string",num2string);
					}
				}
				break;
			case 3:
				if (resultCode == RESULT_OK) {
					if (showStr.equals(num1.getText().toString())) {
						alertMsg(R.string.dif1);
					} else if (showStr.equals(num2.getText().toString())) {
						alertMsg(R.string.dif2);
					} else if (showStr.equals(num4.getText().toString())) {
						alertMsg(R.string.dif4);
					} else if (showStr.equals(num5.getText().toString())) {
						alertMsg(R.string.dif5);
					} else {
						textShow(num3, showStr);
						num3value = num;
						num3string = num3.getText().toString();
						 Storage.getInstance().putinStorage(NumList_Mod.this,"num3value",num3value);
						 Storage.getInstance().putinStorage(NumList_Mod.this,"num3string",num3string);
					}
				}
				break;
			case 4:
				if (resultCode == RESULT_OK) {
					if (showStr.equals(num1.getText().toString())) {
						alertMsg(R.string.dif1);
					} else if (showStr.equals(num2.getText().toString())) {
						alertMsg(R.string.dif2);
					} else if (showStr.equals(num3.getText().toString())) {
						alertMsg(R.string.dif3);
					} else if (showStr.equals(num5.getText().toString())) {
						alertMsg(R.string.dif5);
					} else {
						textShow(num4, showStr);
						num4value = num;
						num4string = num4.getText().toString();
						 Storage.getInstance().putinStorage(NumList_Mod.this,"num4value",num4value);
						 Storage.getInstance().putinStorage(NumList_Mod.this,"num4string",num4string);
					}
				}
				break;
			case 5:
				if (resultCode == RESULT_OK) {
					if (showStr.equals(num1.getText().toString())) {
						alertMsg(R.string.dif1);
					} else if (showStr.equals(num2.getText().toString())) {
						alertMsg(R.string.dif2);
					} else if (showStr.equals(num3.getText().toString())) {
						alertMsg(R.string.dif3);
					} else if (showStr.equals(num4.getText().toString())) {
						alertMsg(R.string.dif4);
					} else {
						textShow(num5, showStr);
						num5value = num;
						num5string = num5.getText().toString();
						 Storage.getInstance().putinStorage(NumList_Mod.this,"num5value",num5value);
						 Storage.getInstance().putinStorage(NumList_Mod.this,"num5string",num5string);
					}
				}
				break;
			default:
				break;
			}
		} else {
			// do nothing
		}
	}

	private Boolean isNum(String txt) {
		Pattern p = Pattern.compile("[0-9]*");//some numbers or no string
		Matcher m = p.matcher(txt);
		if (m.matches()) {
			return true;
		}
		return false;
	}

	private Boolean judge(EditText num, String str) {
		if (isNum(num.getText().toString())
				&& !num.getText()
						.toString()
						.equals(Storage.getInstance().getoutStorage(
								NumList_Mod.this, str))
				|| num.getText().toString().equals("")) {
			return true;
		} else
			return false;
	}

	private void alertMsg(int messageId) {
		new AlertDialog.Builder(this).setTitle(R.string.notice)
				.setMessage(messageId).setPositiveButton(R.string.repick, null)
				.show();
	}

	public void textShow(EditText edt,String message) {
		Spannable span = new SpannableString(message);
	    span.setSpan(new RelativeSizeSpan(0.8f), message.indexOf("("), message.indexOf(")")+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    span.setSpan(new ForegroundColorSpan(R.color.sumFont), message.indexOf("("), message.indexOf(")")+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    edt.setText(span);
	}

	
    
}
