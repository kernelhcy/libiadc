package com.tsoftime;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;

/**
 * 下拉刷新ListView
 *
 * 下拉刷新ListView包含如下文件：
 *  PullToRefreshListView.java
 *  layout/pull_to_refresh_header.xml
 *  drawable/ic_pulltorefresh_arrow.png
 *  drawable/ic_pulltorefresh_progressbar.png
 *
 *  使用方式和ListView一样。
 *  设置OnRefreshListener来监听PullToRefreshListView的下拉刷新事件。
 *  在完成刷新之后，需要调研那个onRefreshComplete来通知PullToRefreshListView刷新完成。
 */
public class PullToRefreshListView extends ListView implements OnScrollListener, GestureDetector.OnGestureListener
{

    private final int MAXHEIGHT = 10;
    private static final int TAP_TO_REFRESH = 1;
    private static final int PULL_TO_REFRESH = 2;
    private static final int RELEASE_TO_REFRESH = 3;
    private static final int REFRESHING = 4;

    private static final String TAG = PullToRefreshListView.class.getSimpleName();

    private OnRefreshListener mOnRefreshListener;

    /**
     * Listener that will receive notifications every time the list scrolls.
     */
    private OnScrollListener mOnScrollListener;
    private LayoutInflater mInflater;

    private LinearLayout mRefreshView;
    private TextView mRefreshViewText;
    private ImageView mRefreshViewImage;
    private ImageView mRefreshViewProgress;
    private TextView mRefreshViewLastUpdated;

    private int mCurrentScrollState;
    private int mRefreshState;

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;
    private Animation mHeaderResetAnimation;
    private RotateAnimation mProgressBarAnimation;

    private int mRefreshViewHeight;
    private int mRefreshOriginalTopPadding;
    private int mLastMotionY;
    private GestureDetector mDetector;

    // whether the refresh image view is animating.
    private boolean isRefreshViewImageAnimationRunning = false;
    private RefreshViewImageAnimationListener refreshViewImageAnimationListener = null;

    public PullToRefreshListView(Context context)
    {
        super(context);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context)
    {

        GestureDetector localGestureDetector = new GestureDetector(this);
        this.mDetector = localGestureDetector;

        refreshViewImageAnimationListener = new RefreshViewImageAnimationListener();

        // Load all of the animations we need in code rather than through XML
        mFlipAnimation = new RotateAnimation(0, -180,
                                            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                            RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mFlipAnimation.setDuration(200);
        mFlipAnimation.setFillAfter(true);
        mFlipAnimation.setAnimationListener(refreshViewImageAnimationListener);

        mReverseFlipAnimation = new RotateAnimation(-180, 0,
                                                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mReverseFlipAnimation.setDuration(200);
        mReverseFlipAnimation.setFillAfter(true);
        mReverseFlipAnimation.setAnimationListener(refreshViewImageAnimationListener);

        mProgressBarAnimation = new RotateAnimation(0, 360,
                                                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mProgressBarAnimation.setInterpolator(new LinearInterpolator());
        mProgressBarAnimation.setDuration(1200);
        mProgressBarAnimation.setRepeatCount(Integer.MAX_VALUE);
        mProgressBarAnimation.setFillAfter(true);

        mHeaderResetAnimation = new HeaderPaddingAnimation();

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mRefreshView = (LinearLayout) mInflater.inflate(R.layout.pull_to_refresh_header, null);

        mRefreshViewText = (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_text);
        mRefreshViewImage = (ImageView) mRefreshView.findViewById(R.id.pull_to_refresh_image);
        mRefreshViewProgress = (ImageView) mRefreshView.findViewById(R.id.pull_to_refresh_progressview);
        mRefreshViewLastUpdated = (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_updated_at);

        mRefreshViewImage.setMinimumHeight(50);
        mRefreshView.setOnClickListener(new OnClickRefreshListener());
        mRefreshOriginalTopPadding = mRefreshView.getPaddingTop();

        mRefreshState = TAP_TO_REFRESH;
        super.setOnScrollListener(this);

        addHeaderView(mRefreshView);
        measureView(mRefreshView);
        mRefreshViewHeight = mRefreshView.getMeasuredHeight();

        mRefreshViewText.setText("下拉刷新");
    }

    @Override
    public void setAdapter(ListAdapter adapter)
    {
        super.setAdapter(adapter);
        setSelection(1);
    }

    /**
     * Set the listener that will receive notifications every time the list
     * scrolls.
     *
     * @param l The scroll listener.
     */
    @Override
    public void setOnScrollListener(OnScrollListener l)
    {
        mOnScrollListener = l;
    }

    /**
     * Register a callback to be invoked when this list should be refreshed.
     *
     * @param onRefreshListener The callback to run.
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener)
    {
        mOnRefreshListener = onRefreshListener;
    }

    /**
     * Set a text to represent when the list was last updated.
     *
     * @param lastUpdated Last updated at.
     */
    public void setLastUpdated(CharSequence lastUpdated)
    {
        if (lastUpdated != null) {
            mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
            mRefreshViewLastUpdated.setText(lastUpdated);
        } else {
            mRefreshViewLastUpdated.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        GestureDetector localGestureDetector = this.mDetector;
        localGestureDetector.onTouchEvent(event);
        final int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (!isVerticalScrollBarEnabled()) {
                    setVerticalScrollBarEnabled(true);
                }
                if (getFirstVisiblePosition() == 0 && mRefreshState != REFRESHING) {
                    if ((mRefreshView.getBottom() >= mRefreshViewHeight + MAXHEIGHT || mRefreshView.getTop() >= 0)) {
                        // Initiate the refresh
                        mRefreshState = REFRESHING;
                        prepareForRefresh();
                        onRefresh();
                    } else if (mRefreshView.getBottom() < mRefreshViewHeight + MAXHEIGHT
                                        || mRefreshView.getTop() <= 0) {
                        // Abort refresh and scroll down below the refresh view
                        resetHeader();
                        setSelection(1);
                        Log.d(TAG, String.format("reset to selection 1"));
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                applyHeaderPadding(event);
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     *
     * @param ev
     */
    private void applyHeaderPadding(MotionEvent ev)
    {
        if (mRefreshState == REFRESHING) return;

        int historicalY = (int) ev.getY();
        // Calculate the padding to apply, we divide by 1.7 to
        // simulate a more resistant effect during pull.
        int topPadding = (int) (((historicalY - mLastMotionY) - mRefreshViewHeight) / 2.5);

        mRefreshView.setPadding(mRefreshView.getPaddingLeft(),
            topPadding,
            mRefreshView.getPaddingRight(),
            mRefreshView.getPaddingBottom());

    }

    /**
     * Sets the header padding back to original size.
     */
    private void resetHeaderPadding()
    {
        Log.d(TAG, String.format("[resetHeaderPadding]currState=%d, refreshState=%d"
                                    , mCurrentScrollState, mRefreshState));

        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mHeaderResetAnimation);
    }

    /**
     * Resets the header to the original state.
     */
    private void resetHeader()
    {
        Log.d(TAG, String.format("[resetHeader]currState=%d, refreshState=%d", mCurrentScrollState, mRefreshState));
        if (mRefreshState != TAP_TO_REFRESH) {
            mRefreshState = TAP_TO_REFRESH;
            //resetHeaderPadding();

            // Set refresh view text to the pull label
            // mRefreshViewText.setText(R.string.pull_to_refresh_tap_label);//点击刷新是否有用
            mRefreshViewText.setText("下拉刷新");
            // Replace refresh drawable with arrow drawable
            mRefreshViewImage.setImageResource(R.drawable.ic_pulltorefresh_arrow);
            // Clear the full rotation animation
            mRefreshViewImage.clearAnimation();
            // Hide progress bar and arrow.
            mRefreshViewImage.setVisibility(View.GONE);
            mRefreshViewProgress.setVisibility(View.GONE);
            mRefreshViewProgress.clearAnimation();
        }
    }

    private void measureView(View child)
    {
        Log.d(TAG, String.format("[measureView]currState=%d, refreshState=%d", mCurrentScrollState, mRefreshState));
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT
                                           , ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (mCurrentScrollState == SCROLL_STATE_FLING
            && firstVisibleItem == 0 && mRefreshState != REFRESHING) {
            setSelection(1);
            mRefreshViewImage.setVisibility(View.INVISIBLE);
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(this, firstVisibleItem,
                visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        mCurrentScrollState = scrollState;

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    public void prepareForRefresh()
    {
        resetHeaderPadding();

        mRefreshViewImage.setVisibility(View.GONE);
        // We need this hack, otherwise it will keep the previous drawable.
        mRefreshViewImage.setImageDrawable(null);
        mRefreshViewProgress.setVisibility(View.VISIBLE);
        mRefreshViewProgress.startAnimation(mProgressBarAnimation);

        // Set refresh view text to the refreshing label
        mRefreshViewText.setText("正在刷新");
        mRefreshState = REFRESHING;
    }

    public void onRefresh()
    {
        //Log.d(TAG, "onRefresh");
        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }
    }

    /**
     * Resets the list to a normal state after a refresh.
     *
     * @param lastUpdated Last updated at.
     */
    public void onRefreshComplete(CharSequence lastUpdated)
    {
        setLastUpdated(lastUpdated);
        onRefreshComplete();
    }

    /**
     * Resets the list to a normal state after a refresh.
     */
    public void onRefreshComplete()
    {
        //Log.d(TAG, "onRefreshComplete");
        resetHeader();

        // If refresh view is visible when loading completes, scroll down to
        // the next item.
        if (mRefreshView.getBottom() > 0) {
            invalidateViews();
            if (getFirstVisiblePosition() <= 1) {
                setSelection(1);
            }
        }
    }

    /**
     * Invoked when the refresh view is clicked on. This is mainly used when
     * there's only a few items in the list and it's not possible to drag the
     * list.
     */
    private class OnClickRefreshListener implements OnClickListener
    {

        @Override
        public void onClick(View v)
        {
            if (mRefreshState != REFRESHING) {
                prepareForRefresh();
                onRefresh();
            }
        }

    }

    /**
     * Interface definition for a callback to be invoked when list should be
     * refreshed.
     */
    public interface OnRefreshListener
    {
        /**
         * Called when the list should be refreshed.
         * <p/>
         * A call to {@link com.tsoftime.PullToRefreshListView #onRefreshComplete()} is
         * expected to indicate that the refresh has completed.
         */
        public void onRefresh();
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        int firstVisibleItem = this.getFirstVisiblePosition();

        if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL
            && mRefreshState != REFRESHING) {
            if (firstVisibleItem == 0) {
                mRefreshViewImage.setVisibility(View.VISIBLE);
                if ((mRefreshView.getBottom() >= mRefreshViewHeight + MAXHEIGHT || mRefreshView.getTop() >= 0)
                    && mRefreshState != RELEASE_TO_REFRESH  && !isRefreshViewImageAnimationRunning) {
                    mRefreshViewText.setText("松开刷新");
                    mRefreshViewImage.clearAnimation();
                    mRefreshViewImage.startAnimation(mFlipAnimation);
                    mRefreshState = RELEASE_TO_REFRESH;
                } else if (mRefreshView.getBottom() < mRefreshViewHeight + MAXHEIGHT - 10
                    && mRefreshState != PULL_TO_REFRESH && !isRefreshViewImageAnimationRunning) {
                    mRefreshViewText.setText("下拉刷新");
                    if (mRefreshState != TAP_TO_REFRESH) {
                        mRefreshViewImage.clearAnimation();
                        mRefreshViewImage.startAnimation(mReverseFlipAnimation);
                    }
                    mRefreshState = PULL_TO_REFRESH;
                }
            } else {
                mRefreshViewImage.setVisibility(View.GONE);
                Log.d(TAG, String.format("scroll state %d refresh state %d", mCurrentScrollState, mRefreshState));
                resetHeader();
            }
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        return false;
    }

    private class RefreshViewImageAnimationListener implements Animation.AnimationListener
    {

        @Override
        public void onAnimationStart(Animation animation)
        {
            isRefreshViewImageAnimationRunning = true;
        }

        @Override
        public void onAnimationEnd(Animation animation)
        {
            isRefreshViewImageAnimationRunning = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation)
        {
        }
    }

    /**
     * The animation of the resetting header.
     */
    private class HeaderPaddingAnimation extends Animation
    {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t)
        {
            int topPadding = nowTopPadding - mRefreshOriginalTopPadding;
            topPadding *= (1 - interpolatedTime);
            mRefreshView.setPadding(mRefreshView.getPaddingLeft()
                                    , topPadding, mRefreshView.getPaddingRight()
                                    , mRefreshView.getPaddingBottom());
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight)
        {
            super.initialize(width, height, parentWidth, parentHeight);
            setDuration(200);
            setInterpolator(new DecelerateInterpolator());
            nowTopPadding = mRefreshView.getPaddingTop();
        }

        private int nowTopPadding;
    }
}
