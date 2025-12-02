package com.example.myapplication;

public class AppItem {
    private String name;
    private int iconResId;
    private AppType type;

    public enum AppType {
        CALENDAR,
        GALLERY,
        NOTES,
        CALCULATOR,
        WEATHER,
        CLOCK,
        TODO,
        MUSIC
    }

    public AppItem(String name, int iconResId, AppType type) {
        this.name = name;
        this.iconResId = iconResId;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public AppType getType() {
        return type;
    }

    public void setType(AppType type) {
        this.type = type;
    }
}
