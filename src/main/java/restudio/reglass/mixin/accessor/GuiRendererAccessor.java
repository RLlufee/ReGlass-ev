package restudio.reglass.mixin.accessor;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiRenderer.class)
public interface GuiRendererAccessor {

    @Accessor("vertexConsumers")
    MultiBufferSource.BufferSource getVertexConsumers();
}
