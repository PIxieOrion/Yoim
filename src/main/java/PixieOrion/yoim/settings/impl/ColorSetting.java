package PixieOrion.yoim.settings.impl;

import PixieOrion.yoim.settings.Setting;
import java.awt.Color;
import java.util.function.BooleanSupplier;

public final class ColorSetting extends Setting {
    private Color value;
    private final Color defaultValue;
    private boolean sync;
    private boolean rainbow;

    public ColorSetting(String name, String description, Color value) {
        this(name, name, description, null, value);
    }

    public ColorSetting(String name, String tag, String description, Color value) {
        this(name, tag, description, null, value);
    }

    public ColorSetting(String name, String tag, String description, BooleanSupplier visibility, Color value) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
    }

    public Color getColor() { return value; }
    public void setColor(Color color) { this.value = color; }
    public int getAlpha() { return value.getAlpha(); }
    public boolean isSync() { return sync; }
    public boolean isRainbow() { return rainbow; }
    public void setSync(boolean sync) { this.sync = sync; }
    public void setRainbow(boolean rainbow) { this.rainbow = rainbow; }
}
