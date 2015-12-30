package com.qrt.factory.domain;

import android.content.Intent;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:23 Time: 17:49 To
 * change this template use File | Settings | File Templates.
 */
public class TestItem {

    private String title;

    private Intent intent;

    private Boolean pass = null;

    private StringBuffer result = new StringBuffer();

    private String name;
    private String time;    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 */

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Boolean getPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = Boolean.valueOf(pass);
    }

    public void clearPass() {
        this.pass = null;
    }

    public String getResult() {
        return result.toString();
    }

    public void clearResult() {
        this.result = new StringBuffer();
    }

    public void addResult(String result) {
        this.result.append(result);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 begin*/
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 end*/
}
