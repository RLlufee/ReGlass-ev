package restudio.reglass.client.screen.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import restudio.reglass.client.LiquidGlassWidget;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.client.config.ReGlassSettingsIO;
import restudio.reglass.client.ui.MappedSlider;

public class ReGlassConfigScreen extends Screen {
    private final Screen parent;
    private final List<PositionedWidget> positionedWidgets = new ArrayList<>();
    private LiquidGlassWidget previewCircle;
    private LiquidGlassWidget previewRounded;
    private double scrollPosition;
    private int totalListHeight;

    private record PositionedWidget(AbstractWidget widget, int y) {}

    public ReGlassConfigScreen(Screen parent) {
        super(Component.translatable("reglass.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        positionedWidgets.clear();

        int listWidth = Math.min(300, this.width / 2 - 20);
        int widgetWidth = listWidth - 20;
        int widgetX = 10 + 10;

        int y = 5;
        int gap = 4;
        int widgetHeight = 20;

        ReGlassConfig cfg = ReGlassConfig.INSTANCE;

        addTitle("reglass.config.category.general", widgetX, y, widgetWidth);
        y += widgetHeight;

        Button enableRedesignButton = Button.builder(getEnableRedesignText(), button -> {
            cfg.features.enableRedesign = !cfg.features.enableRedesign;
            button.setMessage(getEnableRedesignText());
            this.minecraft.gui.setScreen(new ReGlassConfigScreen(this.parent));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(enableRedesignButton, y);
        y += widgetHeight + gap;

        Button enableButtonsButton = Button.builder(getFeatureText("reglass.config.feature.buttons", cfg.features.buttons), button -> {
            cfg.features.buttons = !cfg.features.buttons;
            button.setMessage(getFeatureText("reglass.config.feature.buttons", cfg.features.buttons));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(enableButtonsButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

        Button enableSlidersButton = Button.builder(getFeatureText("reglass.config.feature.sliders", cfg.features.sliders), button -> {
            cfg.features.sliders = !cfg.features.sliders;
            button.setMessage(getFeatureText("reglass.config.feature.sliders", cfg.features.sliders));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(enableSlidersButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

        Button enableHotbarButton = Button.builder(getFeatureText("reglass.config.feature.hotbar", cfg.features.hotbar), button -> {
            cfg.features.hotbar = !cfg.features.hotbar;
            button.setMessage(getFeatureText("reglass.config.feature.hotbar", cfg.features.hotbar));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(enableHotbarButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

        Button enableContainersButton = Button.builder(getFeatureText("reglass.config.feature.containers", cfg.features.containers), button -> {
            cfg.features.containers = !cfg.features.containers;
            button.setMessage(getFeatureText("reglass.config.feature.containers", cfg.features.containers));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(enableContainersButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

        Button containerTextPanelsButton = Button.builder(getFeatureText("reglass.config.feature.container_text_panels", cfg.features.containerTextPanels), button -> {
            cfg.features.containerTextPanels = !cfg.features.containerTextPanels;
            button.setMessage(getFeatureText("reglass.config.feature.container_text_panels", cfg.features.containerTextPanels));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(containerTextPanelsButton, y).active = cfg.features.enableRedesign && cfg.features.containers;
        y += widgetHeight + gap;

        Button cancelDarkeningButton = Button.builder(getFeatureText("reglass.config.feature.cancel_screen_darkening", cfg.features.cancelScreenDarkening), button -> {
            cfg.features.cancelScreenDarkening = !cfg.features.cancelScreenDarkening;
            button.setMessage(getFeatureText("reglass.config.feature.cancel_screen_darkening", cfg.features.cancelScreenDarkening));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(cancelDarkeningButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

        Button enableTooltipsButton = Button.builder(getFeatureText("reglass.config.feature.tooltips", cfg.features.tooltips), button -> {
            cfg.features.tooltips = !cfg.features.tooltips;
            button.setMessage(getFeatureText("reglass.config.feature.tooltips", cfg.features.tooltips));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(enableTooltipsButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.tooltip_opacity"), 0.0f, 1.0f, cfg.features.tooltipOpacity, v -> cfg.features.tooltipOpacity = v.floatValue()), y).active = cfg.features.enableRedesign && cfg.features.tooltips;
        y += widgetHeight + gap * 2;

        addTitle("reglass.config.category.appearance", widgetX, y, widgetWidth);
        y += widgetHeight;

        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.tint_alpha"), 0f, 1f, cfg.defaultTintAlpha, v -> cfg.defaultTintAlpha = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.intSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.blur_radius"), 0, 32, cfg.defaultBlurRadius, v -> cfg.defaultBlurRadius = v), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.smoothing"), -0.02f, 0.02f, cfg.defaultSmoothing, v -> cfg.defaultSmoothing = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.intSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.default_layer"), 0, 15, cfg.defaultLayer, v -> cfg.defaultLayer = v), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap * 2;

        addTitle("reglass.config.category.shadow", widgetX, y, widgetWidth);
        y += widgetHeight;

        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.shadow_expand"), 0f, 100f, cfg.defaultShadowExpand, v -> cfg.defaultShadowExpand = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.shadow_factor"), 0f, 1f, cfg.defaultShadowFactor, v -> cfg.defaultShadowFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.shadow_offset_y"), -10f, 10f, cfg.defaultShadowOffsetY, v -> cfg.defaultShadowOffsetY = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap * 2;

        addTitle("reglass.config.category.refraction", widgetX, y, widgetWidth);
        y += widgetHeight;

        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.ref_thickness"), 1f, 60f, cfg.defaultRefThickness, v -> cfg.defaultRefThickness = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.ref_factor"), 1.0f, 2.5f, cfg.defaultRefFactor, v -> cfg.defaultRefFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.fresnel_range"), 0f, 60f, cfg.defaultRefFresnelRange, v -> cfg.defaultRefFresnelRange = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.fresnel_hardness"), 0f, 100f, cfg.defaultRefFresnelHardness, v -> cfg.defaultRefFresnelHardness = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.fresnel_factor"), 0f, 100f, cfg.defaultRefFresnelFactor, v -> cfg.defaultRefFresnelFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap * 2;

        addTitle("reglass.config.category.glare", widgetX, y, widgetWidth);
        y += widgetHeight;

        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.glare_range"), 0f, 60f, cfg.defaultGlareRange, v -> cfg.defaultGlareRange = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.glare_factor"), 0f, 100f, cfg.defaultGlareFactor, v -> cfg.defaultGlareFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap * 2;

        addTitle("reglass.config.category.interactions", widgetX, y, widgetWidth);
        y += widgetHeight;

        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.hover_scale"), 0f, 6f, cfg.hoverScalePx, v -> cfg.hoverScalePx = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.focus_scale"), 0f, 8f, cfg.focusScalePx, v -> cfg.focusScalePx = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.focus_border_width"), 0f, 6f, cfg.focusBorderWidthPx, v -> cfg.focusBorderWidthPx = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.focus_border_intensity"), 0f, 1f, cfg.focusBorderIntensity, v -> cfg.focusBorderIntensity = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.focus_border_speed"), 0f, 4f, cfg.focusBorderSpeed, v -> cfg.focusBorderSpeed = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap * 2;

        addTitle("reglass.config.category.debug", widgetX, y, widgetWidth);
        y += widgetHeight;

        Button pixelatedGridButton = Button.builder(getFeatureText("reglass.config.feature.pixelated_grid", cfg.features.pixelatedGrid), button -> {
            cfg.features.pixelatedGrid = !cfg.features.pixelatedGrid;
            button.setMessage(getFeatureText("reglass.config.feature.pixelated_grid", cfg.features.pixelatedGrid));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(pixelatedGridButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.grid_size"), 1f, 32f, cfg.pixelatedGridSize, v -> cfg.pixelatedGridSize = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.intSlider(widgetX, y, widgetWidth, widgetHeight, Component.translatable("reglass.config.slider.debug_step"), 0, 9, Math.round(cfg.debugStep), v -> cfg.debugStep = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

        this.totalListHeight = y;

        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), b -> {
            ReGlassSettingsIO.apply(new ReGlassSettingsIO.Data());
            if (this.minecraft != null) {
                this.minecraft.gui.setScreen(new ReGlassConfigScreen(this.parent));
            }
        }).bounds(this.width / 2 - 100, this.height - 28, 98, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose()).bounds(this.width / 2 + 2, this.height - 28, 98, 20).build());

        int previewX = this.width / 2 + 20;
        int previewY = this.height / 2 - 50;

        WidgetStyle s1 = WidgetStyle.create().tint(0xFFFFFF, Math.min(1f, Math.max(0f, cfg.defaultTintAlpha))).blurRadius(cfg.defaultBlurRadius)
                .shadow(cfg.defaultShadowExpand, cfg.defaultShadowFactor, cfg.defaultShadowOffsetX, cfg.defaultShadowOffsetY)
                .shadowColor(cfg.defaultShadowColor, cfg.defaultShadowColorAlpha)
                .refractionThickness(cfg.defaultRefThickness).refractionFactor(cfg.defaultRefFactor).refractionDispersion(cfg.defaultRefDispersion)
                .fresnelRange(cfg.defaultRefFresnelRange).fresnelHardness(cfg.defaultRefFresnelHardness).fresnelFactor(cfg.defaultRefFresnelFactor)
                .glareRange(cfg.defaultGlareRange).glareHardness(cfg.defaultGlareHardness).glareConvergence(cfg.defaultGlareConvergence)
                .glareOppositeFactor(cfg.defaultGlareOppositeFactor).glareFactor(cfg.defaultGlareFactor).glareAngleRad(cfg.defaultGlareAngleRad)
                .layer(1);

        previewCircle = addRenderableWidget(new LiquidGlassWidget(previewX + 40, previewY, 100, 100, s1).setCornerRadiusPx(50f));

        WidgetStyle s2 = WidgetStyle.create().tint(cfg.defaultTintColor, cfg.defaultTintAlpha).blurRadius(cfg.defaultBlurRadius)
                .shadow(cfg.defaultShadowExpand, cfg.defaultShadowFactor, cfg.defaultShadowOffsetX, cfg.defaultShadowOffsetY)
                .shadowColor(cfg.defaultShadowColor, cfg.defaultShadowColorAlpha)
                .layer(0);

        previewRounded = addRenderableWidget(new LiquidGlassWidget(previewX + 20, previewY + 120, 140, 60, s2).setCornerRadiusPx(16f));
    }

    private Component getEnableRedesignText() {
        return Component.translatable("reglass.config.feature.enable_redesign").append(": ").append(ReGlassConfig.INSTANCE.features.enableRedesign ? Component.translatable("options.on") : Component.translatable("options.off"));
    }

    private Component getFeatureText(String key, boolean enabled) {
        return Component.translatable(key).append(": ").append(enabled ? Component.translatable("options.on") : Component.translatable("options.off"));
    }

    private <T extends AbstractWidget> T addPositionedWidget(T widget, int y) {
        positionedWidgets.add(new PositionedWidget(widget, y));
        return addRenderableWidget(widget);
    }

    private MappedSlider addSlider(MappedSlider slider, int y) {
        return addPositionedWidget(slider, y);
    }

    private void addTitle(String key, int x, int y, int width) {
        addPositionedWidget(new TitleWidget(x, y, width, 20, Component.translatable(key)), y);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int listTop = 32;
        int listBottom = this.height - 32;

        for (PositionedWidget pw : positionedWidgets) {
            pw.widget.setY(pw.y() + listTop - (int) this.scrollPosition);
            if (pw.widget() instanceof TitleWidget tw) {
                tw.visible = (pw.widget.getY() >= listTop && (pw.widget.getY() + 20) <= listBottom);
            } else {
                pw.widget.visible = (pw.widget.getY() >= listTop && (pw.widget.getY() + pw.widget.getHeight()) <= listBottom);
            }
        }

        super.extractRenderState(context, mouseX, mouseY, delta);

        context.centeredText(this.font, Component.translatable("reglass.config.title"), this.width / 2, 15, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int listHeight = (this.height - 32) - 32;
        int maxScroll = Math.max(0, this.totalListHeight - listHeight);
        if (maxScroll > 0) {
            this.scrollPosition -= verticalAmount * 10;
            this.scrollPosition = Mth.clamp(this.scrollPosition, 0, maxScroll);
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        ReGlassSettingsIO.saveFromMemory();
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    private class TitleWidget extends AbstractWidget {
        public TitleWidget(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
        }

        @Override
        public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            if (this.visible) {
                context.centeredText(ReGlassConfigScreen.this.font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, 0xFFFFFF);
            }
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {}
    }
}