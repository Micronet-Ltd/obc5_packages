package com.ehang.dbutil;

import android.graphics.drawable.Drawable;
import android.widget.CheckBox;

public class listviewItemEntity {

	private String apkname;
	private String packagename;
	private Drawable picture;
	private String versionnumber;
	private int enable;

	public String getApkname() {
		return apkname;
	}

	public void setApkname(String apkname) {
		this.apkname = apkname;
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	public Drawable getPicture() {
		return picture;
	}

	public void setPicture(Drawable picture) {
		this.picture = picture;
	}

	public void setVersionnumber(String versionnumber) {
		this.versionnumber = versionnumber;
	}

	public String getVersionnumber() {
		return versionnumber;
	}
	
    public void setEnable(int enable){
		this.enable = enable;
	}
	public int getEnable(){
		return enable;
	}

}
