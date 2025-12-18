package com.example.myapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.adapter.CalendarAdapter;
import com.example.myapplication.ui.adapter.DayScheduleAdapter;
import com.example.myapplication.data.model.CalendarDay;
import com.example.myapplication.data.model.CalendarEvent;
import com.example.myapplication.manager.EventManager;
import com.example.myapplication.util.CalendarUtils;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 日历主Activity
 * 这是一个纯粹的日历应用，提供年/月/日视图切换和日程管理功能
 */
public class CalendarActivity extends AppCompatActivity {

    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TextView tvMonthYear;
    private ImageButton btnPrevMonth, btnNextMonth, btnToday;
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
        toggleViewMode = findViewById(R.id.toggle_view_mode);
        weekdayHeader = findViewById(R.id.weekday_header);
        
        // 日视图相关
        dayScheduleLayout = findViewById(R.id.day_schedule_layout);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvEventCount = findViewById(R.id.tv_event_count);
        rvDaySchedule = findViewById(R.id.rv_day_schedule);
        emptyView = findViewById(R.id.empty_view);
        fabAddEvent = findViewById(R.id.fab_add_event);
        
        // 初始化日程管理器
        eventManager = new EventManager(this);
        
        // 设置日视图RecyclerView
        rvDaySchedule.setLayoutManager(new LinearLayoutManager(this));
        dayScheduleAdapter = new DayScheduleAdapter(new ArrayList<>(), event -> {
            // 编辑日程
            Intent intent = new Intent(this, EventEditActivity.class);
            intent.putExtra("event_id", event.getId());
            startActivityForResult(intent, REQUEST_EDIT_EVENT);
        });
        rvDaySchedule.setAdapter(dayScheduleAdapter);
        
        // 默认隐藏日视图
        dayScheduleLayout.setVisibility(View.GONE);
    }

    private void initCalendar() {
        currentCalendar = Calendar.getInstance();
        
        // 设置日历RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        
        calendarAdapter = new CalendarAdapter(new ArrayList<>(), day -> {
            onDaySelected(day);
        });
        calendarRecyclerView.setAdapter(calendarAdapter);
        
        // 默认选择月视图
        toggleViewMode.check(R.id.btn_month_view);
        
        updateCalendar();
    }

    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> navigatePrevious());
        btnNextMonth.setOnClickListener(v -> navigateNext());
        btnToday.setOnClickListener(v -> goToToday());
        
        toggleViewMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_year_view) {
                    switchToYearView();
                } else if (checkedId == R.id.btn_month_view) {
                    switchToMonthView();
                } else if (checkedId == R.id.btn_day_view) {
                    switchToDayView();
                }
            }
        });
        
        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventEditActivity.class);
            if (selectedDay != null) {
                intent.putExtra("selected_date", selectedDay.getDate().getTime());
            }
            startActivityForResult(intent, REQUEST_ADD_EVENT);
        });
    }

    private void updateCalendar() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月", Locale.CHINA);
        tvMonthYear.setText(sdf.format(currentCalendar.getTime()));
        
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        calendarDays = CalendarUtils.getMonthDays(year, month);
        
        // 从数据库加载事件数量
        eventManager.loadEventCounts(calendarDays, counts -> {
            runOnUiThread(() -> {
                for (int i = 0; i < calendarDays.size(); i++) {
                    CalendarDay day = calendarDays.get(i);
                    Date date = day.getDate();
                    Integer count = counts.get(date);
                    if (count != null) {
                        day.setEventCount(count);
                    }
                }
                calendarAdapter.updateDays(calendarDays);
            });
        });
    }

    private void navigatePrevious() {
        if (currentViewMode == ViewMode.YEAR) {
            // 年视图：年份减一
            currentCalendar.add(Calendar.YEAR, -1);
            int year = currentCalendar.get(Calendar.YEAR);
            calendarDays = CalendarUtils.getYearDays(year);
            calendarAdapter.updateDays(calendarDays);
            tvMonthYear.setText(String.format(Locale.CHINA, "%d年", year));
        } else if (currentViewMode == ViewMode.MONTH) {
            // 月视图：月份减一
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        } else {
            // 日视图：天数减一
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1);
            selectedDay = null;
            for (CalendarDay day : calendarDays) {
                if (CalendarUtils.isSameDay(day.getCalendar(), currentCalendar)) {
                    selectedDay = day;
                    break;
                }
            }
            if (selectedDay == null) {
                selectedDay = new CalendarDay(currentCalendar);
            }
            updateDaySchedule();
        }
    }
    
    private void navigateNext() {
        if (currentViewMode == ViewMode.YEAR) {
            // 年视图：年份加一
            currentCalendar.add(Calendar.YEAR, 1);
            int year = currentCalendar.get(Calendar.YEAR);
            calendarDays = CalendarUtils.getYearDays(year);
            calendarAdapter.updateDays(calendarDays);
            tvMonthYear.setText(String.format(Locale.CHINA, "%d年", year));
        } else if (currentViewMode == ViewMode.MONTH) {
            // 月视图：月份加一
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        } else {
            // 日视图：天数加一
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
            selectedDay = null;
            for (CalendarDay day : calendarDays) {
                if (CalendarUtils.isSameDay(day.getCalendar(), currentCalendar)) {
                    selectedDay = day;
                    break;
                }
            }
            if (selectedDay == null) {
                selectedDay = new CalendarDay(currentCalendar);
            }
            updateDaySchedule();
        }
    }

    private void goToToday() {
        currentCalendar = Calendar.getInstance();
        updateCalendar();
        
        // 选择今天
        Calendar today = Calendar.getInstance();
        for (CalendarDay day : calendarDays) {
            if (CalendarUtils.isSameDay(day.getCalendar(), today)) {
                onDaySelected(day);
                break;
            }
        }
    }

    private void onDaySelected(CalendarDay day) {
        if (isUpdating) return;
        isUpdating = true;
        
        // 更新选中状态
        if (selectedDay != null) {
            selectedDay.setSelected(false);
        }
        selectedDay = day;
        day.setSelected(true);
        
        if (currentViewMode == ViewMode.YEAR) {
            // 年视图：点击月份，跳转到该月的月视图
            currentCalendar.set(Calendar.YEAR, day.getYear());
            currentCalendar.set(Calendar.MONTH, day.getMonth());
            currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
            toggleViewMode.check(R.id.btn_month_view);
        } else if (currentViewMode == ViewMode.MONTH) {
            // 月视图：点击日期，跳转到日视图
            toggleViewMode.check(R.id.btn_day_view);
        } else {
            // 日视图：刷新日程
            updateDaySchedule();
        }
        
        isUpdating = false;
    }

    private void switchToYearView() {
        currentViewMode = ViewMode.YEAR;
        calendarRecyclerView.setVisibility(View.VISIBLE);
        weekdayHeader.setVisibility(View.GONE);
        dayScheduleLayout.setVisibility(View.GONE);
        fabAddEvent.setVisibility(View.GONE);
        
        // 设置3列3行布局显示12个月
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        calendarRecyclerView.setLayoutManager(layoutManager);
        
        // 年视图显示12个月
        int year = currentCalendar.get(Calendar.YEAR);
        calendarDays = CalendarUtils.getYearDays(year);
        calendarAdapter.updateDays(calendarDays);
        
        // 更新标题
        tvMonthYear.setText(String.format(Locale.CHINA, "%d年", year));
    }

    private void switchToMonthView() {
        currentViewMode = ViewMode.MONTH;
        calendarRecyclerView.setVisibility(View.VISIBLE);
        weekdayHeader.setVisibility(View.VISIBLE);
        dayScheduleLayout.setVisibility(View.GONE);
        fabAddEvent.setVisibility(View.GONE);
        
        // 设置7列布局显示星期
        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        
        updateCalendar();
    }

    private void switchToDayView() {
        currentViewMode = ViewMode.DAY;
        calendarRecyclerView.setVisibility(View.GONE);
        weekdayHeader.setVisibility(View.GONE);
        dayScheduleLayout.setVisibility(View.VISIBLE);
        fabAddEvent.setVisibility(View.VISIBLE);
        
        if (selectedDay == null) {
            // 如果没有选中的日期，选择今天
            Calendar today = Calendar.getInstance();
            for (CalendarDay day : calendarDays) {
                if (CalendarUtils.isSameDay(day.getCalendar(), today)) {
                    selectedDay = day;
                    day.setSelected(true);
                    break;
                }
            }
            if (selectedDay == null && !calendarDays.isEmpty()) {
                selectedDay = calendarDays.get(0);
            }
        }
        
        updateDaySchedule();
    }

    private void updateDaySchedule() {
        if (selectedDay == null) return;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINA);
        tvSelectedDate.setText(sdf.format(selectedDay.getDate()));
        
        // 加载当天的事件
        eventManager.loadDayEvents(selectedDay.getDate(), events -> {
            runOnUiThread(() -> {
                dayScheduleAdapter.updateEvents(events);
                
                // 更新事件数量和空视图
                int eventCount = events.size();
                tvEventCount.setText(String.format(Locale.CHINA, "%d个日程", eventCount));
                
                if (eventCount == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    rvDaySchedule.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    rvDaySchedule.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新日历数据
        updateCalendar();
        if (currentViewMode == ViewMode.DAY) {
            updateDaySchedule();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 刷新日历
            updateCalendar();
            if (currentViewMode == ViewMode.DAY) {
                updateDaySchedule();
            }
        }
    }
}
