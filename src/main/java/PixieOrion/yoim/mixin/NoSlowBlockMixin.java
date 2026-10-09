package PixieOrion.yoim.mixin;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.movement.NoSlowModule;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class NoSlowBlockMixin {
    @Inject(method = "getSlipperiness", at = @At("HEAD"), cancellable = true)
    private void yoim$slime(CallbackInfoReturnable<Float> cir) {
        if ((Object) this == Blocks.SLIME_BLOCK) {
            NoSlowModule module = Yoim.MODULES.get(NoSlowModule.class);
            if (module != null && module.isEnabled() && module.slimeBlocks.getValue()) cir.setReturnValue(0.6f);
        }
    }
}
