package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.movement.NoSlowModule;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(Entity.class)
public abstract class NoSlowEntityMixin {
    @ModifyExpressionValue(method = "getVelocityMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
    private Block yoim$blockSlow(Block original) {
        NoSlowModule module = Yoim.MODULES.get(NoSlowModule.class);
        if (module != null && module.isEnabled()) {
            if ((original == Blocks.SOUL_SAND && module.soulSand.getValue()) || (original == Blocks.HONEY_BLOCK && module.honeyBlocks.getValue())) return Blocks.STONE;
        }
        return original;
    }
}
