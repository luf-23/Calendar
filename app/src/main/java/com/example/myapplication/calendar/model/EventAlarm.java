package com.example.myapplication.calendar.model;

import java.io.Serializable;

/**
 * EventAlarm - 事件提醒
 * 基于 RFC 5545 VALARM 组件
 */
public class EventAlarm implements Serializable {
    
    private long id;
    private long eventId;
    private AlarmAction action;  // RFC 5545: ACTION
    private TriggerType triggerType;
    private int minutesBefore;  // 提前多少分钟提醒
    private String description;  // RFC 5545: DESCRIPTION
    private int repeat;  // RFC 5545: REPEAT
    private int duration;  // RFC 5545: DURATION (重复间隔，秒)
    
    /**
     * 提醒动作类型
     */
    public enum AlarmAction {
        AUDIO,    // 音频提醒
        DISPLAY,  // 显示提醒
        EMAIL     // 邮件提醒
    }
    
    /**
     * 触发器类型
     */
    public enum TriggerType {
        RELATIVE,  // 相对时间（如提前15分钟）
        ABSOLUTE   // 绝对时间
    }
    
    public EventAlarm() {
        this.action = AlarmAction.DISPLAY;
        this.triggerType = TriggerType.RELATIVE;
        this.minutesBefore = 15;
        this.repeat = 0;
        this.duration = 0;
    }
    
    public EventAlarm(int minutesBefore) {
        this();
        this.minutesBefore = minutesBefore;
    }
    
    /**
     * 创建标准提醒选项
     */
    public static EventAlarm createStandard(int minutesBefore) {
        EventAlarm alarm = new EventAlarm();
        alarm.setMinutesBefore(minutesBefore);
        alarm.setDescription(getStandardDescription(minutesBefore));
        return alarm;
    }
    
    private static String getStandardDescription(int minutes) {
        if (minutes == 0) {
            return "事件开始时";
        } else if (minutes < 60) {
            return "提前" + minutes + "分钟";
        } else if (minutes < 1440) {
            return "提前" + (minutes / 60) + "小时";
        } else {
            return "提前" + (minutes / 1440) + "天";
        }
    }
    
    /**
     * 转换为 RFC 5545 VALARM 字符串
     */
    public String toVAlarmString() {
        StringBuilder valarm = new StringBuilder("BEGIN:VALARM\n");
        valarm.append("ACTION:").append(action.name()).append("\n");
        
        if (triggerType == TriggerType.RELATIVE) {
            valarm.append("TRIGGER:-PT").append(minutesBefore).append("M\n");
        }
        
        if (description != null && !description.isEmpty()) {
            valarm.append("DESCRIPTION:").append(description).append("\n");
        }
        
        if (repeat > 0) {
            valarm.append("REPEAT:").append(repeat).append("\n");
            valarm.append("DURATION:PT").append(duration).append("S\n");
        }
        
        valarm.append("END:VALARM\n");
        return valarm.toString();
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getEventId() {
        return eventId;
    }
    
    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
    
    public AlarmAction getAction() {
        return action;
    }
    
    public void setAction(AlarmAction action) {
        this.action = action;
    }
    
    public TriggerType getTriggerType() {
        return triggerType;
    }
    
    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }
    
    public int getMinutesBefore() {
        return minutesBefore;
    }
    
    public void setMinutesBefore(int minutesBefore) {
        this.minutesBefore = minutesBefore;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getRepeat() {
        return repeat;
    }
    
    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
}
