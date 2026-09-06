package restudio.reglass.client.screen.widget.world;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.client.screen.widget.ScrollableListWidget;
import restudio.reglass.client.screen.world.CustomWorldSelectScreen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.function.Supplier;

public class WorldListEntryWidget extends ScrollableListWidget.Entry<WorldListEntryWidget> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier DEFAULT_ICON_ID = Identifier.fromNamespaceAndPath("textures/misc/unknown_server.png");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());

    private final Minecraft client;
    private final CustomWorldSelectScreen parent;
    private final LevelSummary summary;
    private final Identifier iconId;
    private final WidgetStyle defaultStyle = new WidgetStyle().tint(0x000000, 0.1f);
    private final WidgetStyle hoveredStyle = new WidgetStyle().tint(0xFFFFFF, 0.1f);
    private final WidgetStyle selectedStyle = new WidgetStyle().tint(0xFFFFFF, 0.2f);

    private DynamicTexture iconTexture;

    public WorldListEntryWidget(CustomWorldSelectScreen parent, LevelSummary summary, int x, int y, int height) {
        super(x, y, parent.width - 150 - 40, height);
        this.parent = parent;
        this.summary = summary;
        this.minecraft = Minecraft.getInstance();
        String safeName = summary.getName().toLowerCase().replaceAll("[^a-z0-9/._-]", "_");
        this.iconId = Identifier.fromNamespaceAndPath("world-select/icon/" + safeName);

        loadIcon();
    }

    private void loadIcon() {
        File iconFile = summary.getIconPath().toFile();
        if (Files.isRegularFile(iconFile.toPath())) {
            try (InputStream inputStream = Files.newInputStream(iconFile.toPath())) {
                NativeImage image = NativeImage.read(inputStream);
                if (this.iconTexture != null) {
                    this.iconTexture.close();
                }
                Supplier<String> nativeImageSupplier = () -> {
                    try {
                        return Files.readString(iconFile.toPath());
                    } catch (IOException e) {
                        LOGGER.error("Failed to read world icon for {}", summary.getName(), e);
                        return null;
                    }
                };
                this.iconTexture = new DynamicTexture(nativeImageSupplier, image);
                this.minecraft.getTextureManager().registerTexture(this.iconId, this.iconTexture);
            } catch (Exception e) {
                LOGGER.error("Failed to load world icon for {}", summary.getName(), e);
                this.iconTexture = null;
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int index, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        super.extractRenderState(context, index, x, y, width, height, mouseX, mouseY, hovered, delta);

        boolean isSelected = this.parent.getList().getSelectedEntries().contains(this);

        WidgetStyle style = defaultStyle;
        if (isSelected) {
            style = selectedStyle;
        } else if (hovered) {
            style = hoveredStyle;
        }

        ReGlassApi.create(context)
                .bounds(x, y, width, height)
                .cornerRadius(8)
                .style(style)
                .hover(hovered ? 1f : 0f)
                .focus(isSelected ? 1f : 0f)
                .render();

        String displayName = summary.getDisplayName();
        String name = summary.getName();
        long lastPlayed = summary.getLastPlayed();
        if (lastPlayed != -1L) {
            name = name + " (" + DATE_FORMAT.format(Instant.ofEpochMilli(lastPlayed)) + ")";
        }

        if (displayName == null || displayName.isEmpty()) {
            displayName = Component.translatable("selectWorld.world").getString() + " " + (index + 1);
        }

        MutableComponent details = (MutableComponent) summary.getDetails();

        context.text(client.font, displayName, x + 40, y + 2, 0xFFFFFFFF);
        context.text(client.font, name, x + 40, y + 10 + 3, 0xFF808080);
        context.text(client.font, details, x + 40, y + 10 + 9 + 3, 0xFF808080);

        Identifier texture = this.iconTexture != null ? this.iconId : DEFAULT_ICON_ID;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x + 2, y + 2, 0, 0, 32, 32, 32, 32);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click) {
        if (isMouseOver(click.x(), click.y())) {
            parent.getList().setSelected(this);
            return true;
        }
        return false;
    }

    public LevelSummary getSummary() {
        return this.summary;
    }

    @Override
    public void close() {
        if (this.iconTexture != null) {
            this.minecraft.getTextureManager().destroyTexture(this.iconId);
            this.iconTexture.close();
            this.iconTexture = null;
        }
    }
}