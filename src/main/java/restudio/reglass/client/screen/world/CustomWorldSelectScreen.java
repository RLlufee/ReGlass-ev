package restudio.reglass.client.screen.world;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;
import restudio.reglass.client.screen.widget.world.WorldListEntryWidget;
import restudio.reglass.client.screen.widget.ScrollableListWidget;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomWorldSelectScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Screen parent;
    private ScrollableListWidget<WorldListEntryWidget> worldList;
    private TextFieldWidget searchBox;
    private Button playButton, createButton, editButton, deleteButton;

    public CustomWorldSelectScreen(Screen parent) {
        super(Component.translatable("selectWorld.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int listWidth = this.width - 150;
        this.worldList = new ScrollableListWidget<>(this, 20, 50, listWidth - 40, this.height - 100, 36);
        this.worldList.setVerticalPadding(5);

        this.searchBox = new TextFieldWidget(this.font, 20, 20, listWidth - 40, 20, Component.translatable("selectWorld.search"));
        this.searchBox.setChangedListener(this::filterWorlds);

        this.loadWorldList();

        this.addRenderableWidget(this.worldList);
        this.addRenderableWidget(this.searchBox);

        int buttonWidth = 120;

        this.playButton = Button.builder(Component.translatable("selectWorld.select"), button -> play(getSelectedSummaries())).bounds(listWidth, 50, buttonWidth, 20).build();
        this.createButton = Button.builder(Component.translatable("selectWorld.create"), button -> CreateWorldScreen.show(this.minecraft, null)).bounds(listWidth, 80, buttonWidth, 20).build();
        this.editButton = Button.builder(Component.translatable("selectWorld.edit"), button -> {
            Set<LevelSummary> summaries = getSelectedSummaries();
            if (summaries.size() == 1) {
                LevelSummary summary = summaries.iterator().next();
                try {
                    LevelStorage.Session session = this.minecraft.getLevelStorage().createSession(summary.getName());
                    this.minecraft.setScreen(EditWorldScreen.create(this.minecraft, session, (saved) -> {
                        if (saved) {
                            this.loadWorldList();
                        }
                        this.minecraft.setScreen(this);
                    }));
                } catch (IOException e) {
                    LOGGER.error("Failed to access world {}", summary.getName(), e);
                    SystemToast.addWorldAccessFailureToast(this.minecraft, summary.getName());
                } catch (SymlinkValidationException e) {
                    LOGGER.warn("Failed to validate symlinks for world {}", summary.getName(), e);
                    SystemToast.addWorldAccessFailureToast(this.minecraft, summary.getName());
                }
            }
        }).bounds(listWidth, 100, buttonWidth, 20).build();
        this.deleteButton = Button.builder(Component.translatable("selectWorld.delete"), button -> this.delete(getSelectedSummaries())).bounds(listWidth, 120, buttonWidth, 20).build();

        this.addRenderableWidget(this.playButton);
        this.addRenderableWidget(this.createButton);
        this.addRenderableWidget(this.editButton);
        this.addRenderableWidget(this.deleteButton);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.minecraft.setScreen(this.parent)).bounds(listWidth, this.height - 40, buttonWidth, 20).build());

        updateButtonStates();
    }

    private void loadWorldList() {
        this.worldList.clearEntries();
        try {
            LevelStorage.LevelList levelList = this.minecraft.getLevelStorage().getLevelList();
            List<LevelSummary> summaries = this.minecraft.getLevelStorage().loadSummaries(levelList).join();

            for (LevelSummary summary : summaries) {
                this.worldList.addEntry(new WorldListEntryWidget(this, summary, 0, 0, 30));
            }
        } catch (LevelStorageException e) {
            LOGGER.error("Couldn't load worlds", e);
        }
    }

    private void filterWorlds(String filter) {
        this.loadWorldList();
        if (!filter.isEmpty()) {
            String lowerFilter = filter.toLowerCase();
            this.worldList.getEntries().removeIf(entry -> !entry.getSummary().getDisplayName().toLowerCase().contains(lowerFilter) && !entry.getSummary().getName().toLowerCase().contains(lowerFilter));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.worldList.render(context, mouseX, mouseY, delta);
        this.searchBox.render(context, mouseX, mouseY, delta);

        context.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        super.extractRenderState(context, mouseX, mouseY, delta);

        updateButtonStates();
    }

    private void updateButtonStates() {
        int selectionCount = this.worldList.getSelectedEntries().size();
        this.playButton.active = selectionCount == 1;
        this.editButton.active = selectionCount == 1;
        this.deleteButton.active = selectionCount > 0;
    }

    public ScrollableListWidget<WorldListEntryWidget> getList() {
        return this.worldList;
    }

    public void play(Set<LevelSummary> summaries) {
        if (summaries.size() == 1) {
            LevelSummary summary = summaries.iterator().next();
            client.createIntegratedServerLoader().start(summary.getName(), () -> this.minecraft.setScreen(this));
        }
    }

    public void delete(Set<LevelSummary> summaries) {
        if (summaries.isEmpty()) return;

        Component title = Component.translatable("selectWorld.deleteQuestion");
        Component message = Component.translatable("selectWorld.deleteWarning", summaries.stream().map(LevelSummary::getDisplayName).collect(Collectors.joining(", ")));

        this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                try {
                    for (LevelSummary summary : summaries) {
                        try (LevelStorage.Session session = this.minecraft.getLevelStorage().createSession(summary.getName())) {
                            session.deleteSessionLock();
                        } catch (SymlinkValidationException e) {
                            LOGGER.warn("Failed to validate symlinks for world {}", summary.getName(), e);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to delete worlds", e);
                }
                this.loadWorldList();
            }
            this.minecraft.setScreen(this);
        }, title, message));
    }

    private Set<LevelSummary> getSelectedSummaries() {
        return this.worldList.getSelectedEntries().stream()
                .map(WorldListEntryWidget::getSummary)
                .collect(Collectors.toSet());
    }

    @Override
    public void close() {
        this.minecraft.setScreen(this.parent);
    }
}