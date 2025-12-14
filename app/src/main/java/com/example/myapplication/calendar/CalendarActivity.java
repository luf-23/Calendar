package com.example.myapplication.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.GridLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TextView tvMonthYear;
    private ImageButton btnPrevMonth, btnNextMonth, btnToday, btnBack;
    private MaterialButtonToggleGroup toggleViewMode;
    private GridLayout weekdayHeader;
    
    // 日视图相关
    private View dayScheduleLayout;
    private TextView tvSelectedDate;
    private TextView tvEventCount;
    private RecyclerView rvDaySchedule;
    private View emptyView;
    private FloatingActionButton fabAddEvent;
    private DayScheduleAdapter dayScheduleAdapter;
    
    // 日程管理
    private EventManager eventManager;
    
    private static final int REQUEST_ADD_EVENT = 1001;
    private static final int REQUEST_EDIT_EVENT = 1002;
    
    private Calendar currentCalendar;
    private List<CalendarDay> calendarDays;
    private CalendarDay selectedDay;
    private ViewMode currentViewMode = ViewMode.MONTH;
    private boolean isUpdating = false;
    
    private enum ViewMode {
        YEAR, MONTH, DAY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initViews();
        initCalendar();
        setupListeners();
    }

    private void initViews() {
        calendarRecyclerView = findViewById(R.id.calendar_recycler_view);
        tvMonthYear = findViewById(R.id.tv_month_year);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        btnToday = findViewById(R.id.btn_today);
        btnBack = findViewById(R.id.btn_back);
        toggleViewMode = findViewById(R.id.toggle_view_mode);
        weekdayHeader = findViewById(R.id.weekday_header);
        
        // 初始化日程管理器
        eventManager = new EventManager(this);
    }

    private void initCalendar() {
        currentCalendar = Calendar.getInstance();
        updateCalendarView();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPrevMonth.setOnClickListener(v -> {
            if (currentViewMode == ViewMode.YEAR) {
                currentCalendar.add(Calendar.YEAR, -1);
            } else if (currentViewMode == ViewMode.MONTH) {
                currentCalendar.add(Calendar.MONTH, -1);
            } else {
                currentCalendar.add(Calendar.DAY_OF_MONTH, -1);
            }
            updateCalendarView();
        });

        btnNextMonth.setOnClickListener(v -> {
            if (currentViewMode == ViewMode.YEAR) {
                currentCalendar.add(Calendar.YEAR, 1);
            } else if (currentViewMode == ViewMode.MONTH) {
                currentCalendar.add(Calendar.MONTH, 1);
            } else {
                currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            updateCalendarView();
        });

        btnToday.setOnClickListener(v -> {
            currentCalendar = Calendar.getInstance();
            updateCalendarView();
        });

        toggleViewMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && !isUpdating) {
                ViewMode newMode = null;
                if (checkedId == R.id.btn_year_view) {
                    newMode = ViewMode.YEAR;
                } else if (checkedId == R.id.btn_month_view) {
                    newMode = ViewMode.MONTH;
                } else if (checkedId == R.id.btn_day_view) {
                    newMode = ViewMode.DAY;
                }
                
                if (newMode != null && newMode != currentViewMode) {
                    currentViewMode = newMode;
                    updateCalendarView();
                }
            }
        });
    }

    private void updateCalendarView() {
        if (isUpdating) {
            return;
        }
        isUpdating = true;
        
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

        // 更新标题
        if (currentViewMode == ViewMode.YEAR) {
            tvMonthYear.setText(year + "年");
        } else if (currentViewMode == ViewMode.MONTH) {
            tvMonthYear.setText(year + "年" + CalendarUtils.getMonthName(month));
        } else {
            tvMonthYear.setText(year + "年" + CalendarUtils.getMonthName(month) + CalendarUtils.formatDate(currentCalendar.getTime(), "dd日"));
        }

        // 根据视图模式更新日历
        if (currentViewMode == ViewMode.YEAR) {
            calendarRecyclerView.setVisibility(View.VISIBLE);
            weekdayHeader.setVisibility(View.GONE);
            hideDayScheduleView();
            
            calendarDays = CalendarUtils.getYearDays(year);
            calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else if (currentViewMode == ViewMode.MONTH) {
            calendarRecyclerView.setVisibility(View.VISIBLE);
            weekdayHeader.setVisibility(View.VISIBLE);
            hideDayScheduleView();
            
            calendarDays = CalendarUtils.getMonthDays(year, month);
            calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));
        } else {
            // 日视图：显示当日日程
            calendarRecyclerView.setVisibility(View.GONE);
            weekdayHeader.setVisibility(View.GONE);
            showDayScheduleView();
            isUpdating = false;
            return;
        }

        // 设置适配器
        if (calendarAdapter == null) {
            calendarAdapter = new CalendarAdapter(calendarDays, this::onDayClick);
            calendarRecyclerView.setAdapter(calendarAdapter);
        } else {
            calendarAdapter.updateDays(calendarDays);
        }
        
        isUpdating = false;
    }
    
    private void showDayScheduleView() {
        if (dayScheduleLayout == null) {
            // 动态加载日程视图
            LinearLayout container = findViewById(R.id.calendar_container);
            dayScheduleLayout = LayoutInflater.from(this).inflate(R.layout.layout_day_schedule, container, false);
            
            tvSelectedDate = dayScheduleLayout.findViewById(R.id.tv_selected_date);
            tvEventCount = dayScheduleLayout.findViewById(R.id.tv_event_count);
            rvDaySchedule = dayScheduleLayout.findViewById(R.id.rv_day_schedule);
            emptyView = dayScheduleLayout.findViewById(R.id.empty_view);
            fabAddEvent = dayScheduleLayout.findViewById(R.id.fab_add_event);
            
            rvDaySchedule.setLayoutManager(new LinearLayoutManager(this));
            
            fabAddEvent.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, EventEditActivity.class);
                intent.putExtra(EventEditActivity.EXTRA_EVENT_DATE, currentCalendar.getTimeInMillis());
                startActivityForResult(intent, REQUEST_ADD_EVENT);
            });
            
            container.addView(dayScheduleLayout);
        }
        
        dayScheduleLayout.setVisibility(View.VISIBLE);
        updateDaySchedule();
    }
    
    private void hideDayScheduleView() {
        if (dayScheduleLayout != null) {
            dayScheduleLayout.setVisibility(View.GONE);
        }
    }
    
    private void updateDaySchedule() {
        // 更新日期标题
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINA);
        tvSelectedDate.setText(sdf.format(currentCalendar.getTime()));
        
        // 获取当天的日程
        List<CalendarEvent> todayEvents = eventManager.getEventsForDate(currentCalendar.getTime());
        
        // 调试输出
        android.util.Log.d("CalendarActivity", "查询日期: " + sdf.format(currentCalendar.getTime()));
        android.util.Log.d("CalendarActivity", "找到日程数量: " + todayEvents.size());
        for (CalendarEvent event : todayEvents) {
            android.util.Log.d("CalendarActivity", "日程: " + event.getTitle());
        }
        
        if (todayEvents.isEmpty()) {
            rvDaySchedule.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            tvEventCount.setText("今日暂无日程");
        } else {
            rvDaySchedule.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            tvEventCount.setText("今日共" + todayEvents.size() + "个日程");
            
            if (dayScheduleAdapter == null) {
                dayScheduleAdapter = new DayScheduleAdapter(todayEvents, this::onEventClick);
                rvDaySchedule.setAdapter(dayScheduleAdapter);
            } else {
                dayScheduleAdapter.updateEvents(todayEvents);
            }
        }
    }
    
    private void onEventClick(CalendarEvent event) {
        // 点击日程卡片时打开编辑界面
        android.content.Intent intent = new android.content.Intent(this, EventEditActivity.class);
        intent.putExtra(EventEditActivity.EXTRA_EVENT_ID, event.getId());
        startActivityForResult(intent, REQUEST_EDIT_EVENT);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ADD_EVENT || requestCode == REQUEST_EDIT_EVENT) {
            if (resultCode == RESULT_OK || resultCode == EventEditActivity.RESULT_DELETED) {
                // 刷新日视图
                if (currentViewMode == ViewMode.DAY) {
                    updateDaySchedule();
                }
            }
        }
    }

    private void onDayClick(CalendarDay day) {
        if (isUpdating) {
            return;
        }
        
        // 如果是年视图，点击月份后切换到月视图
        if (currentViewMode == ViewMode.YEAR) {
            // 设置日历到选中的月份
            currentCalendar.set(Calendar.YEAR, day.getYear());
            currentCalendar.set(Calendar.MONTH, day.getMonth());
            currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
            
            // 切换到月视图
            isUpdating = true;
            currentViewMode = ViewMode.MONTH;
            toggleViewMode.check(R.id.btn_month_view);
            isUpdating = false;
            updateCalendarView();
            return;
        }
        
        // 如果是月视图，点击日期后切换到日视图
        if (currentViewMode == ViewMode.MONTH) {
            // 设置日历到选中的日期
            currentCalendar.set(Calendar.YEAR, day.getYear());
            currentCalendar.set(Calendar.MONTH, day.getMonth());
            currentCalendar.set(Calendar.DAY_OF_MONTH, day.getDay());
            
            // 切换到日视图
            isUpdating = true;
            currentViewMode = ViewMode.DAY;
            toggleViewMode.check(R.id.btn_day_view);
            isUpdating = false;
            updateCalendarView();
            return;
        }
        
        // 取消之前选中的日期
        if (selectedDay != null) {
            selectedDay.setSelected(false);
        }

        // 选中新日期
        selectedDay = day;
        day.setSelected(true);
        calendarAdapter.notifyDataSetChanged();

        // 显示选中的日期
        String dateStr = CalendarUtils.formatDate(day.getDate(), "yyyy年MM月dd日");
        Toast.makeText(this, dateStr, Toast.LENGTH_SHORT).show();
    }
}
