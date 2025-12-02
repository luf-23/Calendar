package com.example.myapplication.calendar.model;

import java.io.Serializable;

/**
 * Attendee - 参与者
 * 基于 RFC 5545 ATTENDEE 属性
 */
public class Attendee implements Serializable {
    
    private String name;
    private String email;
    private AttendeeRole role;
    private AttendeeStatus status;
    private boolean rsvp;  // 是否需要回复
    
    public enum AttendeeRole {
        CHAIR,        // 主持人
        REQ_PARTICIPANT,  // 必需参与者
        OPT_PARTICIPANT,  // 可选参与者
        NON_PARTICIPANT   // 非参与者
    }
    
    public enum AttendeeStatus {
        NEEDS_ACTION,  // 需要操作
        ACCEPTED,      // 已接受
        DECLINED,      // 已拒绝
        TENTATIVE,     // 待定
        DELEGATED      // 已委托
    }
    
    public Attendee() {
        this.role = AttendeeRole.REQ_PARTICIPANT;
        this.status = AttendeeStatus.NEEDS_ACTION;
        this.rsvp = true;
    }
    
    public Attendee(String name, String email) {
        this();
        this.name = name;
        this.email = email;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public AttendeeRole getRole() {
        return role;
    }
    
    public void setRole(AttendeeRole role) {
        this.role = role;
    }
    
    public AttendeeStatus getStatus() {
        return status;
    }
    
    public void setStatus(AttendeeStatus status) {
        this.status = status;
    }
    
    public boolean isRsvp() {
        return rsvp;
    }
    
    public void setRsvp(boolean rsvp) {
        this.rsvp = rsvp;
    }
}
