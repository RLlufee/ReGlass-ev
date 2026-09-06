package restudio.reglass.mixin.client;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {
    @Shadow @Final private static Component TRADES_LABEL;
    @Shadow @Final private static Identifier TRADE_ARROW_SPRITE;
    @Shadow private int shopItem;

    protected MerchantScreenMixin(MerchantMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "extractLabels", at = @At("HEAD"), cancellable = true)
    private void reglass$extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        Component titleText = this.title;
        int traderLevel = this.menu.getTraderLevel();
        if (traderLevel > 0 && traderLevel <= 5 && this.menu.showProgressBar()) {
            titleText = Component.translatable(
                    "merchant.title",
                    this.title,
                    Component.translatable("merchant.level." + traderLevel));
        }

        int titleWidth = this.font.width(titleText);
        context.text(this.font, titleText, 49 + this.imageWidth / 2 - titleWidth / 2, reglass$labelY(6), 0xFFFFFFFF, true);

        int tradesWidth = this.font.width(TRADES_LABEL);
        context.text(this.font, TRADES_LABEL, 53 - tradesWidth / 2, reglass$labelY(6), 0xFFFFFFFF, true);
        ci.cancel();
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void reglass$extractSelectedTradeArrow(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        MerchantOffers offers = this.menu.getOffers();
        if (offers.isEmpty() || this.shopItem < 0 || this.shopItem >= offers.size()) {
            return;
        }

        MerchantOffer offer = offers.get(this.shopItem);
        if (offer.isOutOfStock()) {
            return;
        }

        int arrowWidth = 20;
        int arrowHeight = 18;
        context.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                TRADE_ARROW_SPRITE,
                this.leftPos + 182 + (28 - arrowWidth) / 2,
                this.topPos + 35 + (21 - arrowHeight) / 2,
                arrowWidth,
                arrowHeight);
    }

    @Unique
    private int reglass$labelY(int y) {
        return y - 2;
    }
}
