package restudio.reglass.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.mixin.accessor.BeaconScreenButtonAccessor;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconPowerButton")
public abstract class BeaconPowerButtonMixin {
    @Shadow private Identifier sprite;

    @Unique private static double reglass$selectedBlobX = Double.NaN;
    @Unique private static double reglass$selectedBlobY = Double.NaN;

    @Inject(method = "extractIcon", at = @At("HEAD"), cancellable = true)
    private void reglass$renderInactiveEffectIcon(GuiGraphicsExtractor context, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        AbstractWidget widget = (AbstractWidget)(Object) this;
        if (widget.active && ((BeaconScreenButtonAccessor) this).reglass$isSelected()) {
            int targetX = widget.getX() + 1;
            int targetY = widget.getY() + 1;
            if (Double.isNaN(reglass$selectedBlobX)
                    || Double.isNaN(reglass$selectedBlobY)
                    || Math.abs(reglass$selectedBlobX - targetX) > 80
                    || Math.abs(reglass$selectedBlobY - targetY) > 80) {
                reglass$selectedBlobX = targetX;
                reglass$selectedBlobY = targetY;
            }

            double deltaTicks = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
            double deltaSeconds = deltaTicks / 20.0;
            double tau = 0.08;
            double alpha = 1.0 - Math.exp(-deltaSeconds / tau);
            if (alpha < 0.0) alpha = 0.0;
            if (alpha > 1.0) alpha = 1.0;
            reglass$selectedBlobX += (targetX - reglass$selectedBlobX) * alpha;
            reglass$selectedBlobY += (targetY - reglass$selectedBlobY) * alpha;

            ReGlassApi.create(context)
                    .dimensions((int) Math.round(reglass$selectedBlobX), (int) Math.round(reglass$selectedBlobY), 20, 20)
                    .cornerRadius(6)
                    .hover(0.9f)
                    .style(WidgetStyle.create().tint(0x000000, 0.16f).layer(2))
                    .screenSpace()
                    .render();
        }

        if (widget.active) {
            return;
        }

        context.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, widget.getX() + 2, widget.getY() + 2, 18, 18, 0xFF555555);
        ci.cancel();
    }

}
