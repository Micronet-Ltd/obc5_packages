package com.android.camera;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.camera2.R;
import android.widget.FrameLayout;
import android.view.View;
import com.android.camera.ui.MainActivityLayout;
import android.view.KeyEvent;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;
/**
 * @author Taolin
 * @date Dec 03, 2013
 * @since v1.0
 */

public class SlideView extends View {

    public interface SlideListener {
        void onDone();
    }

    private static final int MSG_REDRAW = 1;
    private static final int DRAW_INTERVAL = 50;
    private static final int STEP_LENGTH = 5;

    private Paint mPaint;
    private VelocityTracker mVelocityTracker;
    private int mMaxVelocity;
    private LinearGradient mGradient;
    private int[] mGradientColors;
    private int mGradientIndex;
    private Interpolator mInterpolator;
    private SlideListener mSlideListener;
    private float mDensity;
    private Matrix mMatrix;
    private ValueAnimator mValueAnimator;

    private String mText;
    private int mTextSize;
    private int mTextLeft;
    private int mTextTop;
	
	private int dTextTop;
	private int mTextRight;
	private int dTextBottom;

    private int mSlider;
    private Bitmap mSliderBitmap;
    private int mSliderLeft;
    private int mSliderTop;
    private Rect mSliderRect;
    private int mSlidableLength;    // SlidableLength = BackgroundWidth - LeftMagins - RightMagins - SliderWidth
    private int mEffectiveLength;   // Suggested length is 20pixels shorter than SlidableLength
    private float mEffectiveVelocity;

    private float mStartX;
    private float mStartY;
    private float mLastX;
    private float mMoveX;
	
	int count=0;
    Timer time = new Timer();
    myTimeTask task = new myTimeTask();
    private float str_len;
	
    private boolean lock=false;
    private  FrameLayout mCameraRootView;
	private ShutterButton mShutterButton;
    private FrameLayout mCaptureLayout;
	private MainActivityLayout appRootView;
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_REDRAW:
                mMatrix.setTranslate(mGradientIndex, 0);
                mGradient.setLocalMatrix(mMatrix);
                invalidate();
                mGradientIndex += STEP_LENGTH * mDensity;
                if (mGradientIndex > mSlidableLength) {
                    mGradientIndex = 0;
                }
                mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
                break;
            }
        }
    };

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
        mInterpolator = new AccelerateDecelerateInterpolator();
        mDensity = getResources().getDisplayMetrics().density;
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.SlideView);
        mText = typeArray.getString(R.styleable.SlideView_maskText);
        mTextSize = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextSize, R.dimen.mask_text_size);
        mTextLeft = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextMarginLeft, R.dimen.mask_text_margin_left);
        mTextTop = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextMarginTop, R.dimen.mask_text_margin_top);
		
		dTextTop = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextSize, R.dimen.dtext_top);
		mTextRight = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextMarginRight, R.dimen.mask_text_margin_Right);
		dTextBottom = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextSize, R.dimen.dtext_bottom);

        mSlider = typeArray.getResourceId(R.styleable.SlideView_slider, R.drawable.ic_launcher);
        mSliderLeft = typeArray.getDimensionPixelSize(R.styleable.SlideView_sliderMarginLeft, R.dimen.slider_margin_left);
        mSliderTop = typeArray.getDimensionPixelSize(R.styleable.SlideView_sliderMarginTop, R.dimen.slider_margin_top);
        mSliderBitmap = BitmapFactory.decodeResource(getResources(), mSlider);
        mSliderRect = new Rect(mSliderLeft, mSliderTop, mSliderLeft + mSliderBitmap.getWidth(),
                mSliderTop + mSliderBitmap.getHeight());

        mSlidableLength = typeArray.getDimensionPixelSize(R.styleable.SlideView_slidableLength, R.dimen.slidable_length);
        mEffectiveLength = typeArray.getDimensionPixelSize(R.styleable.SlideView_effectiveLength,
                R.dimen.effective_length);
        mEffectiveVelocity = typeArray.getDimensionPixelSize(R.styleable.SlideView_effectiveVelocity,
                R.dimen.effective_velocity);
        typeArray.recycle();

        mGradientColors = new int[] {Color.argb(255, 120, 120, 120),
                Color.argb(255, 120, 120, 120), Color.argb(255, 255, 255, 255)};
        mGradient = new LinearGradient(0, 0, 100 * mDensity, 0, mGradientColors,
                new float[] {0, 0.7f, 1}, TileMode.MIRROR);
        mGradientIndex = 0;
        mPaint = new Paint();
        mMatrix = new Matrix();
        mPaint.setTextSize(mTextSize);
		str_len = mPaint.measureText(mText);
        mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
		if(str_len > mTextRight){
			
        	time.schedule(task, 0, 10);//schedule(task, 2 * 1000);
        }
    }

    public void setSlideListener(SlideListener slideListener) {
        mSlideListener = slideListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setShader(mGradient);
		 // Slider's moving rely on the mMoveX.
        canvas.drawBitmap(mSliderBitmap, mSliderLeft + mMoveX, mSliderTop, null);
		canvas.clipRect(mTextLeft, mTextTop-dTextTop, mTextLeft+mTextRight, mTextTop+dTextBottom);
        canvas.drawText(mText, mTextLeft-count, mTextTop, mPaint);
       
    }

    public void reset() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
        mMoveX = 0;
        mPaint.setAlpha(255);
        mHandler.removeMessages(MSG_REDRAW);
        mHandler.sendEmptyMessage(MSG_REDRAW);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If the start point is not on the slider, moving slider will not be executed.
        if (event.getAction() != MotionEvent.ACTION_DOWN
                && !mSliderRect.contains((int) mStartX, (int) mStartY)) {
            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
            }
            return super.onTouchEvent(event);
        }
        acquireVelocityTrackerAndAddMovement(event);
     mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
     float velocityX ;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mStartX = event.getX();
            mStartY = event.getY();
            mLastX = mStartX;
            mHandler.removeMessages(MSG_REDRAW);
            break;

        case MotionEvent.ACTION_MOVE:
            mLastX = event.getX();
            if (mLastX > mStartX) { // Can not exceed the left boundary, otherwise, mMoveX will get a minimum value.
                // The transparency of text will be changed along with moving slider
                int alpha = (int) (255 - (mLastX - mStartX) * 3 / mDensity);
                if (alpha > 1) {
                    mPaint.setAlpha(alpha);
                } else {
                    mPaint.setAlpha(0);
                }
                // Can not exceed the right boundary, otherwise, mMoveX will get a maximum value.
                if (mLastX - mStartX > mSlidableLength) {
                    mLastX = mStartX + mSlidableLength;
                    mMoveX = mSlidableLength;
                } else {
                    mMoveX = (int) (mLastX - mStartX);
                }
            } else {
                mLastX = mStartX;
                mMoveX = 0;
            }
            invalidate();
            break;

        case MotionEvent.ACTION_UP:
		//如果解锁成功，那么就退出水下模式
		//unlock();
		//mCameraRootView = (FrameLayout) findViewById(R.id.camera_app_root);
		 //mCaptureLayout = (FrameLayout) findViewById(R.id.bottombar_capture);
               
				// mCaptureLayout.setVisibility(View.VISIBLE);
				
				//mShutterButton = (ShutterButton) super.findViewById(R.id.shutter_button);
				//mShutterButton.setClickable(true);
                //  mShutterButton.setVisibility(View.VISIBLE);
				  //mShutterButton.setClickable(false);
	              // mShutterButton.setVisibility(View.GONE);
				  // mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
				   // velocityX = mVelocityTracker.getXVelocity();
				  //startAnimator(mLastX - mStartX,  mSlidableLength, velocityX, false);
				   //mLastX = mStartX;
                   //mMoveX = 0;
				   
        case MotionEvent.ACTION_CANCEL:
             velocityX = mVelocityTracker.getXVelocity();
            if (mLastX - mStartX > mEffectiveLength || velocityX > mEffectiveVelocity) {
                //startAnimator(mLastX - mStartX,  mSlidableLength, velocityX, true);
				mSlideListener.onDone();
				startAnimator(mLastX - mStartX,  0, 0, false);
            } else {
                startAnimator(mLastX - mStartX,  0, velocityX, false);
                mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
            }
            releaseVelocityTracker();
            break;
        }
        return super.onTouchEvent(event);
    }
	public boolean unlock(){
	   lock=true;
	  return lock;
}
    private void startAnimator(float start, float end, float velocity, boolean isRightMoving) {
        if (velocity < mEffectiveVelocity) {
            velocity = mEffectiveVelocity;
        }
        int duration = (int) (Math.abs(end - start) * 1000 / velocity);
        mValueAnimator = ValueAnimator.ofFloat(start, end);
        mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMoveX = (Float) animation.getAnimatedValue();
                int alpha = (int) (255 - (mMoveX) * 3 / mDensity);
                if (alpha > 1) {
                    mPaint.setAlpha(alpha);
                } else {
                    mPaint.setAlpha(0);
                }
                invalidate();
            }
        });
        mValueAnimator.setDuration(duration);
        mValueAnimator.setInterpolator(mInterpolator);
        if (isRightMoving) {
            mValueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                   // if (mSlideListener != null) {
                     //   mSlideListener.onDone();
					//	startAnimator(mLastX - mStartX,  0, 0, false);
                    //mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
                   // }
					
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
        mValueAnimator.start();
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.e("SlideView","onKeyDown:"+keyCode);
		// TODO Auto-generated method stub
		 if(keyCode == KeyEvent.KEYCODE_BACK) {
		       
		        return true;
		    } else if(keyCode == KeyEvent.KEYCODE_HOME){
				
				 return true;
			} else if(keyCode == KeyEvent.KEYCODE_MENU){
				return true;
				
			}
		return super.onKeyDown(keyCode, event);
	}
	 private class myTimeTask extends TimerTask {

		@Override
		public void run() {
			count++;
			if(count >= (int)str_len){
				count = 0 - (mTextRight);
			}
			
		}

	}
}
