package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.render.NoRenderModule;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
    private void yoim$submersion(CallbackInfoReturnable<CameraSubmersionType> cir) {
        NoRenderModule module = Yoim.MODULES.get(NoRenderModule.class);
        if (module != null && module.isEnabled() && module.liquidOverlay.getValue()) cir.setReturnValue(CameraSubmersionType.NONE);
    }
}
