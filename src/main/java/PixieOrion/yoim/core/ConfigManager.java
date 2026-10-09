package PixieOrion.yoim.core;

import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.Setting;
import PixieOrion.yoim.settings.impl.BindSetting;
import PixieOrion.yoim.settings.impl.BooleanSetting;
import PixieOrion.yoim.settings.impl.ColorSetting;
import PixieOrion.yoim.settings.impl.ModeSetting;
import PixieOrion.yoim.settings.impl.NumberSetting;
import PixieOrion.yoim.settings.impl.StringSetting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class ConfigManager {
    private static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("yoim");
    private static final Path FILE = DIRECTORY.resolve("config.json");
    private ConfigManager() {}

    public static void load(ModuleManager manager) {
        if (!Files.exists(FILE)) { save(manager); return; }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject modules = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
            for (Module module : manager.all()) {
                if (!modules.has(module.getName())) continue;
                JsonObject data = modules.getAsJsonObject(module.getName());
                if (data.has("enabled")) module.setEnabled(data.get("enabled").getAsBoolean());
                if (!data.has("settings")) continue;
                JsonObject settings = data.getAsJsonObject("settings");
                for (Setting setting : module.getSettings()) {
                    if (!settings.has(setting.getName())) continue;
                    JsonElement value = settings.get(setting.getName());
                    try {
                        if (setting instanceof BooleanSetting s) s.setValue(value.getAsBoolean());
                        else if (setting instanceof BindSetting s) s.setValue(value.getAsInt());
                        else if (setting instanceof NumberSetting s) s.setValue(value.getAsDouble());
                        else if (setting instanceof ModeSetting s) s.setValue(value.getAsString());
                        else if (setting instanceof StringSetting s) s.setValue(value.getAsString());
                        else if (setting instanceof ColorSetting s) {
                            JsonObject c = value.getAsJsonObject();
                            s.setColor(new Color(c.get("r").getAsInt(), c.get("g").getAsInt(), c.get("b").getAsInt(), c.get("a").getAsInt()));
                        }
                    } catch (RuntimeException ignored) {}
                }
            }
        } catch (Exception exception) {
            System.err.println("[Yoim] Failed to load config: " + exception.getMessage());
        }
    }

    public static void save(ModuleManager manager) {
        try {
            Files.createDirectories(DIRECTORY);
            JsonObject root = new JsonObject();
            JsonObject modules = new JsonObject();
            for (Module module : manager.all()) {
                JsonObject data = new JsonObject();
                data.addProperty("enabled", module.isEnabled());
                JsonObject settings = new JsonObject();
                for (Setting setting : module.getSettings()) {
                    if (setting instanceof BooleanSetting s) settings.addProperty(setting.getName(), s.getValue());
                    else if (setting instanceof BindSetting s) settings.addProperty(setting.getName(), s.getValue());
                    else if (setting instanceof NumberSetting s) settings.addProperty(setting.getName(), s.getValue().doubleValue());
                    else if (setting instanceof ModeSetting s) settings.addProperty(setting.getName(), s.getValue());
                    else if (setting instanceof StringSetting s) settings.addProperty(setting.getName(), s.getValue());
                    else if (setting instanceof ColorSetting s) {
                        Color c = s.getColor(); JsonObject color = new JsonObject();
                        color.addProperty("r", c.getRed()); color.addProperty("g", c.getGreen());
                        color.addProperty("b", c.getBlue()); color.addProperty("a", c.getAlpha());
                        settings.add(setting.getName(), color);
                    }
                }
                data.add("settings", settings);
                modules.add(module.getName(), data);
            }
            root.add("modules", modules);
            Files.writeString(FILE, root.toString(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.err.println("[Yoim] Failed to save config: " + exception.getMessage());
        }
    }
}
