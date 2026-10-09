package PixieOrion.yoim.module.client;

import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.impl.BooleanSetting;
import PixieOrion.yoim.settings.impl.NumberSetting;

public final class ArrayListModule extends Module {
    public final BooleanSetting background = setting(new BooleanSetting("Background", "Draw a translucent background behind each entry.", true));
    public final BooleanSetting accent = setting(new BooleanSetting("Accent", "Draw a global-color accent strip.", true));
    public final NumberSetting offset = setting(new NumberSetting("Offset", "Right and top screen margin.", 4, 0, 20));

    public ArrayListModule() {
        super("ArrayList", Category.CLIENT, "Shows enabled modules on the HUD. Respects each module's Drawn setting.");
    }
}
