package PixieOrion.yoim.settings;

import java.util.function.BooleanSupplier;

public abstract class Setting {
    private final String name;
    private final String tag;
    private final String description;
    private boolean visible = true;
    private final BooleanSupplier visibility;

    protected Setting(String name, String tag, String description) {
        this(name, tag, description, null);
    }

    protected Setting(String name, String tag, String description, BooleanSupplier visibility) {
        this.name = name;
        this.tag = tag;
        this.description = description;
        this.visibility = visibility;
    }

    public String getName() { return name; }
    public String getTag() { return tag; }
    public String getDescription() { return description; }

    public void updateVisibility() {
        visible = visibility == null || visibility.getAsBoolean();
    }

    public boolean isVisible() { return visible; }
}
