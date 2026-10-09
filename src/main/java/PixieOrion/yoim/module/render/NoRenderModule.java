package PixieOrion.yoim.module.render;

import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.impl.BooleanSetting;

public final class NoRenderModule extends Module {
    public final BooleanSetting hurtCamera = setting(new BooleanSetting("HurtCamera", "Disables the hurt camera effect.", true));
    public final BooleanSetting explosions = setting(new BooleanSetting("Explosions", "Disables explosion particles.", true));
    public final BooleanSetting fireOverlay = setting(new BooleanSetting("FireOverlay", "Disables the fire overlay.", true));
    public final BooleanSetting blockOverlay = setting(new BooleanSetting("BlockOverlay", "Disables the block suffocation overlay.", false));
    public final BooleanSetting liquidOverlay = setting(new BooleanSetting("LiquidOverlay", "Disables the liquid overlay.", false));
    public final BooleanSetting snowOverlay = setting(new BooleanSetting("SnowOverlay", "Disables the snow overlay.", false));
    public final BooleanSetting pumpkinOverlay = setting(new BooleanSetting("PumpkinOverlay", "Disables the pumpkin overlay.", true));
    public final BooleanSetting portalOverlay = setting(new BooleanSetting("PortalOverlay", "Disables the portal overlay.", false));
    public final BooleanSetting totemAnimation = setting(new BooleanSetting("TotemAnimation", "Disables the totem animation.", false));
    public final BooleanSetting bossBar = setting(new BooleanSetting("BossBar", "Disables boss bars.", false));
    public final BooleanSetting vignette = setting(new BooleanSetting("Vignette", "Disables the vignette.", true));
    public final BooleanSetting blindness = setting(new BooleanSetting("Blindness", "Disables blindness and darkness rendering.", true));

    public NoRenderModule() {
        super("NoRender", Category.RENDER, "Disables selected visual effects.");
    }
}
