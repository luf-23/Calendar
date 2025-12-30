package com.example.myapplication.ui.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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
import java.util.UUID;

public class CalendarActivity extends AppCompatActivity {

    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TextView tvMonthYear;
    private ImageButton btnPrevMonth, btnNextMonth, btnMoreOptions;
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
    
    // 文件选择器
    private ActivityResultLauncher<String> exportFileLauncher;
    private ActivityResultLauncher<String[]> importFileLauncher;
    
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

        initFileLaunchers();
        initViews();
        initCalendar();
        setupListeners();
    }
    
    /**
     * 初始化文件选择器
     */
    private void initFileLaunchers() {
        // 导出文件选择器
        exportFileLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/calendar"),
            uri -> {
                if (uri != null) {
                    performExport(uri);
                }
            }
        );
        
        // 导入文件选择器
        importFileLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    performImport(uri);
                }
            }
        );
    }

    private void initViews() {
        calendarRecyclerView = findViewById(R.id.calendar_recycler_view);
        tvMonthYear = findViewById(R.id.tv_month_year);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        btnMoreOptions = findViewById(R.id.btn_more_options);
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
        btnMoreOptions.setOnClickListener(this::showOverflowMenu);
        
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

    private void showOverflowMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_calendar_overflow, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_jump_to_date) {
                showDatePickerDialog();
                return true;
            } else if (itemId == R.id.action_import_export) {
                showImportExportDialog();
                return true;
            } else if (itemId == R.id.action_settings) {
                // 设置入口占位，无需处理点击
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showDatePickerDialog() {
        Calendar defaultDate = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth, 0, 0, 0);
                selectedDate.set(Calendar.MILLISECOND, 0);

                currentCalendar = (Calendar) selectedDate.clone();
                if (currentViewMode == ViewMode.MONTH) {
                    updateCalendar();
                } else {
                    toggleViewMode.check(R.id.btn_month_view);
                }

                for (CalendarDay day : calendarDays) {
                    if (CalendarUtils.isSameDay(day.getCalendar(), selectedDate)) {
                        onDaySelected(day);
                        break;
                    }
                }
            },
            defaultDate.get(Calendar.YEAR),
            defaultDate.get(Calendar.MONTH),
            defaultDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_import_export) {
            showImportExportDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 显示导入导出对话框
     */
    private void showImportExportDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_import_export, null);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .create();
        
        android.widget.Button btnExport = dialogView.findViewById(R.id.btn_export);
        android.widget.Button btnImport = dialogView.findViewById(R.id.btn_import);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        
        btnExport.setOnClickListener(v -> {
            dialog.dismiss();
            startExport();
        });
        
        btnImport.setOnClickListener(v -> {
            dialog.dismiss();
            startImport();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * 开始导出
     */
    private void startExport() {
        // 生成文件名：日程-uuid(8-12位).ics
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String fileName = "日程-" + uuid + ".ics";
        
        exportFileLauncher.launch(fileName);
    }
    
    /**
     * 执行导出
     */
    private void performExport(Uri uri) {
        Toast.makeText(this, "正在导出...", Toast.LENGTH_SHORT).show();
        
        eventManager.exportEventsToIcs(uri, (success, eventCount) -> {
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, 
                        String.format("成功导出 %d 个事件", eventCount), 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
    /**
     * 开始导入
     */
    private void startImport() {
        importFileLauncher.launch(new String[]{"text/calendar", "text/*", "*/*"});
    }
    
    /**
     * 执行导入
     */
    private void performImport(Uri uri) {
        // 显示确认对话框
        new AlertDialog.Builder(this)
            .setTitle("确认导入")
            .setMessage("导入的事件将添加到现有日历中。是否继续？")
            .setPositiveButton("导入", (dialog, which) -> {
                Toast.makeText(this, "正在导入...", Toast.LENGTH_SHORT).show();
                
                eventManager.importEventsFromIcs(uri, (success, eventCount) -> {
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, 
                                String.format("成功导入 %d 个事件", eventCount), 
                                Toast.LENGTH_LONG).show();
                            
                            // 刷新日历
                            updateCalendar();
                            if (currentViewMode == ViewMode.DAY) {
                                updateDaySchedule();
                            }
                        } else {
                            Toast.makeText(this, "导入失败或文件中没有有效事件", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            })
            .setNegativeButton("取消", null)
            .show();
    }
}
