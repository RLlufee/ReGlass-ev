package restudio.reglass.client;

import net.minecraft.world.item.DyeColor;

public final class ContainerColorContext {
    private static DyeColor lastShulkerColor;

    private ContainerColorContext() {
    }

    public static void setLastShulkerColor(DyeColor color) {
        lastShulkerColor = color;
    }

    public static DyeColor getLastShulkerColor() {
        return lastShulkerColor;
    }
}
