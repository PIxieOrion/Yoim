package PixieOrion.yoim.packets;

import PixieOrion.yoim.Yoim;
import net.minecraft.network.packet.Packet;

public final class PacketUtils {
    private PacketUtils() {}

    public static void send(Packet<?> packet) {
        if (Yoim.mc().getNetworkHandler() != null) Yoim.mc().getNetworkHandler().sendPacket(packet);
    }
}
