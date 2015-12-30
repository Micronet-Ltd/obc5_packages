package com.qrt.factory.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created with IntelliJ IDEA. Owner: wangwenlong Date: 2012:10:20 Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class TouchPanelLineForBoard extends AbstractActivity {

    private static final String TAG = "TouchPanelEdge Test";

    private int hightPix = 960, widthPix = 0;

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
        logd(hightPix + " " + widthPix);// 800x480 or 480x320
        setContentView(new Panel(this));
    }

    private class Panel extends View {

        private float mov_x, mov_y;

        private Paint paint;

        private Paint linePaint;

        private Canvas canvas;

        private Bitmap tempBitmap = Bitmap.createBitmap(widthPix, hightPix,
                Bitmap.Config.RGB_565);

        private final RectF rightTopRect;

        private final RectF liftButtomRect;

        private RectF startRect;

        private Path pathRect;

        private RectF endRect;

        private final int mInt = 80;

        private final Path mRightTopToLiftButtom;

        public Panel(Context context) {
            super(context);
            paint = new Paint(Paint.DITHER_FLAG);
            canvas = new Canvas();
            canvas.setBitmap(tempBitmap);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.GREEN);
            paint.setAntiAlias(true);

            linePaint = new Paint(Paint.DITHER_FLAG);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(3);
            linePaint.setColor(Color.RED);
            linePaint.setAntiAlias(true);

            rightTopRect = new RectF(widthPix - mInt, 0, widthPix, mInt);
            liftButtomRect = new RectF(0, hightPix - mInt, mInt, hightPix);
			int mTempInt =110;
            mRightTopToLiftButtom = new Path();
            mRightTopToLiftButtom.moveTo(widthPix, 0);
            mRightTopToLiftButtom.lineTo(widthPix - mTempInt, 0);
            mRightTopToLiftButtom.lineTo(0, hightPix - mTempInt);
            mRightTopToLiftButtom.lineTo(0, hightPix);
            mRightTopToLiftButtom.lineTo(mTempInt, hightPix);
            mRightTopToLiftButtom.lineTo(widthPix, mTempInt);
            mRightTopToLiftButtom.close();

            startRect = rightTopRect;
            pathRect = mRightTopToLiftButtom;
            endRect = liftButtomRect;
        }

        @Override
        protected void onDraw(Canvas canvas) {

            canvas.drawBitmap(tempBitmap, 0, 0, null);
            canvas.drawRect(rightTopRect, paint);
            canvas.drawRect(liftButtomRect, paint);
            canvas.drawPath(mRightTopToLiftButtom, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (endRect != null) {
                    if (mov_x != -1) {
                        canvas.drawLine(mov_x, mov_y, x,
                                y, linePaint);

                        if (!isInPath(x, y) || !isTouchMove(x, y)) {
                            mov_x = -1;
                            tempBitmap = Bitmap.createBitmap(widthPix, hightPix,
                                    Bitmap.Config.RGB_565);
                            canvas.setBitmap(tempBitmap);
                            invalidate();
                            return true;
                        }
                        if (endRect.contains(x, y)) {
                                pass();
                            }
                        mov_x = x;
                        mov_y = y;
                        }
                    }

                }
                invalidate();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mov_x = x;
                mov_y = y;
                if (!startRect.contains(x, y)) {
                    mov_x = -1;
                    tempBitmap = Bitmap.createBitmap(widthPix, hightPix,
                            Bitmap.Config.RGB_565);
                    canvas.setBitmap(tempBitmap);
                    invalidate();
                    return true;
                }
                canvas.drawPoint(mov_x, mov_y, paint);
                invalidate();
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL ||
                    event.getAction() == MotionEvent.ACTION_UP) {
                mov_x = -1;
                tempBitmap = Bitmap.createBitmap(widthPix, hightPix,
                        Bitmap.Config.RGB_565);
                canvas.setBitmap(tempBitmap);
                invalidate();
            }
            return true;
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

        private boolean isTouchMove(float x, float y){
            return (Math.abs(mov_x - x) < 40 && Math.abs(mov_y - y) < 40);
        }
    }
}