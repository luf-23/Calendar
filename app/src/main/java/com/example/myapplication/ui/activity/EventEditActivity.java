package com.example.myapplication.ui.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.data.model.CalendarEvent;
import com.example.myapplication.manager.EventManager;
import com.example.myapplication.util.ReminderScheduler;
import com.example.myapplication.util.BatteryOptimizationHelper;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日程编辑Activity
 */
public class EventEditActivity extends AppCompatActivity {
    
    private static final String TAG = "EventEditActivity";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_DATE = "event_date";
    public static final int RESULT_DELETED = 2;
    
    private TextInputEditText etTitle, etLocation, etDescription;
    private TextView tvTitleBar, tvDate, tvStartTime, tvEndTime;
    private LinearLayout layoutDate, layoutStartTime, layoutEndTime;
    private ChipGroup chipGroupType;
    private Button btnDelete;
    private ImageButton btnCancel, btnSave;
    
    // 提醒相关控件
    private com.google.android.material.switchmaterial.SwitchMaterial switchReminder;
    private com.google.android.material.switchmaterial.SwitchMaterial switchSound;
    private android.widget.Spinner spinnerReminder;
    private com.google.android.material.card.MaterialCardView cardReminderTime;
    private com.google.android.material.card.MaterialCardView cardSound;
    
    private EventManager eventManager;
    private ReminderScheduler reminderScheduler;
    private CalendarEvent currentEvent;
    private boolean isEditMode = false;
    
    private Calendar eventDate;
    private Calendar startTime;
    private Calendar endTime;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        
        // 请求通知权限（Android 13+）
        requestNotificationPermission();
        
        initViews();
        initData();
        setupListeners();
    }
    
    /**
     * 请求通知权限
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "没有通知权限，将无法显示提醒", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    private void initViews() {
        tvTitleBar = findViewById(R.id.tv_title);
        etTitle = findViewById(R.id.et_event_title);
        etLocation = findViewById(R.id.et_location);
        etDescription = findViewById(R.id.et_description);
        
        tvDate = findViewById(R.id.tv_date);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        
        layoutDate = findViewById(R.id.layout_date);
        layoutStartTime = findViewById(R.id.layout_start_time);
        layoutEndTime = findViewById(R.id.layout_end_time);
        
        chipGroupType = findViewById(R.id.chip_group_type);
        btnDelete = findViewById(R.id.btn_delete);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        
        // 提醒相关控件
        switchReminder = findViewById(R.id.switch_reminder);
        switchSound = findViewById(R.id.switch_sound);
        spinnerReminder = findViewById(R.id.spinner_reminder);
        cardReminderTime = findViewById(R.id.card_reminder_time);
        cardSound = findViewById(R.id.card_sound);
        
        // 设置提醒时间选项
        android.widget.ArrayAdapter<CharSequence> adapter = android.widget.ArrayAdapter.createFromResource(
                this, R.array.reminder_times, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminder.setAdapter(adapter);
    }
    
    private void initData() {
        eventManager = new EventManager(this);
        reminderScheduler = new ReminderScheduler(this);
        
        // 初始化时间
        eventDate = Calendar.getInstance();
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        
        // 检查是否是编辑模式
        long eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
        long dateMillis = getIntent().getLongExtra(EXTRA_EVENT_DATE, System.currentTimeMillis());
        
        if (eventId != -1) {
            // 编辑模式
            isEditMode = true;
            tvTitleBar.setText("编辑日程");
            btnDelete.setVisibility(View.VISIBLE);
            
            currentEvent = eventManager.getEvent(eventId);
            if (currentEvent != null) {
                loadEventData();
            }
        } else {
            // 新建模式
            isEditMode = false;
            tvTitleBar.setText("添加日程");
            btnDelete.setVisibility(View.GONE);
            
            // 设置默认日期
            eventDate.setTimeInMillis(dateMillis);
            
            // 设置默认时间（下一个整点）
            startTime.setTimeInMillis(dateMillis);
            startTime.set(Calendar.MINUTE, 0);
            startTime.set(Calendar.SECOND, 0);
            startTime.add(Calendar.HOUR_OF_DAY, 1);
            
            endTime.setTimeInMillis(startTime.getTimeInMillis());
            endTime.add(Calendar.HOUR_OF_DAY, 1);
            
            updateDateTimeDisplay();
        }
    }
    
    private void loadEventData() {
        etTitle.setText(currentEvent.getTitle());
        etLocation.setText(currentEvent.getLocation());
        etDescription.setText(currentEvent.getDescription());
        
        eventDate.setTime(currentEvent.getStartTime());
        startTime.setTime(currentEvent.getStartTime());
        endTime.setTime(currentEvent.getEndTime());
        
        // 设置事件类型
        switch (currentEvent.getType()) {
            case MEETING:
                chipGroupType.check(R.id.chip_meeting);
                break;
            case WORK:
                chipGroupType.check(R.id.chip_work);
                break;
            case PERSONAL:
                chipGroupType.check(R.id.chip_personal);
                break;
            case IMPORTANT:
                chipGroupType.check(R.id.chip_important);
                break;
            case OTHER:
                chipGroupType.check(R.id.chip_other);
                break;
        }
        
        // 加载提醒设置
        switchReminder.setChecked(currentEvent.isReminderEnabled());
        if (currentEvent.isReminderEnabled()) {
            cardReminderTime.setVisibility(View.VISIBLE);
            cardSound.setVisibility(View.VISIBLE);
            
            // 设置提醒时间选项
            CalendarEvent.ReminderTime reminderTime = 
                    CalendarEvent.ReminderTime.fromMinutes(currentEvent.getReminderMinutesBefore());
            spinnerReminder.setSelection(getReminderTimePosition(reminderTime));
            
            // 设置响铃开关
            switchSound.setChecked(currentEvent.isSoundEnabled());
        }
        
        updateDateTimeDisplay();
    }
    
    private int getReminderTimePosition(CalendarEvent.ReminderTime reminderTime) {
        CalendarEvent.ReminderTime[] times = CalendarEvent.ReminderTime.values();
        for (int i = 0; i < times.length; i++) {
            if (times[i] == reminderTime) {
                return i;
            }
        }
        return 0;
    }
    
    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());
        
        btnSave.setOnClickListener(v -> saveEvent());
        
        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
        
        layoutDate.setOnClickListener(v -> showDatePicker());
        
        layoutStartTime.setOnClickListener(v -> showTimePicker(true));
        
        layoutEndTime.setOnClickListener(v -> showTimePicker(false));
        
        // 提醒开关监听
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cardReminderTime.setVisibility(View.VISIBLE);
                cardSound.setVisibility(View.VISIBLE);
            } else {
                cardReminderTime.setVisibility(View.GONE);
                cardSound.setVisibility(View.GONE);
            }
        });
    }
    
    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    eventDate.set(year, month, dayOfMonth);
                    // 同步更新开始和结束时间的日期部分
                    startTime.set(year, month, dayOfMonth);
                    endTime.set(year, month, dayOfMonth);
                    updateDateTimeDisplay();
                },
                eventDate.get(Calendar.YEAR),
                eventDate.get(Calendar.MONTH),
                eventDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }
    
    private void showTimePicker(boolean isStartTime) {
        Calendar time = isStartTime ? startTime : endTime;
        
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    time.set(Calendar.MINUTE, minute);
                    
                    // 如果是开始时间，检查是否晚于结束时间
                    if (isStartTime && startTime.after(endTime)) {
                        endTime.setTimeInMillis(startTime.getTimeInMillis());
                        endTime.add(Calendar.HOUR_OF_DAY, 1);
                    }
                    
                    // 如果是结束时间，检查是否早于开始时间
                    if (!isStartTime && endTime.before(startTime)) {
                        Toast.makeText(this, "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show();
                        endTime.setTimeInMillis(startTime.getTimeInMillis());
                        endTime.add(Calendar.HOUR_OF_DAY, 1);
                    }
                    
                    updateDateTimeDisplay();
                },
                time.get(Calendar.HOUR_OF_DAY),
                time.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }
    
    private void updateDateTimeDisplay() {
        tvDate.setText(dateFormat.format(eventDate.getTime()));
        tvStartTime.setText(timeFormat.format(startTime.getTime()));
        tvEndTime.setText(timeFormat.format(endTime.getTime()));
    }
    
    private void saveEvent() {
        // 验证输入
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            etTitle.requestFocus();
            return;
        }
        
        // 获取事件类型
        CalendarEvent.EventType eventType = CalendarEvent.EventType.OTHER;
        int checkedId = chipGroupType.getCheckedChipId();
        if (checkedId == R.id.chip_meeting) {
            eventType = CalendarEvent.EventType.MEETING;
        } else if (checkedId == R.id.chip_work) {
            eventType = CalendarEvent.EventType.WORK;
        } else if (checkedId == R.id.chip_personal) {
            eventType = CalendarEvent.EventType.PERSONAL;
        } else if (checkedId == R.id.chip_important) {
            eventType = CalendarEvent.EventType.IMPORTANT;
        }
        
        // 创建或更新事件
        CalendarEvent event;
        if (isEditMode && currentEvent != null) {
            event = currentEvent;
        } else {
            event = new CalendarEvent(title, startTime.getTime(), endTime.getTime());
        }
        
        event.setTitle(title);
        event.setStartTime(startTime.getTime());
        event.setEndTime(endTime.getTime());
        event.setType(eventType);
        event.setLocation(etLocation.getText().toString().trim());
        event.setDescription(etDescription.getText().toString().trim());
        
        // 设置提醒
        boolean reminderEnabled = switchReminder.isChecked();
        event.setReminderEnabled(reminderEnabled);
        
        if (reminderEnabled) {
            // 验证提醒时间必须选择
            int selectedPosition = spinnerReminder.getSelectedItemPosition();
            CalendarEvent.ReminderTime[] reminderTimes = CalendarEvent.ReminderTime.values();
            if (selectedPosition >= 0 && selectedPosition < reminderTimes.length) {
                CalendarEvent.ReminderTime reminderTime = reminderTimes[selectedPosition];
                event.setReminderMinutesBefore(reminderTime.getMinutes());
            }
            
            // 设置响铃
            boolean soundEnabled = switchSound.isChecked();
            event.setSoundEnabled(soundEnabled);
        }
        
        // 取消旧的提醒（如果是编辑模式）
        if (isEditMode && currentEvent != null) {
            reminderScheduler.cancelReminder(currentEvent);
        }
        
        // 保存到数据库
        if (isEditMode) {
            eventManager.updateEvent(event);
            Toast.makeText(this, "日程已更新", Toast.LENGTH_SHORT).show();
        } else {
            eventManager.addEvent(event);
            Toast.makeText(this, "日程已添加", Toast.LENGTH_SHORT).show();
        }
        
        // 设置新的提醒
        if (reminderEnabled) {
            // 检查提醒时间是否已过
            if (event.getReminderTime() != null && 
                event.getReminderTime().getTime() < System.currentTimeMillis()) {
                Toast.makeText(this, "提醒时间已过，无法设置提醒", Toast.LENGTH_LONG).show();
            } else {
                // 检查精确闹钟权限
                if (!reminderScheduler.canScheduleExactAlarms()) {
                    Toast.makeText(this, "日程已保存，但需要精确闹钟权限才能设置提醒", Toast.LENGTH_LONG).show();
                } else {
                    reminderScheduler.scheduleReminder(event);
                    Toast.makeText(this, "日程和提醒已设置", Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        setResult(RESULT_OK);
        finish();
    }
    
    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("删除日程")
                .setMessage("确定要删除这个日程吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    if (currentEvent != null) {
                        // 取消提醒
                        if (currentEvent.isReminderEnabled()) {
                            reminderScheduler.cancelReminder(currentEvent);
                        }
                        eventManager.deleteEvent(currentEvent.getId());
                        Toast.makeText(this, "日程已删除", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_DELETED);
                        finish();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
