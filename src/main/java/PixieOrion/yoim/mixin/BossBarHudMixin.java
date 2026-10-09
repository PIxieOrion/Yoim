package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.render.NoRenderModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void yoim$bossBar(DrawContext context, CallbackInfo ci) {
        NoRenderModule module = Yoim.MODULES.get(NoRenderModule.class);
        if (module != null && module.isEnabled() && module.bossBar.getValue()) ci.cancel();
    }
}
