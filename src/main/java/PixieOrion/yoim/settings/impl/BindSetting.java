package PixieOrion.yoim.settings.impl;

import PixieOrion.yoim.settings.Setting;
import java.util.function.BooleanSupplier;

public final class BindSetting extends Setting {
    private int value;
    private final int defaultValue;

    public BindSetting(String name, String description, int value) {
        this(name, name, description, null, value);
    }

    public BindSetting(String name, String tag, String description, int value) {
        this(name, tag, description, null, value);
    }

    public BindSetting(String name, String tag, String description, BooleanSupplier visibility, int value) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
    }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
