package PixieOrion.yoim.events;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.gui.ClickGuiScreen;
import PixieOrion.yoim.module.client.ClickGuiModule;
import PixieOrion.yoim.module.combat.KillAuraModule;
import PixieOrion.yoim.module.render.FullBrightModule;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ClientTickHandler {
    private static boolean lastRightShift;
    private ClientTickHandler() {}

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            KillAuraModule aura = Yoim.MODULES.get(KillAuraModule.class);
            if (aura != null) aura.prepareTick();
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean guiWasOpen = client.currentScreen instanceof ClickGuiScreen;
            boolean pressed = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (pressed && !lastRightShift && (client.currentScreen == null || client.currentScreen instanceof ClickGuiScreen)) toggleClickGui(client);
            lastRightShift = pressed;
            Yoim.MODULES.pollBinds(client.getWindow().getHandle(), !guiWasOpen && !(client.currentScreen instanceof ClickGuiScreen));
            KillAuraModule aura = Yoim.MODULES.get(KillAuraModule.class);
            if (aura != null) aura.attackTick();
            FullBrightModule fullBright = Yoim.MODULES.get(FullBrightModule.class);
            if (fullBright != null) fullBright.tick();
        });
    }

    private static void toggleClickGui(MinecraftClient client) {
        ClickGuiModule gui = Yoim.MODULES.get(ClickGuiModule.class);
        if (client.currentScreen instanceof ClickGuiScreen) {
            gui.setEnabled(false);
            client.setScreen(null);
        } else {
            gui.setEnabled(true);
            client.setScreen(new ClickGuiScreen());
        }
    }
}
