package PixieOrion.yoim.mixin.accessor;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.entity.EntityRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderManager.class)
public interface EntityRenderManagerAccessor {
    @Accessor("itemModelManager")
    ItemModelManager yoim$getItemModelManager();
}
