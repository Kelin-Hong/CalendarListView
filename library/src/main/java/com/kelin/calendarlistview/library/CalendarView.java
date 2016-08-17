package com.kelin.calendarlistview.library;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TreeMap;

/**
 * Created by kelin on 16-7-19.
 */
public class CalendarView extends FrameLayout {
    private static final int ROW_ITEM_COUNT = 7;
    private static final int COLUMN_ITEM_COUNT = 5;
    private static final int ITEM_HEIGHT = 40;
    public static int mItemWidth = 0;
    public static int mItemHeight = 0;
    private TreeMap<String, BaseCalendarItemModel> dayModelTreeMap;
    private OnDateSelectedListener onDateSelectedListener;
    protected BaseCalendarItemAdapter calendarItemAdapter;
    //gridview for contain calendar items
    protected GridView calendarGridView;
    //when selected one date,this view is floating on Calendar
    protected View selectedFloatingView;
    //a view like toast to show Month Changed.
    private TextView floatingMonthTips;
    private GestureDetector gestureDetector;
    private String currentMonth;
    private String selectedDate;
    //avoid to duplicate animation play.
    private boolean isMonthChanging = false;
    //use to custom your CalendarItemModel
    private Class<?> entityClass;
    private CalendarListView.OnMonthChangedListener onMonthChangedListener;

    public CalendarView(Context context) {
        super(context);
        mItemWidth = CalendarHelper.width / ROW_ITEM_COUNT;
        mItemHeight = mItemWidth;
        gestureDetector = new GestureDetector(context, new FlingListener());
    }


    protected void setOnMonthChangedListener(CalendarListView.OnMonthChangedListener onMonthChangedListener) {
        this.onMonthChangedListener = onMonthChangedListener;
    }

    protected String getSelectedDate() {
        return selectedDate;
    }

    protected void setOnDateSelectedListener(OnDateSelectedListener onDateSelectedListener) {
        this.onDateSelectedListener = onDateSelectedListener;
    }


    protected TreeMap<String, BaseCalendarItemModel> getDayModelTreeMap() {
        return dayModelTreeMap;
    }

    public TextView getFloatingMonthTips() {
        return floatingMonthTips;
    }

    public View getSelectedFloatingView() {
        return selectedFloatingView;
    }

    protected void initView() {
        View content = LayoutInflater.from(getContext()).inflate(R.layout.layout_calendar_view, this, true);
        calendarGridView = (GridView) content.findViewById(R.id.gridview);
        selectedFloatingView = content.findViewById(R.id.selected_view);
        floatingMonthTips = (TextView) findViewById(R.id.floating_month_tip);
        floatingMonthTips.setVisibility(GONE);
        floatingMonthTips.post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) floatingMonthTips.getLayoutParams();
                layoutParams.topMargin = 2 * mItemHeight - floatingMonthTips.getHeight() / 2;
                floatingMonthTips.setLayoutParams(layoutParams);
            }
        });
        calendarGridView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                dayModelTreeMap.size() * mItemHeight / ROW_ITEM_COUNT));
        calendarGridView.setAdapter(calendarItemAdapter);
        calendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                animateSelectedViewToPos(position);
                CalendarView.this.selectedDate = (String) calendarItemAdapter.getIndexToTimeMap().get(position);
                if (onDateSelectedListener != null) {
                    onDateSelectedListener.onDateSelected(CalendarView.this, view, CalendarView.this.selectedDate, position);
                }
            }
        });
        selectedFloatingView.setLayoutParams(new FrameLayout.LayoutParams(mItemWidth, mItemHeight));
    }


    protected void animateSelectedViewToDate(String date) {
        selectedDate = date;
        int position = calendarItemAdapter.getIndexToTimeMap().indexOf(date);
        animateSelectedViewToPos(position);
    }


    protected void animateSelectedViewToPos(int position) {
        int left = mItemWidth * (position % ROW_ITEM_COUNT);
        int top = mItemHeight * (position / ROW_ITEM_COUNT);
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("X", selectedFloatingView.getX(),
                left);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("Y", selectedFloatingView.getY(),
                top);
        ObjectAnimator.ofPropertyValuesHolder(selectedFloatingView, pvhX, pvhY).setDuration(200).start();
    }

    protected void animateCalendarViewToDate(String date) {
        int position = calendarItemAdapter.getIndexToTimeMap().indexOf(date);
        int row = position / ROW_ITEM_COUNT;
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, "translationY", -(getHeight() * row / ((getHeight() / mItemHeight))));
        objectAnimator2.setTarget(this);
        objectAnimator2.setDuration(300).start();
    }

    protected void initCalendarView() {
        Calendar calendar = Calendar.getInstance();
        long selectedTime = calendar.getTimeInMillis();
        selectedDate = CalendarHelper.YEAR_MONTH_DAY_FORMAT.format(selectedTime);
        currentMonth = CalendarHelper.YEAR_MONTH_FORMAT.format(selectedTime);
        TreeMap<String, BaseCalendarItemModel> calendarItemModelTreeMap = getDefaultCalendarDataListByYearMonth(
                currentMonth);
        for (BaseCalendarItemModel model : calendarItemModelTreeMap.values()) {
            if (CalendarHelper.areEqualDays(model.getTimeMill(), selectedTime)) {
                model.setStatus(BaseCalendarItemModel.Status.SELECTED);
            }
        }
        setDayModelTreeMap(calendarItemModelTreeMap);
        int pos = calendarItemAdapter.getIndexToTimeMap().indexOf(selectedDate);
        animateSelectedViewToPos(pos);
    }


    private void setDayModelTreeMap(TreeMap<String, BaseCalendarItemModel> dayModelTreeMap) {
        this.dayModelTreeMap = dayModelTreeMap;
        updateCalendarView();
    }


    protected void setCalendarItemAdapter(BaseCalendarItemAdapter calendarItemAdapter) {
        this.calendarItemAdapter = calendarItemAdapter;
        entityClass = (Class<?>) ((ParameterizedType) calendarItemAdapter.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        initCalendarView();
    }

    private void updateCalendarView() {
        initView();
        calendarItemAdapter.setDayModelList(dayModelTreeMap);
        calendarItemAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private TreeMap<String, BaseCalendarItemModel> getDefaultCalendarDataListByYearMonth(String yearMonth) {

        int calendarViewRow = COLUMN_ITEM_COUNT;
        int calendarViewColumn = ROW_ITEM_COUNT;

        Calendar calToday = Calendar.getInstance();
        Calendar calStartDate = Calendar.getInstance();
        calToday.setFirstDayOfWeek(Calendar.SUNDAY);
        calStartDate.setFirstDayOfWeek(Calendar.SUNDAY);
        long time = 0;
        try {
            time = CalendarHelper.YEAR_MONTH_FORMAT.parse(yearMonth).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calStartDate.setTimeInMillis(time);
        calStartDate.set(Calendar.DAY_OF_MONTH, 1);
        calStartDate.set(Calendar.HOUR_OF_DAY, 0);
        calStartDate.set(Calendar.MINUTE, 0);
        calStartDate.set(Calendar.SECOND, 0);
        Calendar activeCalendar = (Calendar) calStartDate.clone();
        calStartDate.add(Calendar.DAY_OF_WEEK, -(calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
        Calendar calEndDate = (Calendar) calStartDate.clone();
        calEndDate.add(Calendar.DAY_OF_MONTH, calendarViewRow * calendarViewColumn - 1);
        Calendar calCalendar = Calendar.getInstance();
        calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());
        TreeMap<String, BaseCalendarItemModel> dayModelList = new TreeMap<>();
        for (int i = 0; i < calendarViewRow * calendarViewColumn; i++) {
            BaseCalendarItemModel baseCalendarItemModel = null;
            try {
                baseCalendarItemModel = (BaseCalendarItemModel) entityClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            baseCalendarItemModel.setCurrentMonth(CalendarHelper.areEqualMonth(calCalendar, activeCalendar));
            baseCalendarItemModel.setToday(CalendarHelper.areEqualDays(calCalendar.getTimeInMillis(), calToday.getTimeInMillis()));
            baseCalendarItemModel.setTimeMill(calCalendar.getTimeInMillis());
            baseCalendarItemModel.setHoliday(Calendar.SUNDAY == calCalendar.get(Calendar.DAY_OF_WEEK) ||
                    Calendar.SATURDAY == calCalendar.get(Calendar.DAY_OF_WEEK));
            baseCalendarItemModel.setDayNumber(String.valueOf(calCalendar.get(Calendar.DAY_OF_MONTH)));
            calCalendar.add(Calendar.DAY_OF_MONTH, 1);
            dayModelList.put(CalendarHelper.YEAR_MONTH_DAY_FORMAT.format(
                    baseCalendarItemModel.getTimeMill()), baseCalendarItemModel);
        }
        return dayModelList;

    }


    protected void changeMonth(int offset, final String date, final CalendarListView.Status status) {
        this.selectedDate = date;
        isMonthChanging = true;
        CalendarView oldCalendarView = new CalendarView(this.getContext());
        oldCalendarView.setCalendarItemAdapter(this.calendarItemAdapter);
        FrameLayout container = (FrameLayout) this.getParent();
        container.addView(oldCalendarView);
        oldCalendarView.setTranslationY(this.getTranslationY());
        Calendar calendar = CalendarHelper.getCalendarByYearMonthDay(selectedDate);
        currentMonth = CalendarHelper.YEAR_MONTH_FORMAT.format(calendar.getTime());
        TreeMap<String, BaseCalendarItemModel> calendarItemModelTreeMap = getDefaultCalendarDataListByYearMonth(
                currentMonth);
        setDayModelTreeMap(calendarItemModelTreeMap);
        this.setTranslationY(this.getTranslationY() + offset * this.getHeight());
        animateCalendarViewToNewMonth(oldCalendarView, offset, oldCalendarView.getTranslationY(), new CalendarListView.OnMonthChangedListener() {
            @Override
            public void onMonthChanged(String yearMonth) {
                if (status == CalendarListView.Status.LIST_OPEN) {
                    animateCalendarViewToDate(date);
                }
                animateSelectedViewToDate(date);
            }
        });

    }


    private void animateCalendarViewToNewMonth(final CalendarView oldCalendarView, int offset, float translationY, final CalendarListView.OnMonthChangedListener monthChangeListener) {
        floatingMonthTips.setText(currentMonth);
        final ObjectAnimator alpha = ObjectAnimator.ofFloat(floatingMonthTips, "alpha", 0f, 1f, 0f);
        alpha.setDuration(1500);//设置动画时间
        alpha.setInterpolator(new AccelerateInterpolator());
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                floatingMonthTips.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                floatingMonthTips.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        alpha.start();

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, "translationY", translationY);
        objectAnimator1.setTarget(this);
        objectAnimator1.setDuration(800).start();

        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, "translationY", oldCalendarView.getTranslationY() - offset * this.getHeight());
        objectAnimator2.setTarget(oldCalendarView);
        objectAnimator2.setDuration(800).start();
        objectAnimator2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                FrameLayout container = (FrameLayout) CalendarView.this.getParent();
                container.removeView(oldCalendarView);
                isMonthChanging = false;
                if (onMonthChangedListener != null) {
                    onMonthChangedListener.onMonthChanged(currentMonth);
                }
                if (monthChangeListener != null) {
                    monthChangeListener.onMonthChanged(currentMonth);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private class FlingListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (!isMonthChanging) {
                if (Math.abs(velocityY) > Math.abs(velocityX)) {
                    Calendar calendar = CalendarHelper.getCalendarByYearMonthDay(selectedDate);
                    calendar.add(Calendar.MONTH, velocityY < 0 ? 1 : -1);
                    changeMonth(velocityY < 0 ? 1 : -1, CalendarHelper.YEAR_MONTH_DAY_FORMAT.format(calendar.getTime()),
                            CalendarListView.Status.LIST_CLOSE);
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    public SelectedRowColumn getSelectedRowColumn() {
        Calendar firstItemCalendar = CalendarHelper.getCalendarByYearMonthDay(dayModelTreeMap.firstKey());
        Calendar selectedItemCalendar = CalendarHelper.getCalendarByYearMonthDay(selectedDate);
        int diff = CalendarHelper.getDiffDayByTimeStamp(firstItemCalendar.getTimeInMillis(), selectedItemCalendar.getTimeInMillis());
        SelectedRowColumn selectedRowColumn = new SelectedRowColumn();
        selectedRowColumn.column = (diff % ROW_ITEM_COUNT);
        selectedRowColumn.row = (diff / ROW_ITEM_COUNT);
        return selectedRowColumn;
    }


    public static class SelectedRowColumn {
        public int row;
        public int column;
    }

    public interface OnDateSelectedListener {
        /**
         * @param calendarView current view
         * @param view         the view(Calendar View Item) that was clicked.
         * @param time         the date has been selected with "yyyy-MM-dd" format
         * @param pos          position in GridView
         */
        void onDateSelected(CalendarView calendarView, View view, String time, int pos);
    }


}
