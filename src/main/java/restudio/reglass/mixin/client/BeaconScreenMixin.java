package restudio.reglass.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin extends AbstractContainerScreen<BeaconMenu> {
    protected BeaconScreenMixin(BeaconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void reglass$layoutButtons(CallbackInfo ci) {
        reglass$layoutBeaconControls();
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void reglass$relayoutButtons(CallbackInfo ci) {
        reglass$layoutBeaconControls();
    }

    private void reglass$layoutBeaconControls() {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        int primaryCols = 3;
        int primaryX = this.leftPos + 62 - (primaryCols * 28 - 4) / 2;
        int primaryY = this.topPos + 32;
        int secondaryX = this.leftPos + 168 - (2 * 28 - 4) / 2;
        int secondaryY = this.topPos + 46;

        int powerIndex = 0;
        for (GuiEventListener child : this.children()) {
            if (!(child instanceof AbstractWidget widget)) {
                continue;
            }

            String widgetName = widget.getClass().getName();
            if (widgetName.contains("BeaconConfirmButton")) {
                widget.setX(this.leftPos + 168);
                widget.setY(this.topPos + 104);
            } else if (widgetName.contains("BeaconCancelButton")) {
                widget.visible = false;
            } else if (widgetName.contains("BeaconPowerButton")) {
                widget.visible = true;
                if (powerIndex < 5) {
                    widget.setX(primaryX + powerIndex % primaryCols * 28);
                    widget.setY(primaryY + powerIndex / primaryCols * 28);
                } else {
                    int secondary = powerIndex - 5;
                    widget.setX(secondaryX + secondary % 2 * 28);
                    widget.setY(secondaryY);
                }
                powerIndex++;
            }
        }
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void reglass$extractPanels(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        reglass$panel(context, this.leftPos + 20, this.topPos + 28, 84, 58, 0.14f);
        reglass$panel(context, this.leftPos + 140, this.topPos + 42, 56, 30, 0.14f);

        reglass$renderPaymentItems(context);
        reglass$panel(context, this.leftPos + 168, this.topPos + 104, 22, 22, 0.14f);
    }

    @Redirect(
            method = "extractBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/item/ItemStack;II)V"
            )
    )
    private void reglass$skipVanillaPaymentItems(GuiGraphicsExtractor context, ItemStack itemStack, int x, int y) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            context.item(itemStack, x, y);
        }
    }

    @Redirect(
            method = "extractLabels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;centeredText(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            )
    )
    private void reglass$centeredText(GuiGraphicsExtractor context, Font font, Component text, int centerX, int y, int color) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            context.centeredText(font, text, centerX, y, color);
            return;
        }

        context.text(font, text, centerX - font.width(text) / 2, y - 2, 0xFFFFFFFF, true);
    }

    private void reglass$panel(GuiGraphicsExtractor context, int x, int y, int width, int height, float alpha) {
        ReGlassApi.create(context)
                .dimensions(x, y, width, height)
                .cornerRadius(8)
                .style(WidgetStyle.create().tint(0x000000, alpha).layer(1))
                .screenSpace()
                .render();
    }

    private void reglass$renderPaymentItems(GuiGraphicsExtractor context) {
        ItemStack[] items = new ItemStack[] {
                new ItemStack(Items.NETHERITE_INGOT),
                new ItemStack(Items.EMERALD),
                new ItemStack(Items.DIAMOND),
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.IRON_INGOT)
        };

        int startX = this.leftPos + 14;
        int y = this.topPos + 106;
        for (int i = 0; i < items.length; i++) {
            int x = startX + i * 23;
            reglass$panel(context, x - 2, y - 2, 20, 20, 0.13f);
            context.item(items[i], x, y);
        }
    }
}
