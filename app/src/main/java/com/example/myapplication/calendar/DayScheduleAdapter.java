package com.example.myapplication.calendar;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 日程列表适配器
 */
public class DayScheduleAdapter extends RecyclerView.Adapter<DayScheduleAdapter.EventViewHolder> {
    
    private List<CalendarEvent> events;
    private OnEventClickListener listener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    
    public interface OnEventClickListener {
        void onEventClick(CalendarEvent event);
    }
    
    public DayScheduleAdapter(List<CalendarEvent> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_event, parent, false);
        return new EventViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);
        
        // 设置时间
        String timeText = timeFormat.format(event.getStartTime()) + " - " + timeFormat.format(event.getEndTime());
        holder.tvTime.setText(timeText);
        
        // 设置标题
        holder.tvTitle.setText(event.getTitle());
        
        // 设置描述
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(event.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }
        
        // 设置地点
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            holder.tvLocation.setVisibility(View.VISIBLE);
            holder.tvLocation.setText("📍 " + event.getLocation());
        } else {
            holder.tvLocation.setVisibility(View.GONE);
        }
        
        // 设置类型标签
        holder.tvType.setText(event.getType().getName());
        holder.tvType.setBackgroundColor(event.getColor());
        
        // 设置卡片左侧颜色条
        holder.colorBar.setBackgroundColor(event.getColor());
        
        // 计算时长
        long duration = event.getDurationMinutes();
        holder.tvDuration.setText(duration + "分钟");
        
        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }
    
    public void updateEvents(List<CalendarEvent> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }
    
    static class EventViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        View colorBar;
        TextView tvTime;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvLocation;
        TextView tvType;
        TextView tvDuration;
        
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            colorBar = itemView.findViewById(R.id.color_bar);
            tvTime = itemView.findViewById(R.id.tv_event_time);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvDescription = itemView.findViewById(R.id.tv_event_description);
            tvLocation = itemView.findViewById(R.id.tv_event_location);
            tvType = itemView.findViewById(R.id.tv_event_type);
            tvDuration = itemView.findViewById(R.id.tv_event_duration);
        }
    }
}
