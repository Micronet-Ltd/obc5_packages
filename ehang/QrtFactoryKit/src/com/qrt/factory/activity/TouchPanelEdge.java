/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;

public class TouchPanelEdge extends AbstractActivity {

    private static final String TAG = "TouchPanelEdge Test";

    private ArrayList<EdgePoint> mArrayList;

    private int pointRadius = 40;

    private int hightPix = 960, widthPix = 0;

    private float w = 0, h = 0;

    // If points is too more, it will be hard to touch edge points.
    private final int MAX_POINTS = 10;

    private class EdgePoint {

        int x;

        int y;

        boolean isChecked = false;

        public EdgePoint(int x, int y, boolean isCheck) {

            this.x = x;
            this.y = y;
            this.isChecked = isCheck;
        }
    }

    public ArrayList<EdgePoint> getTestPoint() {

        ArrayList<EdgePoint> list = new ArrayList<EdgePoint>();

        for (int w = pointRadius - 1; w < widthPix; w += pointRadius * 2) {
            for (int h = pointRadius - 1; h < hightPix; h += pointRadius * 2) {

                if (w == pointRadius - 1) {
                    list.add(new EdgePoint(pointRadius, h, false));
                    continue;
                }

                if (w == widthPix - pointRadius - 1) {
                    list.add(new EdgePoint(widthPix - pointRadius, h, false));
                    continue;
                }

                if (h == pointRadius - 1) {
                    list.add(new EdgePoint(w, pointRadius, false));
                    continue;
                }

//                if (h == hightPix - pointRadius - 1) {
//                    list.add(new EdgePoint(w, hightPix - pointRadius, false));
//                    continue;
//                }
            }
        }
        list.add(new EdgePoint(widthPix / 2, hightPix / 2, false));

        return list;
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // get panel size
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        hightPix = mDisplayMetrics.heightPixels;
        widthPix = mDisplayMetrics.widthPixels;
        // It must be common divisor of width and hight
        pointRadius = getRadius(hightPix, widthPix);
        logd(hightPix + " " + widthPix);// 800x480 or 480x320

        setContentView(new Panel(this));
    }

    int getRadius(int hightPix, int widthPix) {

        int h = hightPix / 2;
        int w = widthPix / 2;
        if (w > h)// landscape mode
        {
            int t;
            t = w;
            w = h;
            h = t;
        }
        int radius = -1;
        int minRadius = w / MAX_POINTS;

        for (int i = minRadius; i < w; i++) {
            if (h % i == 0 && w % i == 0) {
                return i;
            }
        }
        return radius;
    }

    private class Panel extends View {

        public static final int TOUCH_TRACE_NUM = 30;

        public static final int PRESSURE = 500;

        private TouchData[] mTouchData = new TouchData[TOUCH_TRACE_NUM];

        private int traceCounter = 0;

        private Paint mPaint = new Paint();

        public class TouchData {

            public float x;

            public float y;

            public float r;
        }

        public Panel(Context context) {

            super(context);
            mArrayList = getTestPoint();
            mPaint.setARGB(100, 100, 100, 100);
            for (int i = 0; i < TOUCH_TRACE_NUM; i++) {
                mTouchData[i] = new TouchData();
            }
        }

        private int getNext(int c) {

            int temp = c + 1;
            return temp < TOUCH_TRACE_NUM ? temp : 0;
        }

        public void onDraw(Canvas canvas) {

            super.onDraw(canvas);
            mPaint.setColor(Color.LTGRAY);
            mPaint.setTextSize(20);
            canvas.drawText("W: " + w, widthPix / 2 - 20, hightPix / 4 - 10,
                    mPaint);
            canvas.drawText("H: " + h, widthPix / 2 - 20, hightPix / 4 + 10,
                    mPaint);

            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(pointRadius);
            for (int i = 0; i < mArrayList.size(); i++) {
                EdgePoint point = mArrayList.get(i);
                mPaint.setColor(Color.RED);
                canvas.drawCircle(point.x, point.y, mPaint.getStrokeWidth(),
                        mPaint);
            }

            for (int i = 0; i < TOUCH_TRACE_NUM; i++) {
                TouchData td = mTouchData[i];
                mPaint.setColor(Color.BLUE);
                if (td.r > 0) {
                    canvas.drawCircle(td.x, td.y, 2, mPaint);
                }
            }
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            final int eventAction = event.getAction();

            w = event.getRawX();
            h = event.getRawY();
            if ((eventAction == MotionEvent.ACTION_MOVE) || (eventAction
                    == MotionEvent.ACTION_UP)) {
                for (int i = 0; i < mArrayList.size(); i++) {
                    EdgePoint point = mArrayList.get(i);
                    if (!point.isChecked
                            && ((w >= (point.x - pointRadius)) && (w <= (point.x
                            + pointRadius)))
                            && ((h >= (point.y - pointRadius)) && (h <= (point.y
                            + pointRadius)))) {
                        mArrayList.remove(i);
                        break;
                    }
                }

                if (mArrayList.isEmpty()) {
                    pass();
                }

                TouchData tData = mTouchData[traceCounter];
                tData.x = event.getX();
                tData.y = event.getY();
                tData.r = event.getPressure() * PRESSURE;
                traceCounter = getNext(traceCounter);
                invalidate();
            }
            return true;
        }
    }
}
