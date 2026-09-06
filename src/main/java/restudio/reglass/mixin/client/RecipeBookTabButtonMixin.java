package restudio.reglass.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

@Mixin(RecipeBookTabButton.class)
public abstract class RecipeBookTabButtonMixin {
    @Shadow private RecipeBookComponent.TabInfo tabInfo;
    @Shadow private boolean selected;

    @Inject(method = "extractContents", at = @At("HEAD"), cancellable = true)
    private void reglass$renderStableTab(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        RecipeBookTabButton self = (RecipeBookTabButton)(Object) this;
        int x = self.getX();
        int y = self.getY();
        int width = self.getWidth();
        int height = self.getHeight();
        int size = Math.min(width, height) - 3;
        int cx = x + width / 2;
        int cy = y + height / 2;

        ReGlassApi.create(context)
                .dimensions(cx - size / 2, cy - size / 2, size, size)
                .cornerRadius(size * 0.5f)
                .hover(this.selected ? 1f : 0f)
                .style(WidgetStyle.create()
                        .tint(0x000000, this.selected ? 0.22f : 0.12f)
                        .layer(this.selected ? 3 : 2))
                .screenSpace()
                .render();

        reglass$renderIcon(context, cx, cy);
        ci.cancel();
    }

    @Unique
    private void reglass$renderIcon(GuiGraphicsExtractor context, int cx, int cy) {
        if (this.tabInfo.secondaryIcon().isPresent()) {
            context.fakeItem(this.tabInfo.primaryIcon(), cx - 13, cy - 8);
            context.fakeItem((ItemStack)this.tabInfo.secondaryIcon().get(), cx - 3, cy - 8);
            return;
        }
        context.fakeItem(this.tabInfo.primaryIcon(), cx - 8, cy - 8);
    }
}
