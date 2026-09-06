package restudio.reglass.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

@Mixin(Hud.class)
public abstract class InGameHudMixin {

    @Shadow @Final private Minecraft minecraft;

    @Shadow
    protected abstract void extractSlot(
            GuiGraphicsExtractor context,
            int x,
            int y,
            DeltaTracker tickCounter,
            Player player,
            ItemStack stack,
            int seed
    );

    @Unique private double reglass$slotBlobX = Double.NaN;
    @Unique private int reglass$lastSelected = -1;

    @Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true)
    private void reglass$onRenderHotbar(
            GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci
    ) {
        if (!ReGlassConfig.INSTANCE.features.enableRedesign
                || !ReGlassConfig.INSTANCE.features.hotbar) {
            return;
        }
        if (this.minecraft.gameMode != null && this.minecraft.gameMode.isSpectator()) {
            return;
        }

        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }

        if (this.minecraft.gui.hud.isHidden()) {
            return;
        }

        ci.cancel();

        int hotbarWidth = 182;
        int hotbarHeight = 22;
        int x = this.minecraft.getWindow().getGuiScaledWidth() / 2 - hotbarWidth / 2;
        int offhandY = this.minecraft.getWindow().getGuiScaledHeight() - hotbarHeight;

        ReGlassApi.create(context)
                .dimensions(x, offhandY, hotbarWidth, hotbarHeight)
                .cornerRadius(11)
                .style(new WidgetStyle().tint(0x000000, 0.3f).layer(0))
                .render();

        int selectedSlot = player.getInventory().getSelectedSlot();

        int targetCircleX = x - 1 + selectedSlot * 20;
        if (Double.isNaN(this.reglass$slotBlobX)) {
            this.reglass$slotBlobX = targetCircleX;
        }

        double deltaTicks = tickCounter.getRealtimeDeltaTicks();
        double deltaSeconds = deltaTicks / 20.0;

        double tau = 0.08;
        double alpha = 1.0 - Math.exp(-deltaSeconds / tau);
        if (alpha < 0.0) alpha = 0.0;
        if (alpha > 1.0) alpha = 1.0;

        this.reglass$slotBlobX += (targetCircleX - this.reglass$slotBlobX) * alpha;

        int circleX = (int) Math.round(this.reglass$slotBlobX) + 1;

        WidgetStyle selectorStyle = new WidgetStyle().smoothing(-0.005f).layer(1);

        ReGlassApi.create(context)
                .dimensions(circleX, offhandY, hotbarHeight, hotbarHeight)
                .cornerRadius(0.5f * hotbarHeight)
                .style(selectorStyle)
                .focus(1f)
                .render();

        for (int i = 0; i < 9; ++i) {
            int itemX = x + 3 + i * 20;
            int itemY = offhandY + 3;
            this.extractSlot(
                    context,
                    itemX,
                    itemY,
                    tickCounter,
                    player,
                    player.getInventory().getItem(i),
                    i + 1
            );
        }

        ItemStack offHandStack = player.getOffhandItem();
        if (!offHandStack.isEmpty()) {
            HumanoidArm arm = player.getMainArm().getOpposite();
            int offhandX = (arm == HumanoidArm.LEFT ? x - hotbarHeight - 4 : x + hotbarWidth + 4);

            ReGlassApi.create(context)
                    .dimensions(offhandX, offhandY, hotbarHeight, hotbarHeight)
                    .cornerRadius(hotbarHeight * 0.5f)
                    .style(new WidgetStyle().tint(0x000000, 0.3f).layer(0))
                    .render();

            this.extractSlot(context, offhandX + 3, offhandY + 3, tickCounter, player, offHandStack, 0);
        }

        LiquidGlassUniforms.get().tryApplyBlur(context);
    }

    @Shadow
    private Player getCameraPlayer() {
        throw new AssertionError("Mixin application failed!");
    }
}
