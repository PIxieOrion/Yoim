package PixieOrion.yoim.module.client;

import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.impl.BooleanSetting;
import PixieOrion.yoim.settings.impl.NumberSetting;
import org.lwjgl.glfw.GLFW;

public final class ClickGuiModule extends Module {
    public final BooleanSetting sounds = setting(new BooleanSetting("Sounds", "Play ClickGUI click sounds.", true));
    public final NumberSetting scrollSpeed = setting(new NumberSetting("Scroll Speed", "Frame scroll amount.", 8, 1, 30));

    public ClickGuiModule() {
        super("ClickGUI", Category.CLIENT, "Yoim ClickGUI and interface controls.");
        setBind(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}
