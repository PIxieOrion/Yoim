package PixieOrion.yoim.settings.impl;

import PixieOrion.yoim.settings.Setting;
import java.util.function.BooleanSupplier;

public final class NumberSetting extends Setting {
    private Number value;
    private final Number defaultValue;
    private final Number minimum;
    private final Number maximum;

    public NumberSetting(String name, String description, Number value, Number minimum, Number maximum) {
        this(name, name, description, null, value, minimum, maximum);
    }

    public NumberSetting(String name, String tag, String description, Number value, Number minimum, Number maximum) {
        this(name, tag, description, null, value, minimum, maximum);
    }

    public NumberSetting(String name, String tag, String description, BooleanSupplier visibility, Number value, Number minimum, Number maximum) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Number getValue() { return value; }
    public Number getDefaultValue() { return defaultValue; }
    public Number getMinimum() { return minimum; }
    public Number getMaximum() { return maximum; }

    public enum Type { INTEGER, LONG, DOUBLE, FLOAT }

    public Type getType() {
        if (defaultValue instanceof Long) return Type.LONG;
        if (defaultValue instanceof Double) return Type.DOUBLE;
        if (defaultValue instanceof Float) return Type.FLOAT;
        return Type.INTEGER;
    }

    public void setValue(Number value) {
        switch (getType()) {
            case LONG -> this.value = Math.max(minimum.longValue(), Math.min(maximum.longValue(), value.longValue()));
            case DOUBLE -> this.value = Math.max(minimum.doubleValue(), Math.min(maximum.doubleValue(), value.doubleValue()));
            case FLOAT -> this.value = Math.max(minimum.floatValue(), Math.min(maximum.floatValue(), value.floatValue()));
            default -> this.value = Math.max(minimum.intValue(), Math.min(maximum.intValue(), value.intValue()));
        }
    }
}
