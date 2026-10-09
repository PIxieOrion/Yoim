package PixieOrion.yoim.core;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.module.client.ClickGuiModule;
import PixieOrion.yoim.module.client.ArrayListModule;
import PixieOrion.yoim.module.combat.KillAuraModule;
import PixieOrion.yoim.module.client.ColorsModule;
import PixieOrion.yoim.module.movement.VelocityModule;
import PixieOrion.yoim.module.movement.SpeedModule;
import PixieOrion.yoim.module.movement.NoSlowModule;
import PixieOrion.yoim.module.render.FullBrightModule;
import PixieOrion.yoim.module.render.NoRenderModule;
import PixieOrion.yoim.module.render.ShadersModule;
import PixieOrion.yoim.module.render.NameTagsModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Integer, Boolean> previousBindState = new HashMap<>();

    public void register(Module module) { modules.add(module); }

    public void registerDefaults() {
        register(new KillAuraModule());
        register(new VelocityModule());
        register(new SpeedModule());
        register(new NoSlowModule());
        register(new FullBrightModule());
        register(new NoRenderModule());
        register(new ShadersModule());
        register(new NameTagsModule());
        register(new ColorsModule());
        register(new ArrayListModule());
        register(new ClickGuiModule());
    }

    public List<Module> all() { return Collections.unmodifiableList(modules); }

    public List<Module> byCategory(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).toList();
    }

    public <T extends Module> T get(Class<T> type) {
        for (Module module : modules) if (type.isInstance(module)) return type.cast(module);
        return null;
    }

    public void pollBinds(long windowHandle) {
        pollBinds(windowHandle, true);
    }

    public void pollBinds(long windowHandle, boolean allowToggles) {
        for (Module module : modules) {
            if (module instanceof ClickGuiModule) continue;
            int key = module.getBind();
            if (key == 0) continue;
            boolean pressed = isPressed(windowHandle, key);
            int identity = System.identityHashCode(module);
            boolean previous = previousBindState.getOrDefault(identity, false);
            if (allowToggles && pressed && !previous) module.toggle();
            previousBindState.put(identity, pressed);
        }
    }

    private boolean isPressed(long windowHandle, int bind) {
        if (bind < 0) {
            int button = -bind - 1;
            return GLFW.glfwGetMouseButton(windowHandle, button) == GLFW.GLFW_PRESS;
        }
        try {
            return net.minecraft.client.util.InputUtil.isKeyPressed(Yoim.mc().getWindow(), bind);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
