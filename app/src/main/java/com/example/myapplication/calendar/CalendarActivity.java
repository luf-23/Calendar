package com.example.myapplication.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TextView tvMonthYear;
    private ImageButton btnPrevMonth, btnNextMonth, btnToday, btnBack;
    private MaterialButtonToggleGroup toggleViewMode;
    private FloatingActionButton fabAddEvent;
    
    private Calendar currentCalendar;
    private List<CalendarDay> calendarDays;
    private CalendarDay selectedDay;
    private ViewMode currentViewMode = ViewMode.MONTH;
    
    private EventDatabase eventDatabase;
    
    private enum ViewMode {
        MONTH, WEEK, DAY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initViews();
        initDatabase();
        initCalendar();
        setupListeners();
        loadEvents();
    }

    private void initViews() {
        calendarRecyclerView = findViewById(R.id.calendar_recycler_view);
        tvMonthYear = findViewById(R.id.tv_month_year);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        btnToday = findViewById(R.id.btn_today);
        btnBack = findViewById(R.id.btn_back);
        toggleViewMode = findViewById(R.id.toggle_view_mode);
        fabAddEvent = findViewById(R.id.fab_add_event);
    }

    private void initDatabase() {
        eventDatabase = new EventDatabase(this);
    }

    private void initCalendar() {
        currentCalendar = Calendar.getInstance();
        updateCalendarView();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPrevMonth.setOnClickListener(v -> {
            if (currentViewMode == ViewMode.MONTH) {
                currentCalendar.add(Calendar.MONTH, -1);
            } else if (currentViewMode == ViewMode.WEEK) {
                currentCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            } else {
                currentCalendar.add(Calendar.DAY_OF_MONTH, -1);
            }
            updateCalendarView();
        });

        btnNextMonth.setOnClickListener(v -> {
            if (currentViewMode == ViewMode.MONTH) {
                currentCalendar.add(Calendar.MONTH, 1);
            } else if (currentViewMode == ViewMode.WEEK) {
                currentCalendar.add(Calendar.WEEK_OF_YEAR, 1);
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
            if (isChecked) {
                if (checkedId == R.id.btn_month_view) {
                    currentViewMode = ViewMode.MONTH;
                } else if (checkedId == R.id.btn_week_view) {
                    currentViewMode = ViewMode.WEEK;
                } else if (checkedId == R.id.btn_day_view) {
                    currentViewMode = ViewMode.DAY;
                }
                updateCalendarView();
            }
        });

        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventEditActivity.class);
            if (selectedDay != null) {
                intent.putExtra("selected_date", selectedDay.getDate().getTime());
            }
            startActivity(intent);
        });
    }

    private void updateCalendarView() {
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

        // 更新标题
        tvMonthYear.setText(year + "年" + CalendarUtils.getMonthName(month));

        // 根据视图模式更新日历
        if (currentViewMode == ViewMode.MONTH) {
            calendarDays = CalendarUtils.getMonthDays(year, month);
            calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));
        } else if (currentViewMode == ViewMode.WEEK) {
            calendarDays = CalendarUtils.getWeekDays(year, month, day);
            calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));
        } else {
            // 日视图：只显示一天
            calendarDays = CalendarUtils.getWeekDays(year, month, day);
            calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        }

        // 加载事件到日历
        loadEventsForDays();

        // 设置适配器
        if (calendarAdapter == null) {
            calendarAdapter = new CalendarAdapter(calendarDays, this::onDayClick);
            calendarRecyclerView.setAdapter(calendarAdapter);
        } else {
            calendarAdapter.updateDays(calendarDays);
        }
    }

    private void loadEvents() {
        // 从数据库加载事件
        loadEventsForDays();
    }

    private void loadEventsForDays() {
        if (calendarDays == null || calendarDays.isEmpty()) {
            return;
        }

        // 获取所有事件
        List<CalendarEvent> allEvents = eventDatabase.getAllEvents();

        // 将事件分配到对应的日期
        for (CalendarDay day : calendarDays) {
            day.getEvents().clear();
            for (CalendarEvent event : allEvents) {
                if (CalendarUtils.isEventOnDay(event, day.getDate())) {
                    day.addEvent(event);
                }
            }
        }

        if (calendarAdapter != null) {
            calendarAdapter.notifyDataSetChanged();
        }
    }

    private void onDayClick(CalendarDay day) {
        // 取消之前选中的日期
        if (selectedDay != null) {
            selectedDay.setSelected(false);
        }

        // 选中新日期
        selectedDay = day;
        day.setSelected(true);
        calendarAdapter.notifyDataSetChanged();

        // 显示该日期的事件
        if (day.hasEvents()) {
            Intent intent = new Intent(this, EventListActivity.class);
            intent.putExtra("selected_date", day.getDate().getTime());
            startActivity(intent);
        } else {
            String dateStr = CalendarUtils.formatDate(day.getDate(), "yyyy年MM月dd日");
            Toast.makeText(this, dateStr + " 无日程", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新事件列表
        loadEvents();
    }
}
