package PixieOrion.yoim;

import PixieOrion.yoim.core.ModuleManager;
import PixieOrion.yoim.core.ConfigManager;
import PixieOrion.yoim.events.ClientTickHandler;
import PixieOrion.yoim.core.NameTagsRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;

public final class Yoim implements ClientModInitializer {
    public static final String NAME = "Yoim";
    public static final String VERSION = "0.6.17";
    public static final ModuleManager MODULES = new ModuleManager();

    @Override
    public void onInitializeClient() {
        MODULES.registerDefaults();
        ConfigManager.load(MODULES);
        ClientTickHandler.register();
        NameTagsRenderer.register();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.save(MODULES));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ConfigManager.save(MODULES)));
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> System.out.println("[Yoim] loaded v" + VERSION));
    }

    public static MinecraftClient mc() { return MinecraftClient.getInstance(); }
}
