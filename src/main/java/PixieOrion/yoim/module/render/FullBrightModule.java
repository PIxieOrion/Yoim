package PixieOrion.yoim.module.render;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public final class FullBrightModule extends Module {
    private double previousGamma = -1.0D;

    public FullBrightModule() {
        super("FullBright", Category.RENDER, "Removes client-side darkness while enabled.");
    }

    @Override
    protected void onEnable() {
        if (Yoim.mc().player != null) {
            previousGamma = Yoim.mc().options.getGamma().getValue();
        }
        apply();
    }

    @Override
    protected void onDisable() {
        if (previousGamma >= 0.0D) {
            Yoim.mc().options.getGamma().setValue(previousGamma);
            previousGamma = -1.0D;
        }
        if (Yoim.mc().player != null) {
            Yoim.mc().player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    public void tick() {
        if (!isEnabled()) return;
        apply();
        if (Yoim.mc().player != null) {
            Yoim.mc().player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false, false));
        }
    }

    private void apply() {
        Yoim.mc().options.getGamma().setValue(16.0D);
    }
}
