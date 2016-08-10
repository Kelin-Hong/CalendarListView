# CalendarListView
---
A custom ListView combine with CalendarView which interactive each other. just watch demo to get more detail.

![CalendarListView Demo](art/CalendarViewDemo.gif) 

## Demo ##

Apk Download:[CalendarListView.apk](art/CalendarListView.apk) 

## Download ##

```groovy
 compile 'com.kelin.calendarlistview:library:1.0.0'
```


#### 中文文档：[CalendarListView 日历列表文档](http://www.jianshu.com/p/ca2af05b3a53)

## Usage ##


**1、Custom style of your CalendarView (like：add price、tag、icon into your items of CalendarView)**
```java
//create a Model extends BaseCalendarItemModel then add your custom field. 
public class CustomCalendarItemModel  extends BaseCalendarItemModel{     
   //data count.
   private int count;
   private boolean isFav;
   ...
   //getXXX 
   //setXXX
   ...
}
//create a custom Adapter extends BaseCalendarItemAdapter<T> (T  extends //BaseCalendarItemModel),then override getView function to custom your 
//calendar item's style. 
public class CalendarItemAdapter extends BaseCalendarItemAdapter<CustomCalendarItemModel>{
    //date format:"yyyy-MM-dd"
    @Override
    public View getView(String date, CustomCalendarItemModel model, View convertView, ViewGroup parent) {
        // CustomCalendarItemModel model = dayModelList.get(date); is supported.
        ....
        ....
        ViewGroup view = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.custom_calendar_item, null);
        TextView dayNum = (TextView)  view.findViewById(R.id.day_num);
        dayNum.setText(model.getDayNumber());
        ....
        //get data from model then render your UI.
        ....
        ....
   }
}
```

> tips：you can just use BaseCalendarItemAdapter but it is only show date in calendar View.


**2、Custom style of your ListView，override getSectionHeaderView and getItemView**

```java
public class ListItemAdapter extends BaseCalendarListAdapter<ListModel> {
    //date format:'yyyy-MM-dd'
    @Override
    public View getSectionHeaderView(String date, View convertView, ViewGroup parent) {
       // List<ListModel> modelList = dateDataMap.get(date);is supported.
       .....
       //custom style of SectionHeader
       .....
    }
   
    @Override
    public View getItemView(ListModel model,String date, int pos, View convertView, ViewGroup parent) {
      //you can get model by follow code. 
      //List<ListModel> modelList = dateDataMap.get(date);
      //model = modelList.get(pos) 
      
      .....
      //custom style of List Items
      .....
   }
}
```
**3、Initialize CalendarListView and set CalendarItemAdapter and ListItemAdapter**
```xml
<com.kelin.calendarlistview.library.CalendarListView   
   android:id="@+id/calendar_listview"    
   android:layout_width="match_parent"    
   android:layout_height="match_parent">
</com.kelin.calendarlistview.library.CalendarListView>
```
 
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
 ...
 calendarListView = (CalendarListView) findViewById(R.id.calendar_listview);
 
 listItemAdapter = new ListItemAdapter(this);
 calendarItemAdapter = new CalendarItemAdapter(this);

 calendarListView.setCalendarListViewAdapter(calendarItemAdapter, listItemAdapter);
 
 ...
}

```
**4、Loading data from server then update DataSet**

- CalendarView 

```java
private void onCalendarDataLoadFinish(List<Data> datas){
    ....
    ....
    //TreeMap<String, T>，key:'yyyy-MM-dd',value:model of this date.
    TreeMap<String, CustomCalendarItemModel> dateMap=calendarItemAdapter.getDayModelList();
    ....
    CustomCalendarItemModel customCalendarItemModel = dateMap.get(date);
    //update model
    customCalendarItemModel.setXXX(data.getXXX());
    ....
    calendarItemAdapter.notifyDataSetChanged();
}
```
- ListView

```java
//key:'yyyy-mm-dd' format date  
private TreeMap<String, List<ListModel>> listTreeMap = new TreeMap<>();

private void onListDataLoadFinish(List<Data> datas){
   ....
   ....
  for（Data item:datas) {
     String day=item.getDate();
    //add data
     if (listTreeMap.get(day) != null) {    
         List<NewsService.News.StoriesBean> list = listTreeMap.get(day);    
         list.add(i);
      } else {    
         List<NewsService.News.StoriesBean> list = new ArrayList<NewsService.News.StoriesBean>();    
         list.add(i);   
         listTreeMap.put(day, list);
     }
  }
 ....
 listItemAdapter.setDateDataMap(listTreeMap);
 listItemAdapter.notifyDataSetChanged();
}
  ```
**5、event support**
- date selected.
```java
calendarListView.setOnCalendarViewItemClickListener(new CalendarListView.OnCalendarViewItemClickListener() {   
     @Override    
     public void onDateSelected(View View, String selectedDate, int listSection) {   
     //do something....
     }
});
```
- month changed.
```java
calendarListView.setOnMonthChangedListener(new CalendarListView.OnMonthChangedListener() {    
       @Override    
       public void onMonthChanged(String yearMonth) {
       //yearMonth:'yyyy-MM-dd'
       
       }
});
```
- refresh and load more.
```java
calendarListView.setOnListPullListener(new CalendarListView.onListPullListener() {    
    @Override    
    public void onRefresh() {
          
    }    
    @Override    
    public void onLoadMore() {
 
    }
});
```
     
     
## License
   ```
    Copyright 2016 Kelin Hong
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
   ``` 