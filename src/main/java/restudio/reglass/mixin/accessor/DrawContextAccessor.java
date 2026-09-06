package restudio.reglass.mixin.accessor;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor.ScissorStack;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphicsExtractor.class)
public interface DrawContextAccessor {

    @Accessor("scissorStack")
    ScissorStack getScissorStack();

    @Accessor("guiRenderState")
    GuiRenderState getGuiRenderState();
}