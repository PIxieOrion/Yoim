package PixieOrion.yoim.settings.impl;

import PixieOrion.yoim.settings.Setting;
import java.util.function.BooleanSupplier;

public final class StringSetting extends Setting {
    private String value;
    private final String defaultValue;

    public StringSetting(String name, String description, String value) {
        this(name, name, description, null, value);
    }

    public StringSetting(String name, String tag, String description, String value) {
        this(name, tag, description, null, value);
    }

    public StringSetting(String name, String tag, String description, BooleanSupplier visibility, String value) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
    }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDefaultValue() { return defaultValue; }
}
