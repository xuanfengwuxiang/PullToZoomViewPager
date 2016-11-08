package com.itheima.pulltozoomviewpager;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Created by xuanfengwuxiang on 2016/11/7.
 */

public class PullToZoomListView extends ListView implements AbsListView.OnScrollListener{
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "PullToZoomListView";
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float paramAnonymousFloat) {
            float f = paramAnonymousFloat - 1.0F;
            return 1.0F + f * (f * (f * (f * f)));
        }
    };
    int mActivePointerId = -1;
    private FrameLayout mHeaderContainer;
    private int mHeaderHeight;
    private ViewPager mHeaderImage;
    float mLastMotionY = -1.0F;
    float mLastScale = -1.0F;
    float mMaxScale = -1.0F;
    private AbsListView.OnScrollListener mOnScrollListener;
    private ScalingRunnalable mScalingRunnalable;
    private int mScreenHeight;
    private ImageView mShadow;
    public PullToZoomListView(Context context) {
        super(context);
        init(context);
    }

    public PullToZoomListView(Context paramContext,
                              AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init(paramContext);
    }

    public PullToZoomListView(Context paramContext,
                              AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init(paramContext);
    }


    //最终位置做缩放动画
    private void endScraling() {
        if (this.mHeaderContainer.getBottom() >= this.mHeaderHeight)
            Log.d("mmm", "endScraling");
        this.mScalingRunnalable.startAnimation(200L);
    }

    //初始化，添加headerView以及？？？
    private void init(Context context) {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(localDisplayMetrics);
        this.mScreenHeight = localDisplayMetrics.heightPixels;
        this.mHeaderContainer = new FrameLayout(context);
        this.mHeaderImage = new ViewPager(getContext());
        int screenWidth = localDisplayMetrics.widthPixels;
        setHeaderViewSize(screenWidth, (int) ( screenWidth* ( 9.0F/ 16.0F)));
        this.mShadow = new ImageView(context);
        FrameLayout.LayoutParams localLayoutParams = new FrameLayout.LayoutParams(
                -1, -2);
        localLayoutParams.gravity = 80;
        this.mShadow.setLayoutParams(localLayoutParams);
        this.mHeaderContainer.addView(this.mHeaderImage);
        this.mHeaderContainer.addView(this.mShadow);
        addHeaderView(this.mHeaderContainer);
        this.mScalingRunnalable = new ScalingRunnalable();
        super.setOnScrollListener(this);
    }

    //什么意思
    private void onSecondaryPointerUp(MotionEvent paramMotionEvent) {
        int i = (paramMotionEvent.getAction()) >> 8;
        if (paramMotionEvent.getPointerId(i) == this.mActivePointerId)
            if (i != 0) {
                int j = 1;
                this.mLastMotionY = paramMotionEvent.getY(0);
                this.mActivePointerId = paramMotionEvent.getPointerId(0);
                return;
            }
    }

    //什么意思
    private void reset() {
        this.mActivePointerId = -1;
        this.mLastMotionY = -1.0F;
        this.mMaxScale = -1.0F;
        this.mLastScale = -1.0F;
    }

    public ViewPager getHeaderView() {
        return this.mHeaderImage;
    }

    public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent) {
        return super.onInterceptTouchEvent(paramMotionEvent);
    }

    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2,
                            int paramInt3, int paramInt4) {
        super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
        if (this.mHeaderHeight == 0)
            this.mHeaderHeight = this.mHeaderContainer.getHeight();
    }
    //设置headerView的大小
    public void setHeaderViewSize(int paramInt1, int paramInt2) {
        Object localObject = this.mHeaderContainer.getLayoutParams();
        if (localObject == null)
            localObject = new AbsListView.LayoutParams(paramInt1, paramInt2);
        ((ViewGroup.LayoutParams) localObject).width = paramInt1;
        ((ViewGroup.LayoutParams) localObject).height = paramInt2;
        this.mHeaderContainer
                .setLayoutParams((ViewGroup.LayoutParams) localObject);
        this.mHeaderHeight = paramInt2;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (this.mOnScrollListener != null)
            this.mOnScrollListener.onScrollStateChanged(view,
                    scrollState);
    }



    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
        Log.d("mmm", "" + (0xFF & paramMotionEvent.getAction()));
        switch (0xFF & paramMotionEvent.getAction()) {
            case 4:
            case 0:
                if (!this.mScalingRunnalable.mIsFinished) {
                    this.mScalingRunnalable.abortAnimation();
                }
                this.mLastMotionY = paramMotionEvent.getY();
                this.mActivePointerId = paramMotionEvent.getPointerId(0);
                this.mMaxScale = (this.mScreenHeight / this.mHeaderHeight);
                this.mLastScale = (this.mHeaderContainer.getBottom() / this.mHeaderHeight);
                break;
            case 2:
                Log.d("mmm", "mActivePointerId" + mActivePointerId);
                int j = paramMotionEvent.findPointerIndex(this.mActivePointerId);
                if (j == -1) {
                    Log.e("PullToZoomListView", "Invalid pointerId="
                            + this.mActivePointerId + " in onTouchEvent");
                } else {
                    if (this.mLastMotionY == -1.0F)
                        this.mLastMotionY = paramMotionEvent.getY(j);
                    if (this.mHeaderContainer.getBottom() >= this.mHeaderHeight) {
                        ViewGroup.LayoutParams localLayoutParams = this.mHeaderContainer
                                .getLayoutParams();
                        float f = ((paramMotionEvent.getY(j) - this.mLastMotionY + this.mHeaderContainer
                                .getBottom()) / this.mHeaderHeight - this.mLastScale)
                                / 2.0F + this.mLastScale;
                        if ((this.mLastScale <= 1.0D) && (f < this.mLastScale)) {
                            localLayoutParams.height = this.mHeaderHeight;
                            this.mHeaderContainer
                                    .setLayoutParams(localLayoutParams);
                            return super.onTouchEvent(paramMotionEvent);
                        }
                        this.mLastScale = Math.min(Math.max(f, 1.0F),
                                this.mMaxScale);
                        localLayoutParams.height = ((int) (this.mHeaderHeight * this.mLastScale));
                        if (localLayoutParams.height < this.mScreenHeight)
                            this.mHeaderContainer
                                    .setLayoutParams(localLayoutParams);
                        this.mLastMotionY = paramMotionEvent.getY(j);
                        return true;
                    }
                    this.mLastMotionY = paramMotionEvent.getY(j);
                }
                break;
            case 1:
                reset();
                endScraling();
                break;
            case 3:
                int i = paramMotionEvent.getActionIndex();
                this.mLastMotionY = paramMotionEvent.getY(i);
                this.mActivePointerId = paramMotionEvent.getPointerId(i);
                break;
            case 5:
                onSecondaryPointerUp(paramMotionEvent);
                this.mLastMotionY = paramMotionEvent.getY(paramMotionEvent
                        .findPointerIndex(this.mActivePointerId));
                break;
            case 6:
        }
        return super.onTouchEvent(paramMotionEvent);
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int
            totalItemCount) {
        Log.d("mmm", "onScroll");
        float f = this.mHeaderHeight - this.mHeaderContainer.getBottom();
        Log.d("mmm", "f|" + f);
        if ((f > 0.0F) && (f < this.mHeaderHeight)) {
            Log.d("mmm", "1");
            int i = (int) (0.65D * f);
            this.mHeaderImage.scrollTo(0, -i);
        } else if (this.mHeaderImage.getScrollY() != 0) {
            Log.d("mmm", "2");
            this.mHeaderImage.scrollTo(0, 0);
        }
        if (this.mOnScrollListener != null) {
            this.mOnScrollListener.onScroll(view, firstVisibleItem,
                    visibleItemCount, totalItemCount);
        }

    }

    public void setOnScrollListener(
            AbsListView.OnScrollListener paramOnScrollListener) {
        this.mOnScrollListener = paramOnScrollListener;
    }

    //什么啥意思？？？
    public void setShadow(int paramInt) {
        this.mShadow.setBackgroundResource(paramInt);
    }

    //定义一个子线程做动画
    class ScalingRunnalable implements Runnable {
        long mDuration;
        boolean mIsFinished = true;
        float mScale;
        long mStartTime;

        ScalingRunnalable() {
        }

        public void abortAnimation() {
            this.mIsFinished = true;
        }

        public boolean isFinished() {
            return this.mIsFinished;
        }

        public void run() {
            float f2;
            ViewGroup.LayoutParams localLayoutParams;
            if ((!this.mIsFinished) && (this.mScale > 1.0D)) {
                float f1 = ((float) SystemClock.currentThreadTimeMillis() - (float) this.mStartTime)
                        / (float) this.mDuration;
                f2 = this.mScale - (this.mScale - 1.0F)
                        * PullToZoomListView.sInterpolator.getInterpolation(f1);
                localLayoutParams = PullToZoomListView.this.mHeaderContainer
                        .getLayoutParams();
                if (f2 > 1.0F) {
                    Log.d("mmm", "f2>1.0");
                    localLayoutParams.height = PullToZoomListView.this.mHeaderHeight;
                    ;
                    localLayoutParams.height = ((int) (f2 * PullToZoomListView.this.mHeaderHeight));
                    PullToZoomListView.this.mHeaderContainer
                            .setLayoutParams(localLayoutParams);
                    PullToZoomListView.this.post(this);
                    return;
                }
                this.mIsFinished = true;
            }
        }
        public void startAnimation(long paramLong) {
            this.mStartTime = SystemClock.currentThreadTimeMillis();
            this.mDuration = paramLong;
            this.mScale = ((float) (PullToZoomListView.this.mHeaderContainer
                    .getBottom()) / PullToZoomListView.this.mHeaderHeight);
            this.mIsFinished = false;
            PullToZoomListView.this.post(this);

        }
    }

}
