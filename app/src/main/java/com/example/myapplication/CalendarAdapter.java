package com.example.myapplication;

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

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private List<CalendarDay> days;
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    public CalendarAdapter(List<CalendarDay> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //在钩子函数中创建ViewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        
        // 如果是年视图（12个项目），显示月份名称
        if (days.size() == 12) {
            String[] monthNames = {"1月", "2月", "3月", "4月", "5月", "6月", 
                                   "7月", "8月", "9月", "10月", "11月", "12月"};
            holder.tvDayNumber.setText(monthNames[position]);
        } else {
            holder.tvDayNumber.setText(String.valueOf(day.getDay()));
        }
        
        // 设置当前月份的日期样式
        if (day.isCurrentMonth()) {
            holder.tvDayNumber.setTextColor(Color.parseColor("#333333"));
            holder.tvDayNumber.setAlpha(1.0f);
        } else {
            holder.tvDayNumber.setTextColor(Color.parseColor("#CCCCCC"));
            holder.tvDayNumber.setAlpha(0.5f);
        }
        
        // 高亮今天
        if (day.isToday()) {
            holder.dayCard.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
            holder.tvDayNumber.setTextColor(Color.parseColor("#2196F3"));
            holder.tvDayNumber.setTextSize(18);
        } else {
            holder.dayCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.tvDayNumber.setTextSize(16);
        }
        
        // 选中状态
        if (day.isSelected()) {
            holder.dayCard.setCardBackgroundColor(Color.parseColor("#2196F3"));
            holder.tvDayNumber.setTextColor(Color.parseColor("#FFFFFF"));
        }

        // 显示事件指示器（绿点）
        if (day.hasEvents() && days.size() != 12) { // 不在年视图显示
            holder.eventIndicator.setVisibility(View.VISIBLE);
            if (day.getEventCount() > 1) {
                holder.tvEventCount.setText(String.valueOf(day.getEventCount()));
                holder.tvEventCount.setVisibility(View.VISIBLE);
            } else {
                holder.tvEventCount.setVisibility(View.GONE);
            }
        } else {
            holder.eventIndicator.setVisibility(View.GONE);
            holder.tvEventCount.setVisibility(View.GONE);
        }

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void updateDays(List<CalendarDay> newDays) {
        this.days = newDays;
        notifyDataSetChanged();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView dayCard;
        TextView tvDayNumber;
        View eventIndicator;
        TextView tvEventCount;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayCard = itemView.findViewById(R.id.day_card);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            eventIndicator = itemView.findViewById(R.id.event_indicator);
            tvEventCount = itemView.findViewById(R.id.tv_event_count);
        }
    }
}
