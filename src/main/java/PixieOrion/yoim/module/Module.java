package PixieOrion.yoim.module;

import PixieOrion.yoim.settings.Setting;
import PixieOrion.yoim.settings.impl.BindSetting;
import PixieOrion.yoim.settings.impl.BooleanSetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    private final String name;
    private final Category category;
    private final String description;
    private boolean enabled;
    private final List<Setting> settings = new ArrayList<>();
    public final BooleanSetting chatNotify;
    public final BooleanSetting drawn;
    public final BindSetting bind;

    protected Module(String name, Category category) {
        this(name, category, "");
    }

    protected Module(String name, Category category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.chatNotify = new BooleanSetting("ChatNotify(Soon)", "Planned feature: module state notifications in chat.", true);
        this.drawn = new BooleanSetting("Drawn", "Show the module in the future HUD list.", true);
        this.bind = new BindSetting("Bind", "The keybind that toggles the module.", 0);
        settings.add(chatNotify);
        settings.add(drawn);
        settings.add(bind);
    }

    protected final <T extends Setting> T setting(T setting) {
        settings.add(setting);
        return setting;
    }

    public final String getName() { return name; }
    public final Category getCategory() { return category; }
    public final String getDescription() { return description; }
    public final boolean isEnabled() { return enabled; }
    public final int getBind() { return bind.getValue(); }
    public final void setBind(int bind) { this.bind.setValue(bind); }
    public final List<Setting> getSettings() { return Collections.unmodifiableList(settings); }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        if (!enabled && !canDisable()) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public final void toggle() { setEnabled(!enabled); }
    protected boolean canDisable() { return true; }
    protected void onEnable() {}
    protected void onDisable() {}
}
