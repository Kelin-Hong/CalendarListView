package com.kelin.calendarlistview;

import com.kelin.calendarlistview.library.BaseCalendarItemModel;

/**
 * Created by kelin on 16-7-20.
 */
public class CustomCalendarItemModel  extends BaseCalendarItemModel{
    private int newsCount;
    private boolean isFav;

    public int getNewsCount() {
        return newsCount;
    }

    public void setNewsCount(int newsCount) {
        this.newsCount = newsCount;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean fav) {
        isFav = fav;
    }
}
