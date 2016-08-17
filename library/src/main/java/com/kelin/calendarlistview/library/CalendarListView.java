package com.kelin.calendarlistview.library;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by kelin on 16-7-21.
 */
public class CalendarListView extends FrameLayout {
    private static final int WEEK_ITEM_TEXT_SIZE = 12;

    protected PinnedHeaderListView listView;
    protected CalendarView calendarView;
    protected LinearLayout weekBar;
    private GestureDetector gestureDetector;
    private String currentSelectedDate;
    private float startY;
    private float downY;
    private float dy;
    private Status status = Status.LIST_CLOSE;

    protected BaseCalendarListAdapter calendarListAdapter;
    protected BaseCalendarItemAdapter calendarItemAdapter;

    private onListPullListener onListPullListener;
    private OnCalendarViewItemClickListener onCalendarViewItemClickListener;


    public CalendarListView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        CalendarHelper.init(context);
        gestureDetector = new GestureDetector(context, new FlingListener());
        LayoutInflater.from(context).inflate(R.layout.layout_calendar_listview, this);
        listView = (PinnedHeaderListView) findViewById(R.id.listview);
        final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.calendar_view_container);
        calendarView = new CalendarView(context);
        frameLayout.addView(calendarView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initListener();
        initWeekBar();
    }


    /**
     * @param calendarItemAdapter this adapter is for CalendarView
     * @param calendarListAdapter this adapter is for ListView
     */
    public void setCalendarListViewAdapter(BaseCalendarItemAdapter calendarItemAdapter, final BaseCalendarListAdapter calendarListAdapter) {
        this.calendarListAdapter = calendarListAdapter;
        this.calendarItemAdapter = calendarItemAdapter;
        this.listView.setAdapter(calendarListAdapter);
        this.calendarView.setCalendarItemAdapter(calendarItemAdapter);
        listView.getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                // the default selected date is the time of first item in data list
                if (TextUtils.isEmpty(currentSelectedDate)) {
                    currentSelectedDate = calendarListAdapter.getDateBySection(0);
                    int diffMonth = CalendarHelper.getDiffMonthByTime(calendarView.getSelectedDate(), currentSelectedDate);
                    if (diffMonth != 0) {
                        calendarView.changeMonth(diffMonth, currentSelectedDate, status);
                    }
                    CalendarListView.this.calendarView.animateSelectedViewToDate(currentSelectedDate);
                }
                //

                //when data changed,make sure the ListView scroll to last item's position.
                int listPos = CalendarListView.this.calendarListAdapter.getDataListIndexByDate(currentSelectedDate);
                if (listPos >= 0) {
                    listView.smoothScrollToPosition(listPos);
                }
                listView.setRefreshing(false);
                listView.getLoadingFooter().setState(LoadingFooter.State.Idle);

            }
        });
    }

    public String getCurrentSelectedDate() {
        return currentSelectedDate;
    }

    public void setOnMonthChangedListener(OnMonthChangedListener onMonthChangedListener) {
        calendarView.setOnMonthChangedListener(onMonthChangedListener);
    }


    public void setOnListPullListener(onListPullListener onListPullListener) {
        this.onListPullListener = onListPullListener;
    }

    public void setOnCalendarViewItemClickListener(OnCalendarViewItemClickListener onCalendarViewItemClickListener) {
        this.onCalendarViewItemClickListener = onCalendarViewItemClickListener;
    }

    public void setWeekBar(LinearLayout weekBar) {
        this.weekBar = weekBar;
    }

    public LinearLayout getWeekBar() {
        return weekBar;
    }


    protected void initListener() {
        calendarView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int listViewHeight = CalendarListView.this.getHeight() - weekBar.getHeight() - CalendarView.mItemHeight;
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) listView.getLayoutParams();
                layoutParams.height = listViewHeight;
                listView.setLayoutParams(layoutParams);
            }
        });

        calendarView.setOnDateSelectedListener(new CalendarView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(CalendarView calendarView, View view, String time, int pos) {
                if (!listView.smoothScrolling) {
                    currentSelectedDate = time;
                    int listPos = calendarListAdapter.getDataListIndexByDate(time);
                    OnCalendarViewItemClickListener.SelectedDateRegion selectedDateRegion = calendarListAdapter.getSelectedDateTypeByDate(time);
                    if (selectedDateRegion == OnCalendarViewItemClickListener.SelectedDateRegion.INSIDE) {
                        listView.smoothScrollToPosition(listPos);
                    }

                    if (onCalendarViewItemClickListener != null) {
                        onCalendarViewItemClickListener.onDateSelected(view, time, listPos, selectedDateRegion);
                    }
                }
            }
        });


        listView.setOnSectionChangedListener(new PinnedHeaderListView.OnSectionChangedListener() {
            @Override
            public void onSectionChanged(int newSection, int oldSection) {
                currentSelectedDate = calendarListAdapter.getDateBySection(newSection);
                String lastSelectDate = calendarListAdapter.getDateBySection(oldSection);
                int diffMonth = CalendarHelper.getDiffMonthByTime(lastSelectDate, currentSelectedDate);
                if (diffMonth == 0) {
                    diffMonth = CalendarHelper.getDiffMonthByTime(calendarView.getSelectedDate(), currentSelectedDate);
                }
                changeMonth(diffMonth);
            }
        });

        listView.addOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    // correct CalendarView's selected Date same with ListView's currentSelecteddate
                    if (!calendarView.getSelectedDate().equals(currentSelectedDate)) {
                        Log.v("Test", "ChangeMonth");
                        int diffMonth = CalendarHelper.getDiffMonthByTime(calendarView.getSelectedDate(), currentSelectedDate);
                        changeMonth(diffMonth);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });


        listView.setPullUpStateListener(new PinnedHeaderListView.PullUpStateListener() {

            @Override
            public void onLoadMore(PinnedHeaderListView listView) {
                onListPullListener.onLoadMore();
            }
        });
        listView.setPullDownStateListener(new PinnedHeaderListView.PullDownStateListener() {
            @Override
            public void onRefresh(PinnedHeaderListView listView) {
                listView.setRefreshing(true);
                onListPullListener.onRefresh();
            }
        });
    }

    protected void changeMonth(int diffMonth) {
        if (diffMonth != 0) {
            calendarView.changeMonth(diffMonth, currentSelectedDate, status);
        } else {
            if (status == Status.LIST_OPEN) {
                calendarView.animateCalendarViewToDate(currentSelectedDate);
            }
            calendarView.animateSelectedViewToDate(currentSelectedDate);
        }
    }


    protected void initWeekBar() {
        weekBar = (LinearLayout) findViewById(R.id.week_bar);
        String[] weeks = getResources().getStringArray(R.array.week);
        for (int i = 0; i < weeks.length; i++) {
            String week = weeks[i];
            TextView textView = new TextView(this.getContext());
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            textView.setText(week);
            textView.setTextSize(WEEK_ITEM_TEXT_SIZE);
            if (i == 0 || i == weeks.length - 1) {
                textView.setTextColor(getResources().getColor(R.color.red_ff725f));
            } else {
                textView.setTextColor(getResources().getColor(android.R.color.black));
            }
            textView.setGravity(Gravity.CENTER);
            weekBar.addView(textView);
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        CalendarView.SelectedRowColumn selectedRowColumn = calendarView.getSelectedRowColumn();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                dy = ev.getY();
                // when listView is close (calendarView is not shrink)
                if (status == Status.LIST_CLOSE && dy < weekBar.getBottom() + calendarView.getBottom()) {
                    return super.dispatchTouchEvent(ev);
                }
                if (status == Status.LIST_OPEN && dy > weekBar.getBottom() + CalendarView.mItemHeight) {
                    return super.dispatchTouchEvent(ev);
                }

                startY = ev.getRawY();
                downY = ev.getRawY();
                gestureDetector.onTouchEvent(ev);

                break;

            case MotionEvent.ACTION_MOVE:
                float curY = ev.getRawY();
                if (status == Status.LIST_CLOSE && curY > startY) {
                    return super.dispatchTouchEvent(ev);
                }

                if (status == Status.LIST_OPEN && curY < startY) {
                    if (dy > weekBar.getBottom() + CalendarView.mItemHeight) {
                        return super.dispatchTouchEvent(ev);
                    } else {
                        return true;
                    }
                }

                if (status == Status.LIST_CLOSE && dy < weekBar.getBottom() + calendarView.getBottom()) {
                    return super.dispatchTouchEvent(ev);
                }
                if (status == Status.LIST_OPEN && dy > weekBar.getBottom() + CalendarView.mItemHeight) {
                    return super.dispatchTouchEvent(ev);
                }

                gestureDetector.onTouchEvent(ev);

                if (Math.abs(listView.getTranslationY()) >= calendarView.getHeight() - CalendarView.mItemHeight
                        && curY < startY) {
                    listView.setTranslationY(-(calendarView.getHeight() - CalendarView.mItemHeight));
                }
                if (curY > startY && listView.getTranslationY() >= 0) {
                    listView.setTranslationY(0);
                }
                listView.setTranslationY(listView.getTranslationY() + (curY - startY));
                calendarView.setTranslationY(calendarView.getTranslationY() + selectedRowColumn.row
                        * (curY - startY) / ((calendarView.getHeight() / CalendarView.mItemHeight) - 1));
                startY = curY;
                status = Status.DRAGGING;
                return true;

            case MotionEvent.ACTION_UP:
                curY = ev.getRawY();
                if (status != Status.DRAGGING) {
                    return super.dispatchTouchEvent(ev);
                }
                gestureDetector.onTouchEvent(ev);
                if (status == Status.ANIMATING) {
                    return super.dispatchTouchEvent(ev);
                }
                if (curY < downY) {
                    if (Math.abs((curY - downY)) > calendarView.getHeight() / 2) {
                        animationToTop(selectedRowColumn);
                    } else {
                        animationToBottom();
                    }
                } else {
                    if (Math.abs((curY - downY)) < calendarView.getHeight() / 2) {
                        animationToTop(selectedRowColumn);
                    } else {
                        animationToBottom();
                    }

                }
        }

        return super.dispatchTouchEvent(ev);
    }

    private class FlingListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (status != Status.ANIMATING) {
                if (velocityY < 0) {
                    animationToTop(calendarView.getSelectedRowColumn());
                } else {
                    animationToBottom();
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    private void animationToTop(CalendarView.SelectedRowColumn selectedRowColumn) {
        status = Status.ANIMATING;
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, "translationY", -(calendarView.getHeight() - CalendarView.mItemHeight));
        objectAnimator1.setTarget(listView);
        objectAnimator1.setDuration(300).start();

        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, "translationY", -(calendarView.getHeight() * selectedRowColumn.row / ((calendarView.getHeight() / CalendarView.mItemHeight))));
        objectAnimator2.setTarget(calendarView);
        objectAnimator2.setDuration(300).start();

        objectAnimator2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                status = Status.LIST_OPEN;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void animationToBottom() {
        status = Status.ANIMATING;
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, "translationY", 0);
        objectAnimator1.setTarget(listView);
        objectAnimator1.setDuration(300).start();

        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, "translationY", 0);
        objectAnimator2.setTarget(calendarView);
        objectAnimator2.setDuration(300).start();

        objectAnimator2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                status = Status.LIST_CLOSE;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


    public interface OnCalendarViewItemClickListener {
        /**
         * <p>when item of Calendar View was clicked will be trigger. </p>
         *
         * @param View               the view(Calendar View Item) that was clicked.
         * @param selectedDate       the date has been selected is "yyyy-MM-dd" type
         * @param listSection        selectedDate would map to the section of ListView, ListView will scroll to specify position(listSection)
         * @param selectedDateRegion selectedDateRegion = SelectedDateRegion.INSIDE was deal with by CalendarListView,you would take care other conditions
         *                           just do anything you want or do nothing.
         *                           <ul>
         *                           <li>if selectedDateRegion = SelectedDateRegion.INSIDE : ListView will scroll to specify position,just care about it.</li>
         *                           <li>if listSection = SelectedDateRegion.BEFORE : imply the selectedDate is before any date in ListView</li>
         *                           <li>if listSection = SelectedDateRegion.AFTER :imply the selectedDate is after any date in ListView</li>
         *                           </ul>
         */
        void onDateSelected(View View, String selectedDate, int listSection, SelectedDateRegion selectedDateRegion);

        enum SelectedDateRegion {
            BEFORE,
            AFTER,
            INSIDE,
        }

    }


    public interface onListPullListener {

        void onRefresh();

        void onLoadMore();
    }


    public interface OnMonthChangedListener {
        /**
         * when month of calendar view has changed. it include user manually fling CalendarView to change
         * month,also include when user scroll ListView then beyond the current month.it will change month
         * of CalendarView automatically.
         *
         * @param yearMonth the date has been selected is "yyyy-MM-dd" type
         */
        void onMonthChanged(String yearMonth);
    }

    public enum Status {
        // when ListView been push to Top,the status is LIST_OPEN.
        LIST_OPEN,
        // when ListView stay original position ,the status is LIST_CLOSE.
        LIST_CLOSE,
        // when VIEW is dragging.
        DRAGGING,
        //when dragging end,the both CalendarView and ListView will animate to specify position.
        ANIMATING,
    }


}
