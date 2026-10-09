package PixieOrion.yoim.settings.impl;

import PixieOrion.yoim.settings.Setting;
import java.util.function.BooleanSupplier;

public final class BooleanSetting extends Setting {
    private boolean value;
    private final boolean defaultValue;

    public BooleanSetting(String name, String description, boolean value) {
        this(name, name, description, null, value);
    }

    public BooleanSetting(String name, String tag, String description, boolean value) {
        this(name, tag, description, null, value);
    }

    public BooleanSetting(String name, String tag, String description, BooleanSupplier visibility, boolean value) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
    }

    public boolean getValue() { return value; }
    public void setValue(boolean value) { this.value = value; }
    public void toggle() { value = !value; }
    public boolean getDefaultValue() { return defaultValue; }
}
