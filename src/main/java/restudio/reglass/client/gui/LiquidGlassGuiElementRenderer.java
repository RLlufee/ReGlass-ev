package restudio.reglass.client.gui;

import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import restudio.reglass.client.LiquidGlassUniforms;

public class LiquidGlassGuiElementRenderer extends SpecialGuiElementRenderer<LiquidGlassGuiElementRenderState> {

    public LiquidGlassGuiElementRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void extractRenderState(LiquidGlassGuiElementRenderState element, GuiRenderState state, int scale) {
        LiquidGlassUniforms.get().addWidget(element);
    }

    @Override
    public Class<LiquidGlassGuiElementRenderState> getElementClass() {
        return LiquidGlassGuiElementRenderState.class;
    }

    @Override
    protected void render(LiquidGlassGuiElementRenderState element, PoseStack matrices) {
    }

    @Override
    protected String getName() {
        return "liquid_glass_widget";
    }
}