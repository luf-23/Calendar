package com.example.myapplication.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Date;

public class EventEditActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etLocation, etDescription;
    private TextView tvStartTime, tvEndTime;
    private SwitchMaterial switchReminder;
    private Spinner spinnerReminder;
    private RadioGroup rgPriority;
    private Button btnDelete;
    private MaterialCardView cardReminderTime;
    private ImageButton btnCancel, btnSave;

    private Calendar startCalendar, endCalendar;
    private CalendarEvent currentEvent;
    private EventDatabase eventDatabase;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        initViews();
        initDatabase();
        initData();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_event_title);
        etLocation = findViewById(R.id.et_location);
        etDescription = findViewById(R.id.et_description);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        switchReminder = findViewById(R.id.switch_reminder);
        spinnerReminder = findViewById(R.id.spinner_reminder);
        rgPriority = findViewById(R.id.rg_priority);
        btnDelete = findViewById(R.id.btn_delete);
        cardReminderTime = findViewById(R.id.card_reminder_time);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);

        // 设置提醒时间选项
        String[] reminderOptions = {"提前5分钟", "提前15分钟", "提前30分钟", 
                                    "提前1小时", "提前1天"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, reminderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminder.setAdapter(adapter);
        spinnerReminder.setSelection(1); // 默认15分钟
    }

    private void initDatabase() {
        eventDatabase = new EventDatabase(this);
    }

    private void initData() {
        // 初始化时间
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.HOUR_OF_DAY, 1);

        // 检查是否是编辑模式
        long eventId = getIntent().getLongExtra("event_id", -1);
        if (eventId != -1) {
            isEditMode = true;
            currentEvent = eventDatabase.getEventById(eventId);
            loadEventData();
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            // 新建模式
            long selectedDate = getIntent().getLongExtra("selected_date", -1);
            if (selectedDate != -1) {
                startCalendar.setTimeInMillis(selectedDate);
                startCalendar.set(Calendar.HOUR_OF_DAY, 9);
                startCalendar.set(Calendar.MINUTE, 0);
                
                endCalendar.setTimeInMillis(selectedDate);
                endCalendar.set(Calendar.HOUR_OF_DAY, 10);
                endCalendar.set(Calendar.MINUTE, 0);
            }
            updateTimeDisplay();
        }
    }

    private void loadEventData() {
        if (currentEvent == null) return;

        etTitle.setText(currentEvent.getTitle());
        etLocation.setText(currentEvent.getLocation());
        etDescription.setText(currentEvent.getDescription());

        startCalendar.setTime(currentEvent.getStartTime());
        endCalendar.setTime(currentEvent.getEndTime());
        updateTimeDisplay();

        switchReminder.setChecked(currentEvent.isHasReminder());
        cardReminderTime.setVisibility(currentEvent.isHasReminder() ? View.VISIBLE : View.GONE);

        // 设置提醒时间
        int reminderMinutes = currentEvent.getReminderMinutes();
        int position = 1; // 默认15分钟
        if (reminderMinutes == 5) position = 0;
        else if (reminderMinutes == 15) position = 1;
        else if (reminderMinutes == 30) position = 2;
        else if (reminderMinutes == 60) position = 3;
        else if (reminderMinutes == 1440) position = 4;
        spinnerReminder.setSelection(position);

        // 设置优先级
        int priority = currentEvent.getPriority();
        if (priority == 1) {
            ((RadioButton) findViewById(R.id.rb_low)).setChecked(true);
        } else if (priority == 2) {
            ((RadioButton) findViewById(R.id.rb_medium)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.rb_high)).setChecked(true);
        }
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveEvent());

        findViewById(R.id.layout_start_time).setOnClickListener(v -> showDateTimePicker(true));

        findViewById(R.id.layout_end_time).setOnClickListener(v -> showDateTimePicker(false));

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cardReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void showDateTimePicker(boolean isStartTime) {
        Calendar calendar = isStartTime ? startCalendar : endCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // 选完日期后选择时间
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                updateTimeDisplay();
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateTimeDisplay() {
        tvStartTime.setText(CalendarUtils.formatDate(startCalendar.getTime(), "yyyy-MM-dd HH:mm"));
        tvEndTime.setText(CalendarUtils.formatDate(endCalendar.getTime(), "yyyy-MM-dd HH:mm"));
    }

    private void saveEvent() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startCalendar.after(endCalendar)) {
            Toast.makeText(this, "开始时间不能晚于结束时间", Toast.LENGTH_SHORT).show();
            return;
        }

        CalendarEvent event = isEditMode ? currentEvent : new CalendarEvent();
        event.setTitle(title);
        event.setDescription(etDescription.getText().toString().trim());
        event.setLocation(etLocation.getText().toString().trim());
        event.setStartTime(startCalendar.getTime());
        event.setEndTime(endCalendar.getTime());
        event.setHasReminder(switchReminder.isChecked());

        // 获取提醒时间
        if (switchReminder.isChecked()) {
            int position = spinnerReminder.getSelectedItemPosition();
            int[] minutes = {5, 15, 30, 60, 1440};
            event.setReminderMinutes(minutes[position]);
        }

        // 获取优先级
        int checkedId = rgPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_low) {
            event.setPriority(1);
        } else if (checkedId == R.id.rb_medium) {
            event.setPriority(2);
        } else {
            event.setPriority(3);
        }

        // 保存到数据库
        if (isEditMode) {
            eventDatabase.updateEvent(event);
            Toast.makeText(this, "日程已更新", Toast.LENGTH_SHORT).show();
        } else {
            long id = eventDatabase.addEvent(event);
            event.setId(id);
            Toast.makeText(this, "日程已添加", Toast.LENGTH_SHORT).show();
        }

        // 设置提醒
        if (event.isHasReminder()) {
            EventReminderManager.setReminder(this, event);
        }

        finish();
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("删除日程")
                .setMessage("确定要删除这个日程吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    eventDatabase.deleteEvent(currentEvent.getId());
                    EventReminderManager.cancelReminder(this, currentEvent);
                    Toast.makeText(this, "日程已删除", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
