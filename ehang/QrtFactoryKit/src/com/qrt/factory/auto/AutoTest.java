package com.qrt.factory.auto;

import android.content.Context;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:10 Time: 15:39 To
 * change this template use File | Settings | File Templates.
 */
public interface AutoTest {

    void initialize(Context context);

    TestResult doingBackground() throws InterruptedException;
}
