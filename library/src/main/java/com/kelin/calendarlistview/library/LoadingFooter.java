package com.kelin.calendarlistview.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ProgressBar;
import android.widget.TextView;


public class LoadingFooter {
    protected View mLoadingFooter;

    protected ProgressBar mLoadingProgressBar;

    protected TextView mLoadingText;

    protected State mState = State.Idle;

    public static enum State {
        Idle, TheEnd, Loading
    }

    public LoadingFooter(Context context) {
        mLoadingFooter = LayoutInflater.from(context).inflate(
                R.layout.listfooter_loading, null);
        mLoadingFooter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });
        mLoadingProgressBar = (ProgressBar) mLoadingFooter
                .findViewById(R.id.progress_loading);
        mLoadingText = (TextView) mLoadingFooter
                .findViewById(R.id.text_loading);
        mLoadingFooter.setVisibility(View.GONE);

    }

    protected void init(String noMoreDataMessage, int loadingTextColor) {
        mLoadingText.setText(noMoreDataMessage);

        mLoadingText.setTextColor(loadingTextColor);
        setState(State.Idle);
    }

    public View getView() {
        return mLoadingFooter;
    }

    public State getState() {
        return mState;
    }

    public void setState(final State state, long delay) {
        mLoadingFooter.postDelayed(new Runnable() {

            @Override
            public void run() {
                setState(state);
            }
        }, delay);
    }

    public void setState(State status) {
        if (mState == status) {
            return;
        }
        mState = status;

        mLoadingFooter.setVisibility(View.VISIBLE);

        switch (status) {
            case Loading:
                mLoadingProgressBar.setVisibility(View.VISIBLE);
                mLoadingText.setVisibility(View.GONE);
                break;
            case TheEnd:
                mLoadingText.setVisibility(View.VISIBLE);
                mLoadingProgressBar.setVisibility(View.GONE);
                mLoadingText.startAnimation(getFadeInAnimation(null, 100));
                break;
            default:
                mLoadingFooter.setVisibility(View.GONE);
                break;
        }
    }

    public static Animation getFadeInAnimation(AnimationListener listener,
                                               long duration) {
        AlphaAnimation mFadeInAnimation = new AlphaAnimation(0, 1);
        mFadeInAnimation.setDuration(duration);
        mFadeInAnimation.setAnimationListener(listener);
        return mFadeInAnimation;
    }
}
