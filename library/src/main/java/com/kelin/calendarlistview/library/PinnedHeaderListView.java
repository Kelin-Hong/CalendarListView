package com.kelin.calendarlistview.library;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class PinnedHeaderListView extends ListView implements OnScrollListener {

    private int mTriggerRefreshHeight;

    private boolean mTracking = false;

    private float mStartY = 0;

    private int mCurOffsetY = 0;

    private boolean mIsRefreshing = false;

    private Animation mAnimRotate, mAnimRotateBack;

    private boolean mCanRefresh = false;

    private PullDownStateListener mRefreshListener;

    private PullUpStateListener mPullUpListener;

    private View mPullHeader = null;

    private View mFirstHeader;

    private View mContainer = null;

    private ImageView mBackgroundImageView = null;

    private int mMaxHeaderHeight = Integer.MAX_VALUE;

    private boolean mPullDownEnabled = true;

    private boolean mCanPullDown = true;

    private boolean mAnimating;

    private String mPullString;

    private String mRefreshingString;

    private String mReleaseString;

    private TextView mMajorText;

    private TextView mMinorText;

    private ImageView mIndicator;
    private ViewGroup.LayoutParams mContainerLayoutParams;

    protected LoadingFooter mLoadingFooter;

    protected LoadingFooter mLoadingHeader;
    private List<OnScrollListener> mOnScrollListeners = new ArrayList<>();
    private PinnedSectionedHeaderAdapter mAdapter;
    private View mCurrentHeader;
    private int mCurrentHeaderViewType = 0;
    private float mHeaderOffset;
    private boolean mShouldPin = true;
    private int mCurrentSection = 0;
    protected boolean smoothScrolling = false;

    private OnSectionChangedListener onSectionChangedListener;

    public PinnedHeaderListView(Context context) {
        super(context);
        super.setOnScrollListener(this);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        super.setOnScrollListener(this);

    }

    public PinnedHeaderListView(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        super.setOnScrollListener(this);
    }

    public void setPinHeaders(boolean shouldPin) {
        mShouldPin = shouldPin;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        mAdapter = (PinnedSectionedHeaderAdapter) adapter;
        super.setAdapter(adapter);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        pullToRefreshListViewOnScroll(view, firstVisibleItem, visibleItemCount,
                totalItemCount);
        if (mOnScrollListeners != null && mOnScrollListeners.size() > 0) {
            for (OnScrollListener listener : mOnScrollListeners) {
                listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }

        if (mAdapter == null || mAdapter.getCount() == 0 || !mShouldPin || (firstVisibleItem < getHeaderViewsCount())) {
            mCurrentHeader = null;
            mHeaderOffset = 0.0f;
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                View header = getChildAt(i);
                if (header != null) {
                    header.setVisibility(VISIBLE);
                }
            }
            return;
        }

        firstVisibleItem -= getHeaderViewsCount();

        int section = mAdapter.getSectionForPosition(firstVisibleItem);
        int viewType = mAdapter.getSectionHeaderViewType(section);
        mCurrentHeader = getSectionHeaderView(section, mCurrentHeaderViewType != viewType ? null : mCurrentHeader);
        ensurePinnedHeaderLayout(mCurrentHeader);
        mCurrentHeaderViewType = viewType;

        mHeaderOffset = 0.0f;

        for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
            if (mAdapter.isSectionHeader(i)) {
                View header = getChildAt(i - firstVisibleItem);
                float headerTop = header.getTop();
                float pinnedHeaderHeight = mCurrentHeader.getMeasuredHeight();
                header.setVisibility(VISIBLE);
                if (pinnedHeaderHeight >= headerTop && headerTop > 0) {
                    mHeaderOffset = headerTop - header.getHeight();
                } else if (headerTop <= 0) {
                    header.setVisibility(INVISIBLE);
                }
            }
        }

        invalidate();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mOnScrollListeners != null && mOnScrollListeners.size() > 0) {
            for (OnScrollListener listener : mOnScrollListeners) {
                listener.onScrollStateChanged(view, scrollState);
            }
        }
    }

    private View getSectionHeaderView(int section, View oldView) {
        boolean shouldLayout = section != mCurrentSection || oldView == null;

        View view = mAdapter.getSectionHeaderView(section, oldView, this);
        if (shouldLayout) {
            // a new section, thus a new header. We should lay it out again
            if (onSectionChangedListener != null && !smoothScrolling) {
                onSectionChangedListener.onSectionChanged(section, mCurrentSection);
            }
            ensurePinnedHeaderLayout(view);
            mCurrentSection = section;

        }
        return view;
    }

    private Rect mRect = new Rect();

    private void ensurePinnedHeaderLayout(View header) {
        if (header.isLayoutRequested()) {
            int widthSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
            int heightSpec;
            ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
            if (layoutParams != null && layoutParams.height > 0) {
                heightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
            } else {
                heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            }
            header.measure(widthSpec, heightSpec);
            int height = header.getMeasuredHeight();
            header.layout(0, 0, getWidth(), height);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mAdapter == null || !mShouldPin || mCurrentHeader == null)
            return;

        int saveCount = canvas.save();
        canvas.translate(0, mHeaderOffset);
        canvas.clipRect(0, 0, getWidth(), mCurrentHeader.getMeasuredHeight()); // needed
        mCurrentHeader.draw(canvas);

        canvas.restoreToCount(saveCount);


    }

    public void addOnScrollListener(OnScrollListener l) {
        if (mOnScrollListeners != null) {
            mOnScrollListeners.add(l);
        }
    }

    public void removeOnScrollListener(OnScrollListener l) {
        if (mOnScrollListeners != null && mOnScrollListeners.contains(l)) {
            mOnScrollListeners.remove(l);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        super.setOnItemClickListener(listener);
    }

    public void setOnSectionChangedListener(OnSectionChangedListener onSectionChangedListener) {
        this.onSectionChangedListener = onSectionChangedListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mCanPullDown) {
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mPullDownEnabled) {
                    mCanRefresh = false;
                    if (!mIsRefreshing && isOnTop()) {
                        prepareTracking(ev);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTracking) {
                    float curY = ev.getY();

                    if ((curY - mStartY) > 10) {
                        requestDisallowInterceptTouchEvent(true);
                        mCurOffsetY = (int) ((curY - mStartY) / 2);
                        if ((mCurOffsetY) < mMaxHeaderHeight) {
                            setContainerHeight(mCurOffsetY);
                            if (mCurOffsetY >= mTriggerRefreshHeight) {
                                if (!mCanRefresh) {
                                    mMajorText.setText(mReleaseString);
                                    mIndicator.startAnimation(mAnimRotate);
                                    mCanRefresh = true;
                                }
                            } else {
                                if (mCanRefresh) {
                                    mMajorText.setText(mPullString);
                                    mIndicator.startAnimation(mAnimRotateBack);
                                    mCanRefresh = false;
                                }
                            }
                        } else {
                            mCurOffsetY = Math.max(0, mMaxHeaderHeight);
                        }
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                        return true;
                    }
                } else if (mPullDownEnabled && !mTracking && !mIsRefreshing && isOnTop()) {
                    prepareTracking(ev);
                }
                break;
            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:
                startBuncingBack();
                if (mTracking) {
                    if (mCanRefresh) {
                        if (mRefreshListener != null) {
                            mRefreshListener.onRefresh(PinnedHeaderListView.this);
                        }
                    }
                    stopTracking();
                }
                break;
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore;
            return false;
        } catch (IndexOutOfBoundsException e) {
            // ignore;
            return false;
        }

    }

    public static abstract class OnItemClickListener implements
            AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view,
                                int rawPosition, long id) {
            SectionedBaseAdapter adapter;
            if (adapterView.getAdapter().getClass()
                    .equals(HeaderViewListAdapter.class)) {
                HeaderViewListAdapter wrapperAdapter = (HeaderViewListAdapter) adapterView
                        .getAdapter();
                adapter = (SectionedBaseAdapter) wrapperAdapter
                        .getWrappedAdapter();
            } else {
                adapter = (SectionedBaseAdapter) adapterView.getAdapter();
            }
            int section = adapter.getSectionForPosition(rawPosition);
            int position = adapter.getPositionInSectionForPosition(rawPosition);

            if (position == -1) {
                onSectionClick(adapterView, view, section, id);
            } else {
                onItemClick(adapterView, view, section, position, id);
            }
        }

        public abstract void onItemClick(AdapterView<?> adapterView, View view,
                                         int section, int position, long id);

        public abstract void onSectionClick(AdapterView<?> adapterView,
                                            View view, int section, long id);

    }

    private void init(Context context, AttributeSet attrs) {
        setHeaderDividersEnabled(false);
        Resources resources = getResources();
        mLoadingFooter = new LoadingFooter(context);
        mLoadingFooter.setState(LoadingFooter.State.Idle);
        addFooterView(mLoadingFooter.getView());
        mLoadingHeader = new LoadingFooter(context);
        mLoadingHeader.setState(LoadingFooter.State.Loading);
        // addHeaderView(mLoadingHeader.getView());
        mTriggerRefreshHeight = 0;
        mTriggerRefreshHeight = resources
                .getDimensionPixelSize(R.dimen.pulldown_trigger_refresh_height);
        mPullHeader = LayoutInflater.from(getContext()).inflate(
                R.layout.pulldown_header, null);
        mPullHeader.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });
        mContainer = mPullHeader.findViewById(R.id.pull_header);
        mContainerLayoutParams = mContainer.getLayoutParams();
        setContainerHeight(0);
        mBackgroundImageView = (ImageView) mPullHeader
                .findViewById(R.id.img_bkg);
        mMajorText = (TextView) mPullHeader
                .findViewById(R.id.pull_header_major_text);
        mMinorText = (TextView) mPullHeader
                .findViewById(R.id.pull_header_minor_text);
        mIndicator = (ImageView) mPullHeader
                .findViewById(R.id.pullheader_indicator);

        mFirstHeader = mPullHeader;

        addHeaderView(mPullHeader);

        mPullString = resources.getString(R.string.pulldown_pull);
        mRefreshingString = resources.getString(R.string.pulldown_refreshing);
        mReleaseString = resources.getString(R.string.pulldown_release);

        mAnimRotate = AnimationUtils.loadAnimation(getContext(),
                R.anim.rotate_180);
        mAnimRotate.setFillAfter(true);

        mAnimRotateBack = AnimationUtils.loadAnimation(getContext(),
                R.anim.rotate_back_180);
        mAnimRotateBack.setFillAfter(true);
    }

    public LoadingFooter getLoadingFooter() {
        return mLoadingFooter;
    }

    public void pullToRefreshListViewOnScroll(AbsListView view,
                                              int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (mLoadingFooter.getState() == LoadingFooter.State.Loading
                || mLoadingFooter.getState() == LoadingFooter.State.TheEnd) {
            return;
        }
        if (firstVisibleItem + visibleItemCount >= totalItemCount
                && totalItemCount != 0
                && totalItemCount != getHeaderViewsCount()
                + getFooterViewsCount()) {
            mLoadingFooter.setState(LoadingFooter.State.Loading);
            if (mPullUpListener != null) {
                mPullUpListener.onLoadMore(this);
            }
        }
    }

    public void setPullDownEnabled(boolean mPullDownEnabled) {
        this.mPullDownEnabled = mPullDownEnabled;
    }

    private void setContainerHeight(int height) {
        mContainerLayoutParams.height = height;
        mContainer.setLayoutParams(mContainerLayoutParams);
    }

    private int getContainerHeight() {
        return mContainerLayoutParams.height;
    }

    private boolean isOnTop() {
        return getFirstVisiblePosition() <= 0 && mFirstHeader.getTop() >= 0;
    }

    private void prepareTracking(MotionEvent ev) {
        mMajorText.setText(mPullString);
        mTracking = true;
        mStartY = ev.getY();
        if (mRefreshListener != null) {
            mRefreshListener.onPullDownStarted(PinnedHeaderListView.this);
        }
    }

    private void stopTracking() {
        mTracking = false;
        requestDisallowInterceptTouchEvent(false);
    }

    public void setBackgroundImage(Bitmap bitmap) {
        mBackgroundImageView.setImageBitmap(bitmap);
    }

    public void setIndicatorRes(int resId) {
        mIndicator.setImageResource(resId);
    }

    public View getPullHeader() {
        return mPullHeader;
    }

    public TextView getMajorTextView() {
        return mMajorText;
    }

    public void setMajorText(String text) {
        mMajorText.setText(text);
    }

    public void setMinorText(String text) {
        mMinorText.setText(text);
    }

    public void setPullString(String mPullString) {
        this.mPullString = mPullString;
    }

    public void setRefreshingString(String mRefreshingString) {
        this.mRefreshingString = mRefreshingString;
    }

    public void setReleaseString(String mReleaseString) {
        this.mReleaseString = mReleaseString;
    }

    public void setPullDownStateListener(PullDownStateListener listener) {
        mRefreshListener = listener;
    }

    public void setPullUpStateListener(PullUpStateListener listener) {
        mPullUpListener = listener;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    public void setRefreshing1(boolean refreshing) {
        if (mIsRefreshing == refreshing) {
            return;
        }
        mIsRefreshing = refreshing;
        // View prog = findViewById(R.id.pull_header_prog);
        if (mIsRefreshing) {
            //mLoadingHeader.setState(State.Loading);
            addHeaderView(mLoadingHeader.getView());
        } else {
            removeHeaderView(mLoadingHeader.getView());
        }
    }

    public void setRefreshing(boolean refreshing) {
        if (mIsRefreshing == refreshing) {
            return;
        }
        mIsRefreshing = refreshing;
        View prog = findViewById(R.id.pull_header_prog);
        if (mIsRefreshing) {
            // set headerHeight
            if (getContainerHeight() < mTriggerRefreshHeight) {
                mCurOffsetY = mTriggerRefreshHeight;
                animateExpand();
            }

            mIndicator.clearAnimation();
            mIndicator.setVisibility(View.INVISIBLE);
            prog.setVisibility(View.VISIBLE);
            mMajorText.setText(mRefreshingString);
        } else {
            mIndicator.setVisibility(View.VISIBLE);
            mMajorText.setText(mPullString);
            prog.setVisibility(View.INVISIBLE);
            if (!mAnimating) {
                startBuncingBack();
            }
        }
    }

    public void animateExpand() {
        final ObjectAnimator animator = ObjectAnimator.ofInt(this,
                "containerHeight", mTriggerRefreshHeight);
        animator.setDuration(200);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAnimating = false;
            }
        });
    }

    public void startBuncingBack() {
        final ObjectAnimator animator = ObjectAnimator.ofInt(this,
                "containerHeight", 0);
        animator.setDuration(200);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAnimating = false;
            }
        });
    }

    public static abstract class PullDownStateListener {
        public void onPullDownStarted(final PinnedHeaderListView listView) {

        }

        public abstract void onRefresh(final PinnedHeaderListView listView);

        public void onBouncingEnd(final PinnedHeaderListView listView) {

        }
    }

    public static abstract class PullUpStateListener {
        public void onPullUpStarted(final PinnedHeaderListView listView) {

        }

        public abstract void onLoadMore(final PinnedHeaderListView listView);

        public void onBouncingEnd(final PinnedHeaderListView listView) {

        }
    }

    public static interface OnSectionChangedListener {
        void onSectionChanged(int newSection, int oldSection);
    }

    public void smoothScrollToPosition(final int position) {
        smoothScrolling = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setSelection(position);
                smoothScrolling = false;
            }
        }, 1000);
        View child = getChildAtPosition(this, position);
        // There's no need to scroll if child is already at top or view is already scrolled to its end
        if ((child != null) && ((child.getTop() == 0) || ((child.getTop() > 0) && !this.canScrollVertically(1)))) {
            return;
        }

        this.addOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    PinnedHeaderListView.this.removeOnScrollListener(this);

                    // Fix for scrolling bug
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            view.setSelection(position);
                        }
                    });
                }
            }

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                                 final int totalItemCount) {
            }
        });

        // Perform scrolling to position
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                PinnedHeaderListView.this.smoothScrollToPositionFromTop(position, 0);
            }
        });
    }

    public View getChildAtPosition(final AdapterView view, final int position) {
        final int index = position - view.getFirstVisiblePosition();
        if ((index >= 0) && (index < view.getChildCount())) {
            return view.getChildAt(index);
        } else {
            return null;
        }
    }
}
