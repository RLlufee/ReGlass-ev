package restudio.reglass.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.mixin.accessor.SlotAccessor;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow private static CreativeModeTab selectedTab;
    @Shadow private EditBox searchBox;
    @Shadow private Slot destroyItemSlot;

    protected CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void reglass$alignSearchBox(CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers || this.searchBox == null) {
            return;
        }

        this.searchBox.setY(this.topPos + 4);
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void reglass$spaceSurvivalTab(CreativeModeTab tab, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers || selectedTab.getType() != CreativeModeTab.Type.INVENTORY) {
            return;
        }

        for (int i = 0; i < this.menu.slots.size(); i++) {
            Slot slot = this.menu.slots.get(i);
            if (slot == this.destroyItemSlot) {
                reglass$setSlotPosition(slot, 181, 120);
            } else if (i >= 5 && i <= 8) {
                int armorIndex = i - 5;
                reglass$setSlotPosition(slot, 54 + armorIndex / 2 * 54, armorIndex % 2 * 27);
            } else if (i == 45) {
                reglass$setSlotPosition(slot, 24, 14);
            } else if (i >= 36 && i <= 44) {
                reglass$setSlotPosition(slot, 9 + (i - 36) * 18, 120);
            }
        }
    }

    @Inject(method = "getTabY", at = @At("HEAD"), cancellable = true)
    private void reglass$getTabY(CreativeModeTab tab, CallbackInfoReturnable<Integer> cir) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        cir.setReturnValue(tab.row() == CreativeModeTab.Row.TOP ? -36 : this.imageHeight + 4);
    }

    @Inject(method = "extractTabButton", at = @At("HEAD"), cancellable = true)
    private void reglass$extractTabButton(GuiGraphicsExtractor context, int mouseX, int mouseY, CreativeModeTab tab, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        boolean selected = tab == selectedTab;
        boolean top = tab.row() == CreativeModeTab.Row.TOP;
        int x = this.leftPos + reglass$getTabX(tab);
        int y = this.topPos + (top ? -34 : this.imageHeight + 6);
        int blobX = x + 1;
        int blobY = y + (top ? 5 : 3);

        ReGlassApi.create(context)
                .dimensions(blobX, blobY, 24, 24)
                .cornerRadius(12)
                .hover(selected ? 1f : 0f)
                .style(WidgetStyle.create()
                        .tint(0x000000, selected ? 0.22f : 0.12f)
                        .layer(selected ? 3 : 2))
                .screenSpace()
                .render();

        context.item(tab.getIconItem(), x + 5, y + (top ? 9 : 7));
        ci.cancel();
    }

    @Inject(method = "extractLabels", at = @At("HEAD"), cancellable = true)
    private void reglass$extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        if (selectedTab.showTitle()) {
            context.text(this.font, selectedTab.getDisplayName(), 8, 4, 0xFFFFFFFF, true);
        }
        ci.cancel();
    }

    @Unique
    private void reglass$setSlotPosition(Slot slot, int x, int y) {
        ((SlotAccessor) slot).reglass$setX(x);
        ((SlotAccessor) slot).reglass$setY(y);
    }

    @Unique
    private int reglass$getTabX(CreativeModeTab tab) {
        int column = tab.column();
        if (tab.isAlignedRight()) {
            return this.imageWidth - 27 * (7 - column) + 1;
        }
        return 27 * column;
    }
}
