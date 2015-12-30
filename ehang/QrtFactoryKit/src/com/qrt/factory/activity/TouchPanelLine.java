package com.qrt.factory.activity;

import com.qrt.factory.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. Owner: wangwenlong Date: 2012:10:20 Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class TouchPanelLine extends AbstractActivity {

    private static final String TAG = "TouchPanel Test";

    private List<TouchPath> mSidelineTouchPathList;

    private List<TouchPath> mDiagonalTouchPathList;

    private List<TouchPath> mTransverseTouchPathList;

    private List<float[]> mPointerList;

    private Bitmap mBitmap;
    private DisplayMetrics mDisplayMetrics;

    protected String getTag() {
        return TAG;
    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int hightPix = mDisplayMetrics.heightPixels;
        int widthPix = mDisplayMetrics.widthPixels;

        logd(hightPix + " " + widthPix);// 800x480 or 480x320

        createTestPath(widthPix, hightPix);

        initBackgroundBitmap(hightPix, widthPix);

        Panel view = new Panel(this);
        setContentView(view);
        /*Add by wangwenlong for hidn system ui when full screen (8916) HQ00000000 2014-07-11*/
        //view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED);
    }

    private void initBackgroundBitmap(int hightPix, int widthPix) {
        mBitmap = Bitmap.createBitmap(widthPix, hightPix,
                Bitmap.Config.RGB_565);
    }

    private void createTestPath(int widthPix, int hightPix) {

        int touchTestType = 0;
        float variableLength = 100;
        if ("1".equals(SystemProperties.get("ro.ftmtestmode"))) {
            touchTestType = getResources().getInteger(R.integer.default_tp_for_pcba);
            variableLength = getResources().getInteger(R.integer.default_tp_width_for_pcba);
        } else {
            touchTestType = getResources().getInteger(R.integer.default_tp);
            variableLength = getResources().getInteger(R.integer.default_tp_width);
        }

        mSidelineTouchPathList = new ArrayList<TouchPath>();
        mDiagonalTouchPathList = new ArrayList<TouchPath>();
		mTransverseTouchPathList = new ArrayList<TouchPath>();
		
        if (touchTestType < 3) {
            final float x0 = 0, x1 = variableLength,
                    x2 = widthPix- variableLength, x3 = widthPix;

            final float y0 = 0, y1 = variableLength,
                    y2 = hightPix- variableLength, y3 = hightPix;

            if (touchTestType == 2) {
                mSidelineTouchPathList.add(new TouchPath(new RectF(x0, y0, x1, y1),
                        createPath(
                                new float[]{x0, y1},
                                new float[]{x1, y1},
                                new float[]{x1, y2},
                                new float[]{x0, y2}),
                        new RectF(x0, y2, x1, y3)));

                mSidelineTouchPathList.add(new TouchPath(new RectF(x0, y2, x1, y3),
                        createPath(
                                new float[]{x1, y3},
                                new float[]{x1, y2},
                                new float[]{x2, y2},
                                new float[]{x2, y3}),
                        new RectF(x2, y2, x3, y3)));

                mSidelineTouchPathList.add(new TouchPath(new RectF(x2, y2, x3, y3),
                        createPath(
                                new float[]{x3, y2},
                                new float[]{x2, y2},
                                new float[]{x2, y1},
                                new float[]{x3, y1}),
                        new RectF(x2, y0, x3, y1)));

                mSidelineTouchPathList.add(new TouchPath(new RectF(x2, y0, x3, y1),
                        createPath(
                                new float[]{x2, y0},
                                new float[]{x2, y1},
                                new float[]{x1, y1},
                                new float[]{x1, y0}),
                        new RectF(x0, y0, x1, y1)));
            }

            if (touchTestType == 1 || touchTestType == 2) {
                mDiagonalTouchPathList.add(new TouchPath(new RectF(x0, y0, x1, y1),
                        createPath(
                                new float[]{x0, y1},
                                new float[]{x1, y0},
                                new float[]{x3, y2},
                                new float[]{x2, y3}),
                        new RectF(x2, y2, x3, y3)));
            }

            mDiagonalTouchPathList.add(new TouchPath(new RectF(x2, y0, x3, y1),
                    createPath(
                            new float[]{x2, y0},
                            new float[]{x3, y1},
                            new float[]{x1, y3},
                            new float[]{x0, y2}),
                    new RectF(x0, y2, x1, y3)));
        }

        //Add By Wangwenlong to add TP test issue (8x26) HQ00000000 2013-09-29 Begin
        if (touchTestType == 3) {
            final float xPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1.5f, mDisplayMetrics);
            final float x0 = xPx, x1 = variableLength,
                    x2 = widthPix- variableLength, x3 = widthPix - xPx;

            final float yPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 2f, mDisplayMetrics);
            final float y0 = yPx, y1 = variableLength,
                    y2 = hightPix/2 - variableLength, y3 = hightPix/2 - yPx,
                    y4 = hightPix/2 + yPx, y5 = hightPix/2 + variableLength, y6 = hightPix - variableLength,
                    y7 = hightPix - yPx;

            mSidelineTouchPathList.add(new TouchPath(new RectF(x0, y0, x1, y1),
                    createPath(
                            new float[]{x1, y0},
                            new float[]{x1, y1},
                            new float[]{x2, y1},
                            new float[]{x2, y0}),
                    new RectF(x2, y0, x3, y1)));
            mSidelineTouchPathList.add(new TouchPath(new RectF(x0, y2, x1, y3),
                    createPath(
                            new float[]{x1, y2},
                            new float[]{x1, y3},
                            new float[]{x2, y3},
                            new float[]{x2, y2}),
                    new RectF(x2, y2, x3, y3)));
            mSidelineTouchPathList.add(new TouchPath(new RectF(x0, y4, x1, y5),
                    createPath(
                            new float[]{x1, y4},
                            new float[]{x1, y5},
                            new float[]{x2, y5},
                            new float[]{x2, y4}),
                    new RectF(x2, y4, x3, y5)));
            mSidelineTouchPathList.add(new TouchPath(new RectF(x0, y6, x1, y7),
                    createPath(
                            new float[]{x1, y6},
                            new float[]{x1, y7},
                            new float[]{x2, y7},
                            new float[]{x2, y6}),
                    new RectF(x2, y6, x3, y7)));
        }
        //Add By Wangwenlong to add TP test issue (8x26) HQ00000000 2013-09-29 End


        if (touchTestType == 4) {
            final float x0 = 0, x1 = variableLength,
                    x2 = widthPix- variableLength, x3 = widthPix;

            final float y0 = 0, y1 = variableLength,
                    y2 = hightPix- variableLength, y3 = hightPix;

            mSidelineTouchPathList.add(new TouchPath(new RectF(x0, y0, x1, y1),
                        createPath(
                                new float[]{x0, y1},
                                new float[]{x1, y1},
                                new float[]{x1, y2},
                                new float[]{x0, y2}),
                        new RectF(x0, y2, x1, y3)));

            mSidelineTouchPathList.add(new TouchPath(new RectF(x0, y2, x1, y3),
                        createPath(
                                new float[]{x1, y3},
                                new float[]{x1, y2},
                                new float[]{x2, y2},
                                new float[]{x2, y3}),
                        new RectF(x2, y2, x3, y3)));

            mSidelineTouchPathList.add(new TouchPath(new RectF(x2, y2, x3, y3),
                        createPath(
                                new float[]{x3, y2},
                                new float[]{x2, y2},
                                new float[]{x2, y1},
                                new float[]{x3, y1}),
                    new RectF(x2, y0, x3, y1)));

            mSidelineTouchPathList.add(new TouchPath(new RectF(x2, y0, x3, y1),
                        createPath(
                                new float[]{x2, y0},
                                new float[]{x2, y1},
                                new float[]{x1, y1},
                                new float[]{x1, y0}),
                    new RectF(x0, y0, x1, y1)));

            mDiagonalTouchPathList.add(new TouchPath(new RectF(x0, y0, x1, y1),
                    createPath(
                            new float[]{x0, y1},
                            new float[]{x1, y0},
                            new float[]{x3, y2},
                            new float[]{x2, y3}),
                    new RectF(x2, y2, x3, y3)));

            mDiagonalTouchPathList.add(new TouchPath(new RectF(x2, y0, x3, y1),
                    createPath(
                            new float[]{x2, y0},
                            new float[]{x3, y1},
                            new float[]{x1, y3},
                            new float[]{x0, y2}),
                    new RectF(x0, y2, x1, y3)));
        }

        if (touchTestType == 4) {
            final float xPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1.5f, mDisplayMetrics);
            final float x0 = xPx, 
				        x1 = variableLength,
                        x2 = widthPix- variableLength, 
                        x3 = widthPix - xPx;

            final float yPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 2f, mDisplayMetrics);
            final float y0 = yPx, 
				        y1 = variableLength,
                        y2 = hightPix/8 - variableLength, 
                        y3 = hightPix/8 - yPx,
                        y4 = hightPix/4 - variableLength, 
                        y5 = hightPix/4 - yPx,
                        y6 = hightPix/3 - variableLength, 
                        y7 = hightPix/3 - yPx,
                        y8 = hightPix/2 + yPx, 
                        y9 = hightPix/2 + variableLength, 
                        y10 = hightPix/2 - variableLength, 
                        y11 = hightPix/2 - yPx,
                        y12 = hightPix*2/3 + yPx, 
                        y13 = hightPix*2/3 + variableLength,    
                        y14 = hightPix*3/4 + yPx, 
                        y15 = hightPix*3/4 + variableLength,    
                        y16 = hightPix*7/8 + yPx, 
                        y17 = hightPix*7/8 + variableLength,     
                        y18 = hightPix - variableLength,
                        y19 = hightPix - yPx;

            mTransverseTouchPathList.add(new TouchPath(new RectF(x0, y0, x1, y1),
                    createPath(
                            new float[]{x1, y0},
                            new float[]{x1, y1},
                            new float[]{x2, y1},
                            new float[]{x2, y0}),
                    new RectF(x2, y0, x3, y1)));
            mTransverseTouchPathList.add(new TouchPath(new RectF(x0, y2, x1, y3),
                    createPath(
                            new float[]{x1, y2},
                            new float[]{x1, y3},
                            new float[]{x2, y3},
                            new float[]{x2, y2}),
                    new RectF(x2, y2, x3, y3)));
            mTransverseTouchPathList.add(new TouchPath(new RectF(x0, y4, x1, y5),
                    createPath(
                            new float[]{x1, y4},
                            new float[]{x1, y5},
                            new float[]{x2, y5},
                            new float[]{x2, y4}),
                    new RectF(x2, y4, x3, y5)));
            mTransverseTouchPathList.add(new TouchPath(new RectF(x0, y6, x1, y7),
                    createPath(
                            new float[]{x1, y6},
                            new float[]{x1, y7},
                            new float[]{x2, y7},
                            new float[]{x2, y6}),
                    new RectF(x2, y6, x3, y7)));
            mTransverseTouchPathList.add(new TouchPath(new RectF(x0, y8, x1, y9),
                    createPath(
                            new float[]{x1, y8},
                            new float[]{x1, y9},
                            new float[]{x2, y9},
                            new float[]{x2, y8}),
                    new RectF(x2, y8, x3, y9)));
            mTransverseTouchPathList.add(new TouchPath(new RectF(x0, y10, x1, y11),
                    createPath(
                            new float[]{x1, y10},
                            new float[]{x1, y11},
                            new float[]{x2, y11},
                            new float[]{x2, y10}),
                    new RectF(x2, y10, x3, y11)));
            mTransverseTouchPathList.add(new TouchPath(new RectF(x0, y12, x1, y13),
                    createPath(
                            new float[]{x1, y12},
                            new float[]{x1, y13},
                            new float[]{x2, y13},
                            new float[]{x2, y12}),
                    new RectF(x2, y12, x3, y13)));
	        mTransverseTouchPathList.add(new TouchPath(new RectF(x0, y14, x1, y15),
                    createPath(
                            new float[]{x1, y14},
                            new float[]{x1, y15},
                            new float[]{x2, y15},
                            new float[]{x2, y14}),
                    new RectF(x2, y14, x3, y15)));
	        mTransverseTouchPathList.add(new TouchPath(new RectF(x0, y16, x1, y17),
                    createPath(
                            new float[]{x1, y16},
                            new float[]{x1, y17},
                            new float[]{x2, y17},
                            new float[]{x2, y16}),
                    new RectF(x2, y16, x3, y17)));
	        mTransverseTouchPathList.add(new TouchPath(new RectF(x0, y18, x1, y19),
                    createPath(
                            new float[]{x1, y18},
                            new float[]{x1, y19},
                            new float[]{x2, y19},
                            new float[]{x2, y18}),
                    new RectF(x2, y18, x3, y19)));
        }
		
    }

    private Path createPath(float[] liftTop, float[] rightTop, float[] rightButtom, float[] liftButtom) {
        Path path = new Path();
        path.moveTo(liftTop[0], liftTop[1]);
        path.lineTo(liftButtom[0], liftButtom[1]);
        path.lineTo(rightButtom[0], rightButtom[1]);
        path.lineTo(rightTop[0], rightTop[1]);
        path.close();
        return path;
    }

    private class Panel extends View {

        private Paint paint;

        private Paint linePaint;

        private Canvas canvas;

        private float lastX, lastY = -1;

        public Panel(Context context) {
            super(context);
            mPointerList = new ArrayList<float[]>();
            canvas = new Canvas();
            canvas.setBitmap(mBitmap);

            paint = new Paint(Paint.DITHER_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setColor(Color.RED);
            paint.setAntiAlias(true);

            linePaint = new Paint(Paint.DITHER_FLAG);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(3);
            linePaint.setColor(Color.RED);
            linePaint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            canvas.drawBitmap(mBitmap, 0, 0, null);

            boolean sidelineAllPass = true;
            boolean diagonalAllPass = true;
			
            for (TouchPath touchPath : mSidelineTouchPathList){
                touchPath.onDraw(canvas);
                if (!touchPath.isTouchPass()) {
                    sidelineAllPass = false;
					diagonalAllPass = false;
                }
            }

            if (sidelineAllPass) {
                for (TouchPath touchPath : mDiagonalTouchPathList) {
                    touchPath.onDraw(canvas);
                    if (!touchPath.isTouchPass()) {
                        diagonalAllPass = false;
                    }
                }
            }

            if (diagonalAllPass) {
                for (TouchPath touchPath : mTransverseTouchPathList) {
                    touchPath.onDraw(canvas); 					
                }
            }

            for (float[] floats : mPointerList) {
                canvas.drawPoint(floats[0], floats[1], paint);
            }

        }

        private void dispatchTouchEventToTouchPath(float x, float y, int action) {

            boolean sidelineAllPass = true;
            boolean needClearPointer = true;
            for (TouchPath touchPath : mSidelineTouchPathList){
                if (!touchPath.isTouchPass()) {

                    if (MotionEvent.ACTION_DOWN == action) {
                        if (touchPath.isTouchDownInPath(x, y)) {
                            needClearPointer = false;
                        }
                    } else if (MotionEvent.ACTION_MOVE == action){
                        if (touchPath.isTouchMoveInPath(lastX, lastY, x, y)) {
                            needClearPointer = false;
                        }
                    }
                    sidelineAllPass = false;
                }
            }

            boolean diagonalAllPass = true;
            if (sidelineAllPass) {
                for (TouchPath touchPath : mDiagonalTouchPathList) {
                    if (!touchPath.isTouchPass()) {

                        if (MotionEvent.ACTION_DOWN == action) {
                            if (touchPath.isTouchDownInPath(x, y)) {
                                needClearPointer = false;
                            }
                        } else if (MotionEvent.ACTION_MOVE == action){
                            if (touchPath.isTouchMoveInPath(lastX, lastY, x, y)) {
                                needClearPointer = false;
                            }
                        }
                        diagonalAllPass = false;
                    }
                }
            }

            boolean transverseAllPass = true;
            if (diagonalAllPass) {
                for (TouchPath touchPath : mTransverseTouchPathList) {
                    if (!touchPath.isTouchPass()) {

                        if (MotionEvent.ACTION_DOWN == action) {
                            if (touchPath.isTouchDownInPath(x, y)) {
                                needClearPointer = false;
                            }
                        } else if (MotionEvent.ACTION_MOVE == action){
                            if (touchPath.isTouchMoveInPath(lastX, lastY, x, y)) {
                                needClearPointer = false;
                            }
                        }
                        transverseAllPass = false;
                    }
                }
            }

            lastX = x;
            lastY = y;
            mPointerList.add(new float[]{x, y});
            if (needClearPointer) {
                clearAllRect();
                clearPointer();
            }

            if (isAllPass()) {
                pass();
            }
        }

        private boolean isAllPass() {
            boolean sidelineAllPass = true;
            for (TouchPath touchPath : mSidelineTouchPathList){
                if (!touchPath.isTouchPass()) {
                    sidelineAllPass = false;
                }
            }

            boolean diagonallineAllPass = true;
            for (TouchPath touchPath : mDiagonalTouchPathList){
                if (!touchPath.isTouchPass()) {
                    diagonallineAllPass = false;
                }
            }

            boolean transverselineAllPass = true;
            for (TouchPath touchPath : mTransverseTouchPathList){
                if (!touchPath.isTouchPass()) {
                    transverselineAllPass = false;
                }
            }
			
            return sidelineAllPass && diagonallineAllPass && transverselineAllPass;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN :
                    dispatchTouchEventToTouchPath(event.getX(), event.getY(),
                            MotionEvent.ACTION_DOWN);
                    break;

                case MotionEvent.ACTION_MOVE:
                    for (int i = 0; i < event.getHistorySize(); i++) {
                        float x = event.getHistoricalX(i);
                        float y = event.getHistoricalY(i);

                        dispatchTouchEventToTouchPath(x, y,
                                MotionEvent.ACTION_MOVE);
                        invalidate();
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:

                    clearAllRect();
                    clearPointer();
                    break;
            }
            if (isAllPass()) {
                pass();
            }
            invalidate();

            return true;
        }

        private void clearAllRect() {
            for (TouchPath touchPath : mSidelineTouchPathList){
                if (!touchPath.isTouchPass()) {
                    touchPath.clearRect();
                }
            }

            for (TouchPath touchPath : mDiagonalTouchPathList) {
                if (!touchPath.isTouchPass()) {
                    touchPath.clearRect();
                }
            }

            for (TouchPath touchPath : mTransverseTouchPathList) {
                if (!touchPath.isTouchPass()) {
                    touchPath.clearRect();
                }
            }			
        }

        private void clearPointer() {
            lastX = -1;
            lastY = -1;

            mPointerList = new ArrayList<float[]>();
        }
    }

    class TouchPath {

        private RectF startRect;

        private Path pathRect;

        private RectF endRect;

        private RectF rect1;

        private RectF rect2;

        private boolean touchPass;

        private Paint paint;

        TouchPath(RectF rect1, Path pathRect, RectF rect2) {
            paint = new Paint(Paint.DITHER_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.GREEN);
            paint.setAntiAlias(true);
            this.rect1 = rect1;
            this.pathRect = pathRect;
            this.rect2 = rect2;
        }

        private void onDraw(Canvas canvas, Paint paint) {
            if (!touchPass) {
                canvas.drawRect(rect1, paint);
                canvas.drawRect(rect2, paint);
                canvas.drawPath(pathRect, paint);
            }
        }

        private void onDraw(Canvas canvas) {
            onDraw(canvas, paint);
        }

        private boolean isTouchMoveInPath(float lastX, float lastY, float x, float y) {

            if (isTouchDownInPath(x, y)) {
                return true;
            }

            if (lastX != -1 && lastY != -1) {
                if (isThisPath() && isInPath(x, y) && isCorrectTouchMove(lastX, lastY, x, y)) {

                    if (isInEndRect(x, y)) {
                        touchPass = true;
                    }
                    return true;
                } else {
                    clearRect();
                }
            }
            return false;
        }

        public void clearRect() {
            startRect = null;
            endRect = null;
        }

        private boolean isInEndRect(float x, float y) {
            return isThisPath() && endRect.contains(x, y);
        }

        private boolean isThisPath() {
            return startRect != null && endRect != null;
        }

        private boolean isInPath(float x, float y) {
            RectF rectF = new RectF();
            pathRect.computeBounds(rectF, true);
            Region region = new Region();
            region.setPath(pathRect, new Region((int) rectF.left,
                    (int) rectF.top, (int) rectF.right,
                    (int) rectF.bottom));
            return (startRect.contains(x, y) || region.contains((int)x, (int)y)
                    || endRect.contains(x, y));
        }

        private boolean isCorrectTouchMove(float lastX, float lastY, float x, float y){
            return (Math.abs(lastX - x) < 40 && Math.abs(lastY - y) < 40);
        }

        private boolean isTouchDownInPath(float x, float y) {
            if ((startRect == null || endRect == null) &&  !touchPass) {

                if (rect1.contains(x, y)) {
                    startRect = rect1;
                    endRect = rect2;
                    return true;

                } else if (rect2.contains(x, y)) {
                    startRect = rect2;
                    endRect = rect1;
                    return true;
                }
            }
            return false;
        }

        public boolean isTouchPass() {
            return touchPass;
        }
    }
}