package restudio.reglass.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import restudio.reglass.client.api.WidgetStyle;

public record LiquidGlassGuiElementRenderState(int x1, int y1, int x2, int y2, float cornerRadius, @Nullable Component text, WidgetStyle style, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea, float hover, float focus) implements GuiElementRenderState {

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(0);
        vertexConsumer.addVertexWith2DPose(pose, x1, y2).setColor(0);
        vertexConsumer.addVertexWith2DPose(pose, x2, y2).setColor(0);
        vertexConsumer.addVertexWith2DPose(pose, x2, y1).setColor(0);
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Nullable
    @Override
    public ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        ScreenRectangle ownBounds = new ScreenRectangle(x1, y1, x2 - x1, y2 - y1).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(ownBounds) : ownBounds;
    }
}