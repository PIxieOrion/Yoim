package PixieOrion.yoim.core;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.mixin.accessor.EntityVelocityUpdateS2CPacketAccessor;
import PixieOrion.yoim.mixin.accessor.Vec3dAccessor;
import PixieOrion.yoim.module.movement.VelocityModule;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
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
        if (packet.playerKnockback().isEmpty()) return false;

        Vec3d knockback = packet.playerKnockback().get();
        Vec3dAccessor accessor = (Vec3dAccessor) (Object) knockback;

        if (mode.equals("Cancel")) {
            // Keep the original explosion packet intact for Minecraft's normal
            // sound, particles, and other effects; only remove its player knockback.
            accessor.setX(0.0D);
            accessor.setY(0.0D);
            accessor.setZ(0.0D);
            return false;
        }

        if (mode.equals("Normal")) {
            double horizontal = module.horizontal.getValue().doubleValue() / 100.0D;
            double vertical = module.vertical.getValue().doubleValue() / 100.0D;
            accessor.setX(knockback.x * horizontal);
            accessor.setY(knockback.y * vertical);
            accessor.setZ(knockback.z * horizontal);
        }
        return false;
    }
}
