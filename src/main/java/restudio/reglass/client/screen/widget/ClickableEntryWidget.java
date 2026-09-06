package restudio.reglass.client.screen.widget;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public abstract class ClickableEntryWidget<P> extends AbstractWidget {
    protected final P parent;

    public ClickableEntryWidget(P parent, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.parent = parent;
    }
}