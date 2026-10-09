package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.movement.NoSlowModule;
import PixieOrion.yoim.module.combat.KillAuraModule;
import PixieOrion.yoim.module.movement.SpeedModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMovementMixin {
    @Unique private float yoim$cameraYawBeforePacket;
    @Unique private float yoim$cameraPitchBeforePacket;
    @Unique private boolean yoim$temporaryRotationApplied;

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void yoim$applyRotationToMovementPacket(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        KillAuraModule module = Yoim.MODULES.get(KillAuraModule.class);
        yoim$temporaryRotationApplied = module != null && module.hasVisualRotation();
        if (!yoim$temporaryRotationApplied) return;
        yoim$cameraYawBeforePacket = player.getYaw();
        yoim$cameraPitchBeforePacket = player.getPitch();
        player.setYaw(module.getSilentYaw());
        player.setPitch(module.getSilentPitch());
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"))
    private void yoim$restoreCameraRotation(CallbackInfo ci) {
        if (!yoim$temporaryRotationApplied) return;
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        player.setYaw(yoim$cameraYawBeforePacket);
        player.setPitch(yoim$cameraPitchBeforePacket);
        yoim$temporaryRotationApplied = false;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3d yoim$strafe(Vec3d movement) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        SpeedModule module = Yoim.MODULES.get(SpeedModule.class);
        if (module != null && module.isEnabled()) return module.modifyMovement(player, movement);
        return movement;
    }

    @Inject(method = "getActiveItemSpeedMultiplier", at = @At("RETURN"), cancellable = true)
    private void yoim$noSlowSpeed(CallbackInfoReturnable<Float> cir) {
        NoSlowModule module = Yoim.MODULES.get(NoSlowModule.class);
        if (module != null && module.isEnabled() && module.items.getValue() && !module.shouldSlow()) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "isBlockedFromSprinting", at = @At("RETURN"), cancellable = true)
    private void yoim$noSlowSprint(CallbackInfoReturnable<Boolean> cir) {
        NoSlowModule module = Yoim.MODULES.get(NoSlowModule.class);
        if (module != null && module.isEnabled() && module.items.getValue() && !module.shouldSlow()) {
            cir.setReturnValue(false);
        }
    }
}
