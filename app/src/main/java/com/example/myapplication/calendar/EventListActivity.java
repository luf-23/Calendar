package com.example.myapplication.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventListAdapter adapter;
    private TextView tvDate;
    private ImageButton btnBack, btnAdd;
    private LinearLayout emptyView;

    private EventDatabase eventDatabase;
    private Date selectedDate;
    private List<CalendarEvent> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        initViews();
        initData();
        setupListeners();
        loadEvents();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_events);
        tvDate = findViewById(R.id.tv_date);
        btnBack = findViewById(R.id.btn_back);
        btnAdd = findViewById(R.id.btn_add);
        emptyView = findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {
        eventDatabase = new EventDatabase(this);

        long dateMillis = getIntent().getLongExtra("selected_date", -1);
        if (dateMillis != -1) {
            selectedDate = new Date(dateMillis);
            tvDate.setText(CalendarUtils.formatDate(selectedDate, "yyyy年MM月dd日"));
        } else {
            selectedDate = new Date();
            tvDate.setText("日程列表");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventEditActivity.class);
            intent.putExtra("selected_date", selectedDate.getTime());
            startActivity(intent);
        });
    }

    private void loadEvents() {
        // 获取当天的事件
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(selectedDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(selectedDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);

        events = eventDatabase.getEventsBetween(startCal.getTime(), endCal.getTime());

        if (events.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            adapter = new EventListAdapter(events, this::onEventClick);
            recyclerView.setAdapter(adapter);
        }
    }

    private void onEventClick(CalendarEvent event) {
        Intent intent = new Intent(this, EventEditActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }
}
