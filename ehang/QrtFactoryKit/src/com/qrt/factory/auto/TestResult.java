package com.qrt.factory.auto;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:10 Time: 15:38 To
 * change this template use File | Settings | File Templates.
 */
public class TestResult {

    private boolean pass = false;

    private StringBuffer resultBuffer = new StringBuffer();

    private String title;

    private String time;    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 */
    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public String getResult() {
        return resultBuffer.toString();
    }

    public void appendResult(String result) {
        this.resultBuffer.append(result);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
