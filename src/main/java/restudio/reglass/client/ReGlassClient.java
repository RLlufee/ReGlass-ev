package restudio.reglass.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.client.config.ReGlassSettingsIO;
import restudio.reglass.mixin.accessor.OptionsAccessor;
import restudio.reglass.client.screen.config.ReGlassConfigScreen;

import java.util.Arrays;

public class ReGlassClient implements ClientModInitializer {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("reglass", "main"));
    private static final KeyMapping CONFIG_KEY = new KeyMapping("key.reglass.config", InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    private static final KeyMapping PLAYGROUND_KEY = new KeyMapping("key.reglass.playground", InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    private static final KeyMapping TOGGLE_REDESIGN_KEY = new KeyMapping("key.reglass.toggle_redesign", InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    private static boolean keyMappingsRegistered;
    private static boolean toggleRedesignWasDown;

    public static Minecraft minecraftClient;

    @Override
    public void onInitializeClient() {
        minecraftClient = Minecraft.getInstance();

        ReGlassSettingsIO.loadIntoMemory();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!keyMappingsRegistered) {
                if (client.options == null) {
                    return;
                }
                registerKeyMappings(client);
            }
            handleGlobalToggleRedesignKey(client);
            while (CONFIG_KEY.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new ReGlassConfigScreen(null));
                }
            }
            while (PLAYGROUND_KEY.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new PlaygroundScreen());
                }
            }
        });
    }

    private static void registerKeyMappings(Minecraft client) {
        KeyMapping[] existingMappings = client.options.keyMappings;
        KeyMapping[] mappings = Arrays.copyOf(existingMappings, existingMappings.length + 3);
        mappings[existingMappings.length] = CONFIG_KEY;
        mappings[existingMappings.length + 1] = PLAYGROUND_KEY;
        mappings[existingMappings.length + 2] = TOGGLE_REDESIGN_KEY;
        ((OptionsAccessor) client.options).setKeyMappings(mappings);
        KeyMapping.resetMapping();
        client.options.load();
        keyMappingsRegistered = true;
    }

    private static void handleGlobalToggleRedesignKey(Minecraft client) {
        boolean down = isGlobalKeyDown(client, TOGGLE_REDESIGN_KEY);
        if (down && !toggleRedesignWasDown) {
            ReGlassConfig.INSTANCE.features.enableRedesign = !ReGlassConfig.INSTANCE.features.enableRedesign;
            ReGlassSettingsIO.saveFromMemory();
        }
        toggleRedesignWasDown = down;
    }

    private static boolean isGlobalKeyDown(Minecraft client, KeyMapping keyMapping) {
        if (keyMapping.isUnbound()) {
            return false;
        }
        InputConstants.Key key = InputConstants.getKey(keyMapping.saveString());
        if (key.getType() == InputConstants.Type.MOUSE) {
            int btn = key.getValue();
            if (btn == 0) return client.mouseHandler.isLeftPressed();
            if (btn == 1) return client.mouseHandler.isRightPressed();
            if (btn == 2) return client.mouseHandler.isMiddlePressed();
            return false;
        }
        return InputConstants.isKeyDown(key.getValue());
    }

    public static class PlaygroundScreen extends Screen {
        private boolean blur;

        public PlaygroundScreen() {
            super(Component.literal("ReGlass Playground"));
        }

        @Override
        protected void init() {
            super.init();

            WidgetStyle customStyle = WidgetStyle.create().tint(0xFFAA00, 0.4f).blurRadius(0).shadow(25f, 0.2f, 0f, 3f).smoothing(.05f).shadowColor(0x000000, 1.0f);
            addRenderableWidget(new LiquidGlassWidget(width / 2 - 75, height / 2 - 25, 150, 50, customStyle).setMoveable(true));
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            context.text(minecraftClient.font, Component.literal("This is a Minecraft Screen"), width / 2 - 70, 10, 0xFFFFFFFF, true);
            super.extractRenderState(context, mouseX, mouseY, delta);
        }

        @Override
        public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            if (blur) super.extractBackground(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean isDouble) {
            if (click.button() == 1) {
                addRenderableWidget(new LiquidGlassWidget((int) click.x() - 50, (int) click.y() - 50, 100, 100, WidgetStyle.create().smoothing(.05f))).setMoveable(true);
                return true;
            }
            return super.mouseClicked(click, isDouble);
        }
    }
}
