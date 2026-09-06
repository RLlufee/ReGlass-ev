package restudio.reglass.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {
    @Shadow public abstract boolean isVisible();
    @Shadow private int getXOrigin() {
        throw new AssertionError();
    }
    @Shadow private int getYOrigin() {
        throw new AssertionError();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void reglass$renderRecipeBookGlass(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers || !this.isVisible()) {
            return;
        }

        int x = this.getXOrigin();
        int y = this.getYOrigin();
        ReGlassApi.create(context)
                .dimensions(x, y, 147, 166)
                .cornerRadius(10)
                .style(reglass$recipeBookStyle().layer(1))
                .screenSpace()
                .render();
    }

    @Unique
    private WidgetStyle reglass$recipeBookStyle() {
        return WidgetStyle.create()
                .tint(0x000000, 0.18f);
    }
}
