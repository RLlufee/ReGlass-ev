package restudio.reglass.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(
            method = "extractBlurredBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void reglass$skipDuplicateBlur(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (LiquidGlassUniforms.get().screenWantsBlur()) {
            ci.cancel();
        }
    }


    @Inject(
            method = "extractTransparentBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void reglass$onRenderDarkening(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (ReGlassConfig.INSTANCE.features.enableRedesign && ReGlassConfig.INSTANCE.features.cancelScreenDarkening) {
            ci.cancel();
        }
    }
}