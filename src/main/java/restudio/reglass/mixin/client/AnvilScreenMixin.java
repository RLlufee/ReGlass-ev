package restudio.reglass.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin {
    @Shadow private EditBox name;

    @Inject(method = "subInit", at = @At("TAIL"))
    private void reglass$layoutNameField(CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        this.name.setX(this.name.getX() - 54);
        this.name.setWidth(160);
    }

    @Redirect(
            method = "extractLabels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            )
    )
    private void reglass$text(GuiGraphicsExtractor context, Font font, Component text, int x, int y, int color) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            context.text(font, text, x, y, color);
            return;
        }

        context.text(font, text, x, y, 0xFFFFFFFF, true);
    }
}
