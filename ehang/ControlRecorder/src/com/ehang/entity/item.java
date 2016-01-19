package com.ehang.entity;

import android.graphics.drawable.Drawable;
import android.widget.CheckBox;

public class item {

	private String name0;
	private String name;
	private Drawable picture;
	private int leng;
	private boolean choose;

	public boolean isChoose() {
		return choose;
	}

	public void setChoose(boolean choose) {
		this.choose = choose;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName0() {
		return name0;
	}

	public void setName0(String name0) {
		this.name0 = name0;
	}

	public Drawable getPicture() {
		return picture;
	}

	public void setPicture(Drawable picture) {
		this.picture = picture;
	}

	public int getleng() {
		return leng;
	}

	public void setleng(int leng) {
		this.leng = leng;
	}

}
