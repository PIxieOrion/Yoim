package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.render.NoRenderModule;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void yoim$explosion(ParticleEffect parameters, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<Particle> cir) {
        NoRenderModule module = Yoim.MODULES.get(NoRenderModule.class);
        if (module != null && module.isEnabled() && module.explosions.getValue() && parameters.getType() == ParticleTypes.EXPLOSION) cir.cancel();
    }
}
