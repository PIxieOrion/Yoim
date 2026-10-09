package PixieOrion.yoim.core;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.mixin.accessor.EntityVelocityUpdateS2CPacketAccessor;
import PixieOrion.yoim.mixin.accessor.Vec3dAccessor;
import PixieOrion.yoim.module.movement.VelocityModule;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

public final class PacketReceiveHandler {
    private PacketReceiveHandler() {
    }

    public static boolean handle(Packet<?> packet) {
        if (packet instanceof EntityVelocityUpdateS2CPacket velocityPacket) {
            return handleVelocity(velocityPacket);
        }
        if (packet instanceof ExplosionS2CPacket explosionPacket) {
            return handleExplosion(explosionPacket);
        }
        return false;
    }

    private static boolean handleVelocity(EntityVelocityUpdateS2CPacket packet) {
        VelocityModule module = Yoim.MODULES.get(VelocityModule.class);
        if (module == null || !module.isEnabled() || Yoim.mc().player == null) return false;
        if (packet.getEntityId() != Yoim.mc().player.getId()) return false;

        String mode = module.mode.getValue();
        if (mode.equals("Cancel")) return true;

        double horizontal = module.horizontal.getValue().doubleValue() / 100.0D;
        double vertical = module.vertical.getValue().doubleValue() / 100.0D;
        Vec3d current = Yoim.mc().player.getVelocity();
        Vec3d incoming = packet.getVelocity();
        Vec3d modified = new Vec3d(
                (incoming.x - current.x) * horizontal + current.x,
                (incoming.y - current.y) * vertical + current.y,
                (incoming.z - current.z) * horizontal + current.z
        );
        ((EntityVelocityUpdateS2CPacketAccessor) (Object) packet).setVelocity(modified);
        return false;
    }

    private static boolean handleExplosion(ExplosionS2CPacket packet) {
        VelocityModule module = Yoim.MODULES.get(VelocityModule.class);
        if (module == null || !module.isEnabled() || !module.explosions.getValue()) return false;

        String mode = module.mode.getValue();
        if (mode.equals("Normal")) {
            if (packet.playerKnockback().isPresent()) {
                Vec3d knockback = packet.playerKnockback().get();
                double horizontal = module.horizontal.getValue().doubleValue() / 100.0D;
                double vertical = module.vertical.getValue().doubleValue() / 100.0D;
                Vec3dAccessor accessor = (Vec3dAccessor) (Object) knockback;
                accessor.setX(knockback.x * horizontal);
                accessor.setY(knockback.y * vertical);
                accessor.setZ(knockback.z * horizontal);
            }
            return false;
        }

        if (!mode.equals("Cancel")) return false;

        Yoim.mc().executeSync(() -> {
            if (Yoim.mc().world == null) return;
            Vec3d center = packet.center();
            Yoim.mc().world.playSound(
                    null,
                    center.getX(),
                    center.getY(),
                    center.getZ(),
                    packet.explosionSound().value(),
                    SoundCategory.BLOCKS,
                    4.0F,
                    (1.0F + (Yoim.mc().world.random.nextFloat() - Yoim.mc().world.random.nextFloat()) * 0.2F) * 0.7F,
                    0L
            );
            Yoim.mc().world.addParticleClient(
                    packet.explosionParticle(),
                    true,
                    false,
                    center.getX(),
                    center.getY(),
                    center.getZ(),
                    0.0D,
                    0.0D,
                    0.0D
            );
        });
        return true;
    }
}
