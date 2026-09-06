package restudio.reglass.client.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.gui.LiquidGlassGuiElementRenderState;
import restudio.reglass.mixin.accessor.DrawContextAccessor;
import restudio.reglass.mixin.accessor.ScissorStackAccessor;

public final class ReGlassApi {
    private ReGlassApi() {}
    public static WidgetStyle inactiveStyle = new WidgetStyle().tint(0x000000, 0.3f);

    public static ReGlassConfig getGlobalConfig() {
        return ReGlassConfig.INSTANCE;
    }

    public static Builder create(GuiGraphicsExtractor context) {
        return new Builder(context);
    }

    public static class Builder {
        private final GuiGraphicsExtractor context;
        private int x, y, width, height;
        private float cornerRadius = -1f;
        @Nullable private Component text = null;
        private WidgetStyle style = new WidgetStyle();
        private float hoverAmount = 0f;
        private float focusAmount = 0f;
        private boolean screenSpace = false;

        private Builder(GuiGraphicsExtractor context) {
            this.context = context;
        }

        public Builder fromWidget(AbstractWidget widget) {
            this.position(widget.getX(), widget.getY());
            this.size(widget.getWidth(), widget.getHeight());
            this.text(widget.getMessage());
            return this;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public Builder cornerRadius(float radius) {
            this.cornerRadius = radius;
            return this;
        }

        public Builder text(Component text) {
            this.text = text;
            return this;
        }

        public Builder style(WidgetStyle style) {
            this.style = style;
            return this;
        }

        public Builder hover(float amount) {
            if (Float.isNaN(amount)) amount = 0f;
            this.hoverAmount = Math.max(0f, Math.min(1f, amount));
            return this;
        }

        public Builder focus(float amount) {
            if (Float.isNaN(amount)) amount = 0f;
            this.focusAmount = Math.max(0f, Math.min(1f, amount));
            return this;
        }

        public Builder selected(float amount) {
            return this.focus(amount);
        }

        public Builder screenSpace() {
            this.screenSpace = true;
            return this;
        }

        public void render() {
            float finalCornerRadius = this.cornerRadius < 0 ? 0.5f * Math.min(this.width, this.height) : this.cornerRadius;
            Matrix3x2f pose = this.screenSpace ? new Matrix3x2f() : new Matrix3x2f(context.pose());
            ScreenRectangle scissorRect = ((ScissorStackAccessor) ((DrawContextAccessor) context).getScissorStack()).reglass$peek();
            LiquidGlassUniforms.get().tryApplyBlur(context);
            LiquidGlassGuiElementRenderState element = new LiquidGlassGuiElementRenderState(
                    this.x, this.y, this.x + this.width, this.y + this.height,
                    finalCornerRadius, this.text, this.style, pose, scissorRect,
                    this.hoverAmount, this.focusAmount
            );
            LiquidGlassUniforms.get().addWidget(element);
            ((DrawContextAccessor) context).getGuiRenderState().addGuiElement(element);
        }
    }
}