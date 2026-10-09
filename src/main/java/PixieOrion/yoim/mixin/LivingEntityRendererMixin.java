package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.combat.KillAuraModule;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void yoim$renderSilentRotation(T entity, S state, float tickProgress, CallbackInfo ci) {
        if (entity != Yoim.mc().player) return;
        KillAuraModule aura = Yoim.MODULES.get(KillAuraModule.class);
        if (aura == null || !aura.hasVisualRotation()) return;
        state.bodyYaw = aura.getSilentYaw();
        state.relativeHeadYaw = 0.0f;
        state.pitch = aura.getSilentPitch();
    }
}
