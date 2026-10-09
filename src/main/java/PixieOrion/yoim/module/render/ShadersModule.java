package PixieOrion.yoim.module.render;

import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.module.client.ColorsModule;
import PixieOrion.yoim.settings.impl.BooleanSetting;
import PixieOrion.yoim.settings.impl.CategorySetting;
import PixieOrion.yoim.settings.impl.ColorSetting;
import PixieOrion.yoim.settings.impl.NumberSetting;
import PixieOrion.yoim.settings.impl.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

import java.awt.Color;

public final class ShadersModule extends Module {
    public final CategorySetting targets = setting(new CategorySetting("Targets", "Selects entities for the shader."));
    public final BooleanSetting players = setting(new BooleanSetting("Players", "Players", "Players.", () -> targets.isOpen(), true));
    public final BooleanSetting hostiles = setting(new BooleanSetting("Hostiles", "Hostiles", "Hostile mobs.", () -> targets.isOpen(), true));
    public final BooleanSetting animals = setting(new BooleanSetting("Animals", "Animals", "Animals and aquatic creatures.", () -> targets.isOpen(), true));
    public final BooleanSetting ambient = setting(new BooleanSetting("Ambient", "Ambient", "Ambient creatures.", () -> targets.isOpen(), false));
    public final BooleanSetting invisibles = setting(new BooleanSetting("Invisibles", "Invisibles", "Invisible entities.", () -> targets.isOpen(), true));
    public final BooleanSetting items = setting(new BooleanSetting("Items", "Items", "Dropped items and experience bottles.", () -> targets.isOpen(), true));
    public final BooleanSetting crystals = setting(new BooleanSetting("Crystals", "Crystals", "End crystals.", () -> targets.isOpen(), true));
    public final BooleanSetting others = setting(new BooleanSetting("Others", "Others", "Other entities.", () -> targets.isOpen(), false));
    public final ModeSetting mode = setting(new ModeSetting("Mode", "Custom post-processing style.", "Both", "Fill", "Outline", "Both"));
    public final NumberSetting opacity = setting(new NumberSetting("Opacity", "Opacity of the custom shader mask.", 255, 0, 255));
    public final ColorSetting color = setting(new ColorSetting("Color", "Shader color.", new Color(255, 215, 0, 255)));

    public ShadersModule() {
        super("Shaders", Category.RENDER, "Renders a custom post-processing shader around selected entities.");
    }

    public boolean isValidEntity(Entity entity) {
        if (players.getValue() && entity.getType() == EntityType.PLAYER) return true;
        if (hostiles.getValue() && entity.getType().getSpawnGroup() == SpawnGroup.MONSTER) return true;
        if (animals.getValue()) {
            SpawnGroup group = entity.getType().getSpawnGroup();
            if (group == SpawnGroup.CREATURE || group == SpawnGroup.WATER_CREATURE || group == SpawnGroup.WATER_AMBIENT || group == SpawnGroup.UNDERGROUND_WATER_CREATURE || group == SpawnGroup.AXOLOTLS) return true;
        }
        if (ambient.getValue() && entity.getType().getSpawnGroup() == SpawnGroup.AMBIENT) return true;
        if (invisibles.getValue() && entity.isInvisible()) return true;
        if (items.getValue() && (entity.getType() == EntityType.ITEM || entity.getType() == EntityType.EXPERIENCE_BOTTLE)) return true;
        if (crystals.getValue() && entity.getType() == EntityType.END_CRYSTAL) return true;
        return others.getValue();
    }

    public int getOutlineColor() {
        Color c = ColorsModule.isSyncEnabled() ? ColorsModule.getGlobalColor() : color.getColor();
        int alpha = opacity.getValue().intValue();
        return ((alpha & 255) << 24) | ((c.getRed() & 255) << 16) | ((c.getGreen() & 255) << 8) | (c.getBlue() & 255);
    }

    public String getPipelineName() {
        return switch (mode.getValue()) {
            case "Fill" -> "outline_fill";
            case "Outline" -> "outline_outline";
            default -> "outline_both";
        };
    }
}
