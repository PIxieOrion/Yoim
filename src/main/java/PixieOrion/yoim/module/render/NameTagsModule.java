package PixieOrion.yoim.module.render;

import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.impl.BooleanSetting;
import PixieOrion.yoim.settings.impl.NumberSetting;

public final class NameTagsModule extends Module {
    public final BooleanSetting health = setting(new BooleanSetting("Health", "Append player health to the custom name tag.", true));
    public final BooleanSetting ping = setting(new BooleanSetting("Ping", "Append player latency to the custom name tag.", true));
    public final BooleanSetting items = setting(new BooleanSetting("Items", "Render armor and hand item icons above the name tag.", true));
    public final BooleanSetting durability = setting(new BooleanSetting("Durability", "Show durability percentages for damageable gear.", true));
    public final NumberSetting scale = setting(new NumberSetting("Scale", "Controls size at distance, like Lonks.", 30, 10, 100));

    public NameTagsModule() {
        super("NameTags", Category.RENDER, "World-space billboard name tags, independent of vanilla name-label distance checks.");
    }
}
