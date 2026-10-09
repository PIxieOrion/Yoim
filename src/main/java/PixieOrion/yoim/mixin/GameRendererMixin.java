package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.render.NoRenderModule;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void yoim$hurt(CallbackInfo ci) {
        NoRenderModule module = Yoim.MODULES.get(NoRenderModule.class);
        if (module != null && module.isEnabled() && module.hurtCamera.getValue()) ci.cancel();
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void yoim$totem(ItemStack stack, CallbackInfo ci) {
        NoRenderModule module = Yoim.MODULES.get(NoRenderModule.class);
        if (module != null && module.isEnabled() && module.totemAnimation.getValue()) ci.cancel();
    }
}
