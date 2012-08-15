package com.tsoftime;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * 仿Launcher中的WorkSapce，可以左右滑动切换屏幕的类
 *
 * @author Yao.GUET
 *         blog: http://blog.csdn.net/Yao_GUET
 *         date: 2011-05-04
 */
public class ViewPager extends ViewGroup
{
    private static final String TAG = ViewPager.class.getSimpleName();
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private int mCurScreen = 0;

    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_SCROLLING = 1;

    private static final int SNAP_VELOCITY = 600;

    private int mTouchState = TOUCH_STATE_REST;
    private int mTouchSlop;
    private float mLastMotionX;

    private int mChildGap = 20;     // the gap between two items
    private boolean mNeedScrollToScreen;
    private Adapter mAdapter;

    public ViewPager(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ViewPager(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        Log.d(TAG, "onLayout");
        int childLeft = 0;
        final int childCount = getChildCount();
        Log.d(TAG, String.format("child count %d", childCount));
        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                final int childWidth = childView.getMeasuredWidth();
                childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
                childLeft += childWidth;
                childLeft += mChildGap;
            }
        }
        // scroll to the current screen
        if (mNeedScrollToScreen) {
            mNeedScrollToScreen = false;
            scrollTo(mCurScreen * (getWidth() + mChildGap), 0);
            mAdapter.getView(mCurScreen, getChildAt(mCurScreen), null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        Log.d(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ViewPager only canmCurScreen run at EXACTLY mode!");
        }

        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ViewPager only can run at EXACTLY mode!");
        }

        // The children are given the same width and height as the scrollLayout
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(width, height);
    }

    /**
     * According to the position of current layout
     * scroll to the destination page.
     */
    public void snapToDestination()
    {
        final int screenWidth = getWidth() + mChildGap;
        final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
        snapToScreen(destScreen);
    }

    public void snapToScreen(int whichScreen)
    {
        // get the valid layout page
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        if (getScrollX() != (whichScreen * getWidth())) {
            final int delta = whichScreen * (getWidth() + mChildGap) - getScrollX();
            mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta));
            mCurScreen = whichScreen;
            invalidate();        // Redraw the layout
        }
        Log.d(TAG, String.format("snap to screen %d", whichScreen));
        if (whichScreen >= getChildCount() - 1 && whichScreen + 1 < mAdapter.getCount()) {
            addView(mAdapter.getView(whichScreen + 1, null, null));
        }
        mAdapter.getView(whichScreen, getChildAt(whichScreen), null);
    }

    public void setToScreen(int whichScreen)
    {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        mCurScreen = whichScreen;
        mNeedScrollToScreen = true;
        if (mCurScreen >= getChildCount() - 1) {
            removeAllViews();
            for (int i = 0; i <= mCurScreen; ++i) {
                addView(mAdapter.getView(i, null, null));
            }
            invalidate();
            requestLayout();
        }
        mAdapter.getView(mCurScreen, getChildAt(mCurScreen), null);
    }

    public int getCurScreen()
    {
        return mCurScreen;
    }

    @Override
    public void computeScroll()
    {
        // TODO Auto-generated method stub
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // TODO Auto-generated method stub

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        final int action = event.getAction();
        final float x = event.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "event down!");
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastMotionX - x);
                mLastMotionX = x;
                scrollBy(deltaX, 0);
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "event : up");
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) velocityTracker.getXVelocity();
                Log.d(TAG, "velocityX:" + velocityX);

                if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
                    // Fling enough to move left
                    Log.d(TAG, "snap left");
                    snapToScreen(mCurScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY && mCurScreen < getChildCount() - 1) {
                    // Fling enough to move right
                    Log.d(TAG, "snap right");
                    snapToScreen(mCurScreen + 1);
                } else {
                    snapToDestination();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                break;
        }

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        // TODO Auto-generated method stub
        Log.d(TAG, "onInterceptTouchEvent-slop:" + mTouchSlop);

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
        final float x = ev.getX();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(mLastMotionX - x);
                if (xDiff > mTouchSlop) {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }

        return mTouchState != TOUCH_STATE_REST;
    }

    public Adapter getAdapter()
    {
        return mAdapter;
    }

    /*
     *
     */
    public void setAdapter(Adapter adapter)
    {
        mAdapter = adapter;
        for(int i = 0; i < adapter.getCount() ; ++i) {
            addView(adapter.getView(i, null, null));
        }
        adapter.getView(0, getChildAt(0), null);
        invalidate();
        requestLayout();
    }
}
