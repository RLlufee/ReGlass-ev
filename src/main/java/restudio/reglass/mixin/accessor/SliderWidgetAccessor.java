package restudio.reglass.mixin.accessor;

import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSliderButton.class)
public interface SliderWidgetAccessor {
    @Accessor("value")
    double getValue();

    @Accessor("value")
    void setValuePublic(double value);
}
