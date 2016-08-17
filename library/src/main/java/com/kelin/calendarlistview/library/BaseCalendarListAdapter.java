package com.kelin.calendarlistview.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by kelin on 16-7-22.
 */
public abstract class BaseCalendarListAdapter<T> extends SectionedBaseAdapter {
    public static final int CALENDAR_HEADER_HEIGHT = 30;
    protected LayoutInflater inflater;
    //key:"yyyy-MM-dd",value: list of your custom model
    protected TreeMap<String, List<T>> dateDataMap = new TreeMap<>();
    //key:"yyyy-MM-dd",value: position in list which use to scroll ListView to selected Date
    protected TreeMap<String, Integer> dateMapToPos = new TreeMap<>();
    //list to keep dateDataMap's key that convenient for get key by index.
    protected List<String> indexToTimeList = new ArrayList<>();

    public void setDateDataMap(TreeMap<String, List<T>> dateDataMap) {
        this.dateDataMap = dateDataMap;
        indexToTimeList = new ArrayList<>(dateDataMap.keySet());
        int pos = 0;
        for (String key : indexToTimeList) {
            dateMapToPos.put(key, pos);
            pos += dateDataMap.get(key).size() + 1;
        }
    }

    public BaseCalendarListAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getItem(int section, int position) {
        return null;
    }

    @Override
    public long getItemId(int section, int position) {
        return 0;
    }

    @Override
    public int getCountForSection(int section) {
        String date = indexToTimeList.get(section);
        return dateDataMap.get(date).size();
    }

    @Override
    public int getSectionCount() {
        return dateDataMap.size();
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent) {
        String date = indexToTimeList.get(section);

        return getItemView(dateDataMap.get(date).get(position), date, position, convertView, parent);
    }

    public abstract View getItemView(T model, String month, int pos, View convertView, ViewGroup parent);

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        String date = indexToTimeList.get(section);
        return getSectionHeaderView(date, convertView, parent);
    }

    /**
     * @param date require yyyy-mm-dd type
     * @return the position of date in current list,setSection() will make ListView scroll to specify section exactly.
     */
    public Integer getDataListIndexByDate(String date) {
        int i;
        for (i = 0; i < indexToTimeList.size(); i++) {
            String key = indexToTimeList.get(i);
            if (key.compareTo(date) > 0) {
                if (i > 0) {
                    return dateMapToPos.get(indexToTimeList.get(i - 1)) + 1;
                } else if (i == 0) {
                    return 1;
                }
            }
        }
        return dateMapToPos.get(indexToTimeList.get(i - 1)) + 1;
    }


    protected CalendarListView.OnCalendarViewItemClickListener.SelectedDateRegion getSelectedDateTypeByDate(String date) {
        String key = indexToTimeList.get(0);
        if (key.compareTo(date) > 0) {
            return CalendarListView.OnCalendarViewItemClickListener.SelectedDateRegion.BEFORE;
        }
        key = indexToTimeList.get(indexToTimeList.size() - 1);
        if (key.compareTo(date) < 0) {
            return CalendarListView.OnCalendarViewItemClickListener.SelectedDateRegion.AFTER;
        }
        return CalendarListView.OnCalendarViewItemClickListener.SelectedDateRegion.INSIDE;
    }

    /**
     * @param section section
     * @return get date(yyyy-MM-dd) by section
     */
    public String getDateBySection(int section) {
        return indexToTimeList.get(section);
    }

    /**
     * you can override this function to  custom your header view by yourself.
     *
     * @param date        "yyyy-mm-dd" format
     * @param convertView convertView
     * @param parent      parent
     * @return view
     */
    public View getSectionHeaderView(String date, View convertView, ViewGroup parent) {
        HeaderViewHolder headerViewHolder;
        if (convertView != null) {
            headerViewHolder = (HeaderViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.listitem_calendar_header, null);
            convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    CalendarHelper.dp2px(CALENDAR_HEADER_HEIGHT)));
            headerViewHolder = new HeaderViewHolder();
            headerViewHolder.dateText = (TextView) convertView.findViewById(R.id.calendar_header_text_date);
            convertView.setTag(headerViewHolder);
        }
        headerViewHolder.dateText.setText(date);
        return convertView;
    }

    private static class HeaderViewHolder {
        TextView dateText;
    }

}
