package PixieOrion.yoim.module.client;

import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.impl.BooleanSetting;
import PixieOrion.yoim.settings.impl.ColorSetting;

import java.awt.Color;

public final class ColorsModule extends Module {
    public final ColorSetting color = setting(new ColorSetting("Color", "Global Yoim accent color", new Color(255, 215, 0, 255)));
    public final BooleanSetting sync = setting(new BooleanSetting("Sync", "Synchronize the global color with the ClickGUI", true));

    public ColorsModule() {
        super("Colors", Category.CLIENT, "Controls the global accent color and synchronization.");
        setEnabled(true);
    }

    @Override
    protected boolean canDisable() { return false; }

    public static Color getGlobalColor() {
        ColorsModule module = PixieOrion.yoim.Yoim.MODULES.get(ColorsModule.class);
        if (module == null) return new Color(255, 215, 0, 255);
        return module.color.getColor();
    }

    public static boolean isSyncEnabled() {
        ColorsModule module = PixieOrion.yoim.Yoim.MODULES.get(ColorsModule.class);
        return module != null && module.isEnabled() && module.sync.getValue();
    }
}
