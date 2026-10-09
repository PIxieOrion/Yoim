package PixieOrion.yoim.settings.impl;

import PixieOrion.yoim.settings.Setting;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class ModeSetting extends Setting {
    private String value;
    private final String defaultValue;
    private final List<String> modes;

    public ModeSetting(String name, String description, String value, String... modes) {
        this(name, name, description, null, value, modes);
    }

    public ModeSetting(String name, String tag, String description, BooleanSupplier visibility, String value, String... modes) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
        this.modes = Arrays.asList(modes);
    }

    public String getValue() { return value; }
    public List<String> getModes() { return modes; }
    public void setValue(String value) { if (modes.contains(value)) this.value = value; }
}
