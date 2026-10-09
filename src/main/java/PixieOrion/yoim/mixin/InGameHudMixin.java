package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.render.NoRenderModule;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.module.client.ArrayListModule;
import PixieOrion.yoim.module.client.ColorsModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void yoim$arrayList(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ArrayListModule arrayList = Yoim.MODULES.get(ArrayListModule.class);
        if (arrayList == null || !arrayList.isEnabled() || Yoim.mc().player == null || Yoim.mc().options.hudHidden) return;
        if (Yoim.mc().currentScreen instanceof PixieOrion.yoim.gui.ClickGuiScreen) return;

        TextRenderer renderer = Yoim.mc().textRenderer;
        List<Module> visible = new ArrayList<>();
        for (Module module : Yoim.MODULES.all()) {
            if (module != arrayList && module.isEnabled() && module.drawn.getValue()) visible.add(module);
        }
        visible.sort(Comparator.comparingInt((Module module) -> renderer.getWidth(module.getName())).reversed());

        int margin = (int) arrayList.offset.getValue();
        int y = margin;
        int screenWidth = context.getScaledWindowWidth();
        int accentColor = ColorsModule.getGlobalColor().getRGB();
        for (Module module : visible) {
            String name = module.getName();
            int width = renderer.getWidth(name);
            int x = screenWidth - margin - width - 4;
            if (arrayList.background.getValue()) context.fill(x - 3, y - 1, screenWidth - margin, y + 10, 0x90000000);
            if (arrayList.accent.getValue()) context.fill(screenWidth - margin - 2, y - 1, screenWidth - margin, y + 10, accentColor);
            context.drawTextWithShadow(renderer, Text.literal(name), x, y, 0xFFFFFFFF);
            y += 11;
        }
    }
    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void yoim$portal(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        NoRenderModule module = Yoim.MODULES.get(NoRenderModule.class);
        if (module != null && module.isEnabled() && module.portalOverlay.getValue()) ci.cancel();
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    private void yoim$vignette(DrawContext context, Entity entity, CallbackInfo ci) {
        NoRenderModule module = Yoim.MODULES.get(NoRenderModule.class);
        if (module != null && module.isEnabled() && module.vignette.getValue()) ci.cancel();
    }

    @Inject(method = "renderMiscOverlays", at = @At("HEAD"), cancellable = true)
    private void yoim$misc(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        NoRenderModule module = Yoim.MODULES.get(NoRenderModule.class);
        if (module != null && module.isEnabled() && (module.pumpkinOverlay.getValue() || module.snowOverlay.getValue())) ci.cancel();
    }
}
