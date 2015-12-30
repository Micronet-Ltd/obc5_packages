package com.qrt.factory.domain;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:04:24 Time: 下午5:22
 * To change this template use File | Settings | File Templates.
 */
public class TestKey {

    private static final String KeyEventClassName = "android.view.KeyEvent";

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
}
