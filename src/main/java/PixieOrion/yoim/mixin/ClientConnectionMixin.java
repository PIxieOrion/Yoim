package PixieOrion.yoim.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import PixieOrion.yoim.core.PacketReceiveHandler;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void yoim$channelRead0(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        boolean cancelled = PacketReceiveHandler.handle(packet);
        if (packet instanceof BundleS2CPacket bundle) {
            for (Packet<?> subPacket : bundle.getPackets()) {
                PacketReceiveHandler.handle(subPacket);
            }
        }
        if (cancelled) ci.cancel();
    }
}
