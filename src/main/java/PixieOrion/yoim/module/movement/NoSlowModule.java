package PixieOrion.yoim.module.movement;

import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.impl.BooleanSetting;

public final class NoSlowModule extends Module {
    public final BooleanSetting items = setting(new BooleanSetting("Items", "Removes item-use movement slowdown.", true));
    public final BooleanSetting soulSand = setting(new BooleanSetting("SoulSand", "Removes soul sand slowdown.", false));
    public final BooleanSetting slimeBlocks = setting(new BooleanSetting("SlimeBlocks", "Removes slime block slowdown.", false));
    public final BooleanSetting honeyBlocks = setting(new BooleanSetting("HoneyBlocks", "Removes honey block slowdown.", false));

    public NoSlowModule() {
        super("NoSlow", Category.MOVEMENT, "Removes movement slowdown.");
    }

    public boolean shouldSlow() {
        return false;
    }
}
