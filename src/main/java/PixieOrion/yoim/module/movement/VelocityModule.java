package PixieOrion.yoim.module.movement;

import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.impl.BooleanSetting;
import PixieOrion.yoim.settings.impl.ModeSetting;
import PixieOrion.yoim.settings.impl.NumberSetting;

public final class VelocityModule extends Module {
    public final ModeSetting mode = setting(new ModeSetting("Mode", "How incoming knockback is handled.", "Cancel", "Normal", "Cancel"));
    public final NumberSetting horizontal = setting(new NumberSetting("Horizontal", "Horizontal", "Horizontal knockback amount.", () -> mode.getValue().equals("Normal"), 0, 0, 100));
    public final NumberSetting vertical = setting(new NumberSetting("Vertical", "Vertical", "Vertical knockback amount.", () -> mode.getValue().equals("Normal"), 0, 0, 100));
    public final BooleanSetting explosions = setting(new BooleanSetting("Explosions", "Modifies knockback received from explosions.", true));

    public VelocityModule() {
        super("Velocity", Category.MOVEMENT, "Modifies the amount of knockback that you receive.");
    }
}
