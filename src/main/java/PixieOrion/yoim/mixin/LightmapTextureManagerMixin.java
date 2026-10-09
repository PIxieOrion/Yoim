package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.render.FullBrightModule;
import PixieOrion.yoim.module.render.NoRenderModule;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Inject(method = "getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F", at = @At("RETURN"), cancellable = true)
    private static void yoim$getBrightness(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        FullBrightModule fullBright = Yoim.MODULES.get(FullBrightModule.class);
        NoRenderModule noRender = Yoim.MODULES.get(NoRenderModule.class);
        if (fullBright != null && fullBright.isEnabled()) cir.setReturnValue(1.0F);
        if (noRender != null && noRender.isEnabled() && noRender.blindness.getValue()) cir.setReturnValue(1.0F);
    }

    @Inject(method = "getBrightness(FI)F", at = @At("RETURN"), cancellable = true)
    private static void yoim$getBrightness(float ambientLight, int lightLevel, CallbackInfoReturnable<Float> cir) {
        FullBrightModule fullBright = Yoim.MODULES.get(FullBrightModule.class);
        if (fullBright != null && fullBright.isEnabled()) cir.setReturnValue(1.0F);
    }
}
