package restudio.reglass.mixin.client;

import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin extends AbstractContainerScreen<LoomMenu> {
    protected LoomScreenMixin(LoomMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void reglass$extractPatternPanel(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        ReGlassApi.create(context)
                .dimensions(this.leftPos + 58, this.topPos + 12, 58, 58)
                .cornerRadius(9)
                .style(WidgetStyle.create().tint(0x000000, 0.14f).layer(1))
                .screenSpace()
                .render();
    }

    @Redirect(
            method = "extractBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;bannerPattern(Lnet/minecraft/client/model/object/banner/BannerFlagModel;Lnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/entity/BannerPatternLayers;IIII)V"
            )
    )
    private void reglass$moveBannerPreview(GuiGraphicsExtractor context, BannerFlagModel model, DyeColor dyeColor, BannerPatternLayers layers, int x1, int y1, int x2, int y2) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            context.bannerPattern(model, dyeColor, layers, x1, y1, x2, y2);
            return;
        }

        context.bannerPattern(model, dyeColor, layers, x1 - 3, y1 - 9, x2 - 2, y2 - 9);
    }
}
