package com.kelin.calendarlistview.library;

import android.view.View;
import android.view.ViewGroup;

public interface PinnedSectionedHeaderAdapter {

    boolean isSectionHeader(int position);

    int getSectionForPosition(int position);

    View getSectionHeaderView(int section, View convertView, ViewGroup parent);

    int getSectionHeaderViewType(int section);

    int getCount();
}
