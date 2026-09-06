package restudio.reglass.mixin.accessor;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {
    @Mutable
    @Accessor("x")
    void reglass$setX(int x);

    @Mutable
    @Accessor("y")
    void reglass$setY(int y);
}
