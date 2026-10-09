package PixieOrion.yoim.mixin.accessor;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.Glint;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderState.LayerRenderState.class)
public interface LayerRenderStateAccessor {
    @Accessor("tints") int[] yoim$getTints();
    @Accessor("renderLayer") RenderLayer yoim$getRenderLayer();
    @Accessor("glint") Glint yoim$getGlint();
    @Accessor("transform") Transformation yoim$getTransform();
    @Accessor("specialModelType") SpecialModelRenderer<?> yoim$getSpecialModelType();
}
