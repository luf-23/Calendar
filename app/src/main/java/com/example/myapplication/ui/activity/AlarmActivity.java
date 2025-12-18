package com.example.myapplication.ui.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 响铃Activity
 * 当日程提醒开启响铃时显示此界面，播放铃声1分钟
 */
public class AlarmActivity extends AppCompatActivity {
    
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable stopAlarmRunnable;
    
    private TextView tvTitle;
    private TextView tvTime;
    private TextView tvDescription;
    private TextView tvLocation;
    private Button btnStop;
    private Button btnSnooze;
    
    private static final long ALARM_DURATION = 60 * 1000; // 1分钟
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置在锁屏上显示
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }
        
        setContentView(R.layout.activity_alarm);
        
        initViews();
        loadEventData();
        startAlarm();
    }
    
    private void initViews() {
        tvTitle = findViewById(R.id.tv_alarm_title);
        tvTime = findViewById(R.id.tv_alarm_time);
        tvDescription = findViewById(R.id.tv_alarm_description);
        tvLocation = findViewById(R.id.tv_alarm_location);
        btnStop = findViewById(R.id.btn_stop_alarm);
        btnSnooze = findViewById(R.id.btn_snooze);
        
        btnStop.setOnClickListener(v -> stopAlarm());
        btnSnooze.setOnClickListener(v -> snoozeAlarm());
    }
    
    private void loadEventData() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("event_title");
        String description = intent.getStringExtra("event_description");
        String location = intent.getStringExtra("event_location");
        long startTime = intent.getLongExtra("event_start_time", 0);
        
        tvTitle.setText(title != null ? title : "日程提醒");
        
        if (startTime > 0) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
            tvTime.setText("时间: " + timeFormat.format(new Date(startTime)));
            tvTime.setVisibility(View.VISIBLE);
        } else {
            tvTime.setVisibility(View.GONE);
        }
        
        if (description != null && !description.isEmpty()) {
            tvDescription.setText(description);
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }
        
        if (location != null && !location.isEmpty()) {
            tvLocation.setText("📍 " + location);
            tvLocation.setVisibility(View.VISIBLE);
        } else {
            tvLocation.setVisibility(View.GONE);
        }
    }
    
    private void startAlarm() {
        try {
            // 使用系统默认铃声
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, alarmUri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            // 1分钟后自动停止
            handler = new Handler();
            stopAlarmRunnable = this::stopAlarm;
            handler.postDelayed(stopAlarmRunnable, ALARM_DURATION);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        if (handler != null && stopAlarmRunnable != null) {
            handler.removeCallbacks(stopAlarmRunnable);
        }
        
        finish();
    }
    
    private void snoozeAlarm() {
        // 稍后提醒（5分钟）
        stopAlarm();
        // 这里可以添加延后提醒的逻辑
        // 暂时直接关闭
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        if (handler != null && stopAlarmRunnable != null) {
            handler.removeCallbacks(stopAlarmRunnable);
        }
    }
    
    @Override
    public void onBackPressed() {
        // 防止通过返回键关闭，必须点击按钮停止
        // 不调用 super.onBackPressed()
    }
}
