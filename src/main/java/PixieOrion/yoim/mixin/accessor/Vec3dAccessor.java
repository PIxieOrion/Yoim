package PixieOrion.yoim.mixin.accessor;

import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Vec3d.class)
public interface Vec3dAccessor {
    @Accessor("x") @Mutable void setX(double value);
    @Accessor("y") @Mutable void setY(double value);
    @Accessor("z") @Mutable void setZ(double value);
}
