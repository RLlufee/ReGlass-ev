package restudio.reglass.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconScreenButton")
public interface BeaconScreenButtonAccessor {
    @Invoker("isSelected")
    boolean reglass$isSelected();
}