package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.core.YoimShaderRenderer;
import PixieOrion.yoim.mixin.accessor.WorldRendererAccessor;
import PixieOrion.yoim.module.render.NoRenderModule;
import PixieOrion.yoim.module.render.ShadersModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "hasBlindnessOrDarkness", at = @At("HEAD"), cancellable = true)
    private void yoim$blindness(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        NoRenderModule module = Yoim.MODULES.get(NoRenderModule.class);
        if (module != null && module.isEnabled() && module.blindness.getValue()) cir.setReturnValue(false);
    }

    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("HEAD"), cancellable = true)
    private void yoim$customEntityOutline(CallbackInfo ci) {
        ShadersModule module = Yoim.MODULES.get(ShadersModule.class);
        if (module == null || !module.isEnabled()) return;
        WorldRendererAccessor accessor = (WorldRendererAccessor) this;
        if (YoimShaderRenderer.render(accessor.yoim$getEntityOutlineFramebuffer(), module)) ci.cancel();
    }
}
