package eleeter.unifystudiox.ui.launcher;

import eleeter.unifystudiox.launcher.ProjectEntry;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UIButton;
import eleeter.unifystudiox.ui.widgets.UILabel;

/**
 * A single row in the recent-projects list.
 * Displays the project name, its path, and Open / Delete action buttons.
 * Fires events through {@link IProjectManagerListener} — it has no knowledge
 * of how those events are handled.
 */
public class UIProjectRow extends UIPanel
{
    private static final float ROW_HEIGHT = 64.0F;
    private static final float BUTTON_W = 60.0F;
    private static final float BUTTON_H = 28.0F;
    private static final float BUTTON_GAP = 6.0F;

    /* Row hover colours */
    private static final float HOVER_R = 0.10F, HOVER_G = 0.11F, HOVER_B = 0.15F;
    private static final float NORMAL_R = 0.0F, NORMAL_G = 0.0F, NORMAL_B = 0.0F;

    private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private final ProjectEntry entry;
    private final UILabel nameLabel;
    private final UILabel pathLabel;
    private final UIButton openButton;
    private final UIButton deleteButton;

    public UIProjectRow(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, ProjectEntry entry,
                        IProjectManagerListener listener)
    {
        super(id);
        this.context = context;
        this.entry = entry;
        this.setBlocksInput(true);

        /* Project name — full width minus button area */
        this.nameLabel = new UILabel(id + "_name");
        this.nameLabel.setText(entry.getName());
        this.nameLabel.setTextColor(0.92F, 0.93F, 0.95F, 1.0F);
        this.nameLabel.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F)
                .setPixelOffset(14, 10)
                .setPixelSize(-(BUTTON_W * 2.0F + BUTTON_GAP * 3.0F + 14.0F), 22);
        this.addChild(this.nameLabel);

        /* Project path — smaller, muted */
        this.pathLabel = new UILabel(id + "_path");
        this.pathLabel.setText(entry.getPath());
        this.pathLabel.setTextColor(0.42F, 0.46F, 0.55F, 1.0F);
        this.pathLabel.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F)
                .setPixelOffset(14, 36)
                .setPixelSize(-(BUTTON_W * 2.0F + BUTTON_GAP * 3.0F + 14.0F), 18);
        this.addChild(this.pathLabel);

        /* Open button — blue accent for the primary action */
        float buttonY = (ROW_HEIGHT - BUTTON_H) * 0.5F;

        this.openButton = new UIButton(id + "_open", context);
        this.openButton.setText("Open");
        this.openButton.setColors(
                0.18F, 0.32F, 0.58F,
                0.22F, 0.38F, 0.66F,
                0.14F, 0.24F, 0.46F);
        this.openButton.getTransform().set(1.0F, 0.0F, 0.0F, 0.0F)
                .setAnchor(1.0F, 0.0F)
                .setPixelOffset((int) -(BUTTON_W + BUTTON_GAP), (int) buttonY)
                .setPixelSize((int) BUTTON_W, (int) BUTTON_H);
        this.openButton.setOnClick(() -> listener.onProjectOpened(this.entry));
        this.addChild(this.openButton);

        /* Delete button — muted red, less prominent */
        this.deleteButton = new UIButton(id + "_del", context);
        this.deleteButton.setText("\u2715");
        this.deleteButton.setColors(
                0.22F, 0.10F, 0.10F,
                0.32F, 0.14F, 0.14F,
                0.42F, 0.12F, 0.12F);
        this.deleteButton.getTransform().set(1.0F, 0.0F, 0.0F, 0.0F)
                .setAnchor(1.0F, 0.0F)
                .setPixelOffset((int) -BUTTON_GAP, (int) buttonY)
                .setPixelSize((int) BUTTON_W, (int) BUTTON_H);
        this.deleteButton.setOnClick(() -> listener.onProjectDeleted(this.entry.getPath()));
        this.addChild(this.deleteButton);
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = getComputedX();
        float y = getComputedY();
        float w = getComputedWidth();
        float h = getComputedHeight();

        if (this.context.isHoveredWithin(this))
        {
            renderer.drawRect(x, y, w, h, HOVER_R, HOVER_G, HOVER_B, 1.0F);
        }
        else
        {
            renderer.drawRect(x, y, w, h, NORMAL_R, NORMAL_G, NORMAL_B, 0.0F);
        }

        /* Bottom separator line */
        renderer.drawRect(x + 12.0F, y + h - 1.0F, w - 24.0F, 1.0F, 0.18F, 0.19F, 0.22F, 1.0F);
    }

    /** Returns the fixed height every project row occupies. */
    public static float getRowHeight()
    {
        return ROW_HEIGHT;
    }

    /** Releases GPU resources held by child buttons and labels. */
    public void cleanup()
    {
        this.openButton.cleanup();
        this.deleteButton.cleanup();
        this.nameLabel.cleanup();
        this.pathLabel.cleanup();
    }
}
