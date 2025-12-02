package com.example.myapplication.calendar;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {

    private List<CalendarEvent> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(CalendarEvent event);
    }

    public EventListAdapter(List<CalendarEvent> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvTime.setText(CalendarUtils.formatDate(event.getStartTime(), "HH:mm") +
                " - " + CalendarUtils.formatDate(event.getEndTime(), "HH:mm"));

        // 地点
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            holder.layoutLocation.setVisibility(View.VISIBLE);
            holder.tvLocation.setText(event.getLocation());
        } else {
            holder.layoutLocation.setVisibility(View.GONE);
        }

        // 描述
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(event.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // 提醒图标
        holder.ivReminder.setVisibility(event.isHasReminder() ? View.VISIBLE : View.GONE);

        // 优先级颜色
        int priorityColor;
        switch (event.getPriority()) {
            case 1: // 低
                priorityColor = Color.parseColor("#8BC34A");
                break;
            case 3: // 高
                priorityColor = Color.parseColor("#F44336");
                break;
            default: // 中
                priorityColor = Color.parseColor("#2196F3");
                break;
        }
        holder.priorityIndicator.setBackgroundColor(priorityColor);

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
        TextView tvTitle, tvTime, tvLocation, tvDescription;
        View priorityIndicator;
        ImageView ivReminder;
        LinearLayout layoutLocation;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvTime = itemView.findViewById(R.id.tv_event_time);
            tvLocation = itemView.findViewById(R.id.tv_event_location);
            tvDescription = itemView.findViewById(R.id.tv_event_description);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            ivReminder = itemView.findViewById(R.id.iv_reminder);
            layoutLocation = itemView.findViewById(R.id.layout_location);
        }
    }
}
