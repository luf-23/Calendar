package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.calendar.CalendarActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppAdapter appAdapter;
    private List<AppItem> appItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initData();
        setupRecyclerView();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
    }

    private void initData() {
        appItems = new ArrayList<>();
        appItems.add(new AppItem("日历", R.drawable.ic_calendar, AppItem.AppType.CALENDAR));
        appItems.add(new AppItem("相册", R.drawable.ic_gallery, AppItem.AppType.GALLERY));
        appItems.add(new AppItem("备忘录", R.drawable.ic_notes, AppItem.AppType.NOTES));
        appItems.add(new AppItem("计算器", R.drawable.ic_calculator, AppItem.AppType.CALCULATOR));
        appItems.add(new AppItem("天气", R.drawable.ic_weather, AppItem.AppType.WEATHER));
        appItems.add(new AppItem("时钟", R.drawable.ic_clock, AppItem.AppType.CLOCK));
        appItems.add(new AppItem("待办", R.drawable.ic_todo, AppItem.AppType.TODO));
        appItems.add(new AppItem("音乐", R.drawable.ic_music, AppItem.AppType.MUSIC));
    }

    private void setupRecyclerView() {
        // 设置网格布局，每行4列（更符合手机布局）
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);

        // 设置适配器
        appAdapter = new AppAdapter(appItems, appItem -> {
            handleAppClick(appItem);
        });
        recyclerView.setAdapter(appAdapter);
    }

    private void handleAppClick(AppItem appItem) {
        switch (appItem.getType()) {
            case CALENDAR:
                // 打开日历应用
                Intent calendarIntent = new Intent(this, CalendarActivity.class);
                startActivity(calendarIntent);
                break;
            case GALLERY:
                Toast.makeText(this, "打开相册应用，可以浏览照片和视频", Toast.LENGTH_SHORT).show();
                break;
            case NOTES:
                Toast.makeText(this, "打开备忘录，可以记录重要信息", Toast.LENGTH_SHORT).show();
                break;
            case CALCULATOR:
                Toast.makeText(this, "打开计算器，可以进行计算", Toast.LENGTH_SHORT).show();
                break;
            case WEATHER:
                Toast.makeText(this, "打开天气应用，查看天气情况", Toast.LENGTH_SHORT).show();
                break;
            case CLOCK:
                Toast.makeText(this, "打开时钟应用，查看时间和闹钟", Toast.LENGTH_SHORT).show();
                break;
            case TODO:
                Toast.makeText(this, "打开待办事项，管理任务清单", Toast.LENGTH_SHORT).show();
                break;
            case MUSIC:
                Toast.makeText(this, "打开音乐播放器，享受音乐", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    

}