package com.kelin.calendarlistview;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kelin.calendarlistview.library.BaseCalendarListAdapter;
import com.kelin.calendarlistview.library.CalendarHelper;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;


public class DayNewsListAdapter extends BaseCalendarListAdapter<NewsService.News.StoriesBean> {


    public DayNewsListAdapter(Context context) {
        super(context);
    }

    @Override
    public View getSectionHeaderView(String date, View convertView, ViewGroup parent) {
        HeaderViewHolder headerViewHolder;
        List<NewsService.News.StoriesBean> modelList = dateDataMap.get(date);
        if (convertView != null) {
            headerViewHolder = (HeaderViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.listitem_calendar_header, null);
            headerViewHolder = new HeaderViewHolder();
            headerViewHolder.dayText = (TextView) convertView.findViewById(R.id.header_day);
            headerViewHolder.yearMonthText = (TextView) convertView.findViewById(R.id.header_year_month);
            headerViewHolder.isFavImage = (ImageView) convertView.findViewById(R.id.header_btn_fav);
            convertView.setTag(headerViewHolder);
        }

        Calendar calendar = CalendarHelper.getCalendarByYearMonthDay(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayStr = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (day < 10) {
            dayStr = "0" + dayStr;
        }
        headerViewHolder.dayText.setText(dayStr);
        headerViewHolder.yearMonthText.setText(CalendarActivity.YEAR_MONTH_FORMAT.format(calendar.getTime()));
        return convertView;
    }

    @Override
    public View getItemView(NewsService.News.StoriesBean model, String date, int pos, View convertView, ViewGroup parent) {
        ContentViewHolder contentViewHolder;
        if (convertView != null) {
            contentViewHolder = (ContentViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.listitem_calendar_content, null);
            contentViewHolder = new ContentViewHolder();
            contentViewHolder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            contentViewHolder.timeTextView = (TextView) convertView.findViewById(R.id.time);
            contentViewHolder.newsImageView = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(contentViewHolder);
        }

        contentViewHolder.titleTextView.setText(model.getTitle());
        contentViewHolder.timeTextView.setText(date);
//        GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(convertView.getResources())
//                .setRoundingParams(RoundingParams.asCircle())
//                .build();
//        contentViewHolder.newsImageView.setHierarchy(hierarchy);
//        contentViewHolder.newsImageView.setImageURI(Uri.parse(model.getImages().get(0)));
        Picasso.with(convertView.getContext()).load(Uri.parse(model.getImages().get(0)))
                .into(contentViewHolder.newsImageView);
        return convertView;
    }

    private static class HeaderViewHolder {
        TextView dayText;
        TextView yearMonthText;
        ImageView isFavImage;
    }

    private static class ContentViewHolder {
        TextView titleTextView;
        TextView timeTextView;
        ImageView newsImageView;
    }

}
