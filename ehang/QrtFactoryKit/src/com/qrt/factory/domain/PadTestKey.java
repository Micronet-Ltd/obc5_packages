package com.qrt.factory.domain;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:05:11 Time: 下午4:39
 * To change this template use File | Settings | File Templates.
 */
public class PadTestKey {

    private static final String KeyEventClassName = "android.view.KeyEvent";

    private String text;

    private boolean pass;

    private String name;

    private int keyCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        try {
            Class<?> aClass = Class.forName(KeyEventClassName);
            this.keyCode = aClass.getField(keyCode).getInt(aClass);
        } catch (Exception e) {
            this.keyCode = -1;
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }
}
