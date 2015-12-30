package com.qrt.factory.domain;

/**
 * Created with IntelliJ IDEA. Owner: wangwenlong Date: 2012:09:26 Time: 22:26
 * To change this template use File | Settings | File Templates.
 */
public class AutoTestItem extends TestItem {

    private String autoTestClassName;

    public String getAutoTestClassName() {
        return autoTestClassName;
    }

    public void setAutoTestClassName(String autoTestClassName) {
        this.autoTestClassName = autoTestClassName;
    }
}
