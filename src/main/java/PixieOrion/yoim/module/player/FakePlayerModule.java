package PixieOrion.yoim.module.player;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.impl.NumberSetting;
import PixieOrion.yoim.settings.impl.StringSetting;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import java.util.UUID;

public final class FakePlayerModule extends Module {
    private final StringSetting fakeName = setting(new StringSetting("Name", "Fake player name", "FakePlayer"));
    private final NumberSetting health = setting(new NumberSetting("Health", "Fake player health", 20.0f, 1.0f, 20.0f));

    private OtherClientPlayerEntity fakePlayer;
    private ClientWorld spawnWorld;

    public FakePlayerModule() {
        super("FakePlayer", Category.WORLD, "Spawns a local fake player for testing.");
    }

    @Override
    protected void onEnable() {
        MinecraftClient mc = Yoim.mc();
        if (mc.player == null || mc.world == null) {
            setEnabled(false);
            return;
        }
        removeFakePlayer();
        spawnWorld = mc.world;
        String name = fakeName.getValue() == null || fakeName.getValue().isBlank() ? "FakePlayer" : fakeName.getValue();
        fakePlayer = new OtherClientPlayerEntity(spawnWorld, new GameProfile(UUID.randomUUID(), name));
        fakePlayer.copyPositionAndRotation(mc.player);
        fakePlayer.copyFrom(mc.player);
        fakePlayer.setHealth(health.getValue().floatValue());
        fakePlayer.setId(-673);
        spawnWorld.addEntity(fakePlayer);
    }

    @Override
    protected void onDisable() { removeFakePlayer(); }

    private void removeFakePlayer() {
        if (fakePlayer != null && spawnWorld != null)
            spawnWorld.removeEntity(fakePlayer.getId(), Entity.RemovalReason.DISCARDED);
        fakePlayer = null;
        spawnWorld = null;
    }

    public void tick() {
        MinecraftClient mc = Yoim.mc();
        if (!isEnabled() || fakePlayer == null) return;
        if (mc.world == null || mc.world != spawnWorld || mc.player == null) {
            fakePlayer = null;
            spawnWorld = null;
            setEnabled(false);
        }
    }

    public OtherClientPlayerEntity getFakePlayer() { return fakePlayer; }
}
