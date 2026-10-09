package PixieOrion.yoim.module.movement;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.impl.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public final class SpeedModule extends Module {
    public final NumberSetting strafeSpeed = setting(new NumberSetting("StrafeSpeed", "The strafe speed limit.", 0.465, 0.1, 0.576));
    private double distance;
    private double speed;
    private double forward;
    private int stage;
    private int ticks;

    public SpeedModule() {
        super("Speed", Category.MOVEMENT, "Makes movement faster.");
    }

    @Override
    protected void onEnable() {
        stage = 1;
        ticks = 0;
        distance = 0.0;
        speed = 0.0;
        forward = 0.0;
    }

    @Override
    protected void onDisable() {
        stage = 1;
        ticks = 0;
        distance = 0.0;
        speed = 0.0;
        forward = 0.0;
    }

    public Vec3d modifyMovement(ClientPlayerEntity player, Vec3d movement) {
        if (!isEnabled() || player == null || !isMoving(player)) return movement;
        if (player.fallDistance >= 5.0f || player.isSneaking() || player.isClimbing() || player.getAbilities().flying || player.isGliding()) return movement;
        if (player.isInFluid()) return movement;
        if (player.getEntityWorld().getBlockState(player.getBlockPos()).getBlock() == Blocks.COBWEB) return movement;

        distance = Math.sqrt(square(player.getX() - player.lastX) + square(player.getZ() - player.lastZ));
        speed = getPotionSpeed(0.2873) * (player.input.getMovementInput().y <= 0.0f && forward > 0.0 ? 0.66 : 1.0);

        if (stage == 1 && isMoving(player) && player.verticalCollision) {
            double jump = getPotionJump(0.3999999463558197);
            player.setVelocity(player.getVelocity().x, jump, player.getVelocity().z);
            movement = new Vec3d(movement.x, jump, movement.z);
            speed *= 2.149;
            stage = 2;
        } else if (stage == 2) {
            speed = distance - 0.66 * (distance - getPotionSpeed(0.2873));
            stage = 3;
        } else {
            if (!player.getEntityWorld().getEntityCollisions(player, player.getBoundingBox().offset(0.0, player.getVelocity().y, 0.0)).isEmpty() || player.verticalCollision) {
                stage = 1;
            }
            speed = distance - distance / 159.0;
        }

        speed = Math.max(speed, getPotionSpeed(0.2873));

        double strafeCap = strafeSpeed.getValue().doubleValue();
        double ncp = getPotionSpeed(player.input.getMovementInput().y < 1.0f ? strafeCap : 0.576);
        double bypass = getPotionSpeed(player.input.getMovementInput().y < 1.0f ? Math.min(0.44, strafeCap) : 0.57);
        speed = Math.min(speed, ticks > 25 ? ncp : bypass);

        if (++ticks > 50) ticks = 0;

        Vec3d velocity = forward(player, speed);
        forward = player.input.getMovementInput().y;
        return new Vec3d(velocity.x, movement.y, velocity.z);
    }

    public boolean isMoving(ClientPlayerEntity player) {
        return player != null && (player.sidewaysSpeed != 0.0f || player.forwardSpeed != 0.0f);
    }

    private Vec3d forward(ClientPlayerEntity player, double value) {
        float forwardInput = player.input.getMovementInput().y;
        float sideways = player.input.getMovementInput().x;
        float yaw = player.getYaw();

        if (forwardInput == 0.0f && sideways == 0.0f) return Vec3d.ZERO;
        if (forwardInput != 0.0f) {
            if (sideways >= 1.0f) {
                yaw += forwardInput > 0.0f ? -45.0f : 45.0f;
                sideways = 0.0f;
            } else if (sideways <= -1.0f) {
                yaw += forwardInput > 0.0f ? 45.0f : -45.0f;
                sideways = 0.0f;
            }
            if (forwardInput > 0.0f) forwardInput = 1.0f;
            else if (forwardInput < 0.0f) forwardInput = -1.0f;
        }

        double motionX = Math.cos(Math.toRadians(yaw + 90.0f));
        double motionZ = Math.sin(Math.toRadians(yaw + 90.0f));
        return new Vec3d(
                forwardInput * value * motionX + sideways * value * motionZ,
                0.0,
                forwardInput * value * motionZ - sideways * value * motionX
        );
    }

    private double getPotionSpeed(double value) {
        if (Yoim.mc().player == null) return value;
        if (Yoim.mc().player.hasStatusEffect(StatusEffects.SPEED)) {
            value *= 1.0 + 0.2 * (Yoim.mc().player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1);
        }
        if (Yoim.mc().player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            value /= 1.0 + 0.2 * (Yoim.mc().player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1);
        }
        return value;
    }

    private double getPotionJump(double value) {
        if (Yoim.mc().player != null && Yoim.mc().player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            value += (Yoim.mc().player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1;
        }
        return value;
    }

    private double square(double value) {
        return value * value;
    }
}
