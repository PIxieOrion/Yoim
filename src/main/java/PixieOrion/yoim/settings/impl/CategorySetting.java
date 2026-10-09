package PixieOrion.yoim.settings.impl;

import PixieOrion.yoim.settings.Setting;
import java.util.function.BooleanSupplier;

public final class CategorySetting extends Setting {
    private boolean open;

    public CategorySetting(String name, String description) {
        this(name, name, description, null);
    }

    public CategorySetting(String name, String tag, String description, BooleanSupplier visibility) {
        super(name, tag, description, visibility);
    }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
}
