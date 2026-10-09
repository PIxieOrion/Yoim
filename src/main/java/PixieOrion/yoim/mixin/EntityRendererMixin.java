package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.render.ShadersModule;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void yoim$shaderMask(T entity, S state, float tickProgress, CallbackInfo ci) {
        ShadersModule module = Yoim.MODULES.get(ShadersModule.class);
        if (module != null && module.isEnabled() && module.isValidEntity(entity)) {
            state.outlineColor = module.getOutlineColor();
        }
    }
}
