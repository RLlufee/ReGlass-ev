package restudio.reglass.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Inject(method = "extractLabels", at = @At("HEAD"), cancellable = true)
    private void reglass$hidePlayerInventoryLabels(GuiGraphicsExtractor context, int mouseX, int mouseY, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (cfg.features.enableRedesign && cfg.features.containers) {
            ci.cancel();
        }
    }

    @Inject(method = "getRecipeBookButtonPosition", at = @At("RETURN"), cancellable = true)
    private void reglass$moveRecipeBookButton(CallbackInfoReturnable<ScreenPosition> cir) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        ScreenPosition original = cir.getReturnValue();
        cir.setReturnValue(new ScreenPosition(original.x() + 28, original.y() - 4));
    }
}
