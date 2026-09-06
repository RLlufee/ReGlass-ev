package restudio.reglass.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BrewingStandMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(BrewingStandScreen.class)
public abstract class BrewingStandScreenMixin extends AbstractContainerScreen<BrewingStandMenu> {
    protected BrewingStandScreenMixin(BrewingStandMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void reglass$extractBrewingMeters(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        int brewTicks = Mth.clamp(this.menu.getBrewingTicks(), 0, 400);
        float brewProgress = brewTicks == 0 ? 0f : 1.0f - brewTicks / 400.0f;
        reglass$progressRail(context, this.leftPos + 78, this.topPos + 33, 18, brewProgress);

        int fuel = Mth.clamp(this.menu.getFuel(), 0, 20);
        reglass$progressRail(context, this.leftPos + 22, this.topPos + 50, 18, fuel / 20.0f);
    }

    @Unique
    private void reglass$progressRail(GuiGraphicsExtractor context, int x, int y, int width, float progress) {
        context.fill(x, y, x + width, y + 2, 0x66000000);
        int filled = Mth.clamp(Math.round(width * progress), 0, width);
        if (filled > 0) {
            context.fill(x, y, x + filled, y + 2, 0xCCFFFFFF);
        }
    }
}
