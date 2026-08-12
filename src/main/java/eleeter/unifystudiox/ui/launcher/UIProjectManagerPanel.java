package eleeter.unifystudiox.ui.launcher;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.launcher.IProjectRegistry;
import eleeter.unifystudiox.launcher.ProjectEntry;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UIButton;
import eleeter.unifystudiox.ui.widgets.UILabel;
import eleeter.unifystudiox.ui.widgets.UIScrollPanel;
import eleeter.unifystudiox.ui.widgets.UITextField;

/**
 * The full Project Manager UI panel. Fills the launcher window and composes
 * the recent-projects list with the new-project form. Implements
 * {@link IProjectManagerListener} so it can intercept delete/create calls to
 * refresh the list before forwarding to the outer listener.
 */
public class UIProjectManagerPanel extends UIPanel implements IProjectManagerListener
{
    private static final float LEFT_W = 300.0F;
    private static final float HEADER_H = 72.0F;
    private static final float CONTENT_PAD = 28.0F;
    private static final float FIELD_H = 36.0F;
    private static final float BTN_H = 40.0F;
    private static final float LABEL_H = 18.0F;
    private static final float SECTION_H = 20.0F;

    private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private final IProjectRegistry registry;
    private final IProjectManagerListener outerListener;

    private UIScrollPanel projectList;
    private UITextField fieldName;
    private UITextField fieldPath;

    private final List<UIProjectRow> activeRows = new ArrayList<>();
    private final List<Runnable> cleanupActions = new ArrayList<>();

    public UIProjectManagerPanel(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context,
            IProjectRegistry registry, IProjectManagerListener outerListener)
    {
        super(id);
        this.context = context;
        this.registry = registry;
        this.outerListener = outerListener;
        this.setBlocksInput(true);
        buildLeftPanel();
        buildRightPanel();
    }

    /* ---- IProjectManagerListener ---- */

    @Override
    public void onProjectOpened(ProjectEntry entry)
    {
        this.outerListener.onProjectOpened(entry);
    }

    @Override
    public void onNewProjectRequested(String name, String path)
    {
        ProjectEntry entry = new ProjectEntry(name, path);
        this.registry.add(entry);
        this.refreshProjectList();
        this.fieldName.setValue("");
        this.fieldPath.setValue("");
        this.outerListener.onProjectOpened(entry);
    }

    @Override
    public void onProjectDeleted(String path)
    {
        this.registry.remove(path);
        this.refreshProjectList();
    }

    @Override
    public void onExitRequested()
    {
        this.outerListener.onExitRequested();
    }

    /* ---- Layout builders ---- */

    private void buildLeftPanel()
    {
        UIPanel left = new UIPanel("pm_left");
        left.setBackgroundColor(0.040F, 0.044F, 0.058F, 1.0F);
        left.getTransform().set(0.0F, 0.0F, 0.0F, 1.0F).setPixelSize(LEFT_W, 0.0F);
        this.addChild(left);

        UILabel title = new UILabel("pm_title");
        title.setText("UNIFY");
        title.setTextColor(0.90F, 0.92F, 0.95F, 1.0F);
        title.setAlignment(UILabel.Align.CENTER);
        title.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelOffset(0, 12).setPixelSize(0.0F, 32.0F);
        left.addChild(title);
        this.cleanupActions.add(title::cleanup);

        UILabel sub = new UILabel("pm_subtitle");
        sub.setText("Studio X");
        sub.setTextColor(0.40F, 0.44F, 0.54F, 1.0F);
        sub.setAlignment(UILabel.Align.CENTER);
        sub.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelOffset(0, 46).setPixelSize(0.0F, 18.0F);
        left.addChild(sub);
        this.cleanupActions.add(sub::cleanup);

        UIPanel sep = new UIPanel("pm_left_sep");
        sep.setBackgroundColor(0.18F, 0.19F, 0.22F, 1.0F);
        sep.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelOffset(0, (int) HEADER_H).setPixelSize(0.0F, 1.0F);
        left.addChild(sep);

        UILabel recentHdr = new UILabel("pm_recent_hdr");
        recentHdr.setText("RECENT PROJECTS");
        recentHdr.setTextColor(0.38F, 0.42F, 0.52F, 1.0F);
        recentHdr.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F)
                .setPixelOffset(14, (int) (HEADER_H + 12.0F)).setPixelSize(-14.0F, SECTION_H);
        left.addChild(recentHdr);
        this.cleanupActions.add(recentHdr::cleanup);

        float listY = HEADER_H + 12.0F + SECTION_H + 8.0F;
        this.projectList = new UIScrollPanel("pm_proj_list");
        this.projectList.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F)
                .setPixelOffset(0, (int) listY).setPixelSize(0.0F, -listY);
        left.addChild(this.projectList);

        this.refreshProjectList();
    }

    private void buildRightPanel()
    {
        UIPanel right = new UIPanel("pm_right");
        right.setBackgroundColor(0.050F, 0.058F, 0.075F, 1.0F);
        right.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F)
                .setPixelOffset((int) LEFT_W, 0).setPixelSize(-LEFT_W, 0.0F);
        this.addChild(right);

        UILabel hdr = new UILabel("pm_hdr_lbl");
        hdr.setText("Open or Create a Project");
        hdr.setTextColor(0.88F, 0.90F, 0.93F, 1.0F);
        hdr.setAlignment(UILabel.Align.CENTER);
        hdr.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelOffset(0, 20).setPixelSize(0.0F, HEADER_H - 20.0F);
        right.addChild(hdr);
        this.cleanupActions.add(hdr::cleanup);

        UIPanel sep = new UIPanel("pm_right_sep");
        sep.setBackgroundColor(0.18F, 0.19F, 0.22F, 1.0F);
        sep.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelOffset(0, (int) HEADER_H).setPixelSize(0.0F, 1.0F);
        right.addChild(sep);

        int y = (int) (HEADER_H + 20.0F);

        UILabel newHdr = new UILabel("pm_new_hdr");
        newHdr.setText("NEW PROJECT");
        newHdr.setTextColor(0.38F, 0.50F, 0.76F, 1.0F);
        newHdr.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F)
                .setPixelOffset((int) CONTENT_PAD, y).setPixelSize(-(int) (CONTENT_PAD * 2.0F), SECTION_H);
        right.addChild(newHdr);
        this.cleanupActions.add(newHdr::cleanup);
        y += (int) (SECTION_H + 14.0F);

        y = this.addFormField(right, "pm_name_lbl", "Project Name",
                "pm_field_name", "My Project", true, y);
        y = this.addFormField(right, "pm_path_lbl", "Project Location",
                "pm_field_path", "C:/Projects/MyProject", false, y);

        UIButton createBtn = new UIButton("pm_create", this.context);
        createBtn.setText("Create Project");
        createBtn.setColors(0.22F, 0.36F, 0.62F, 0.28F, 0.44F, 0.70F, 0.16F, 0.26F, 0.48F);
        createBtn.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F)
                .setPixelOffset((int) CONTENT_PAD, y).setPixelSize(-(int) (CONTENT_PAD * 2.0F), BTN_H);
        createBtn.setOnClick(() ->
        {
            String name = this.fieldName.getValue().trim();
            String path = this.fieldPath.getValue().trim();
            if (!name.isEmpty() && !path.isEmpty())
            {
                this.onNewProjectRequested(name, path);
            }
        });
        right.addChild(createBtn);
        this.cleanupActions.add(createBtn::cleanup);
        y += (int) (BTN_H + 16.0F);

        UIPanel sep2 = new UIPanel("pm_right_sep2");
        sep2.setBackgroundColor(0.18F, 0.19F, 0.22F, 1.0F);
        sep2.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F)
                .setPixelOffset((int) CONTENT_PAD, y).setPixelSize(-(int) (CONTENT_PAD * 2.0F), 1.0F);
        right.addChild(sep2);
        y += 16;

        UIButton openBtn = new UIButton("pm_open_existing", this.context);
        openBtn.setText("Open Existing Folder...");
        openBtn.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F)
                .setPixelOffset((int) CONTENT_PAD, y).setPixelSize(-(int) (CONTENT_PAD * 2.0F), BTN_H);
        right.addChild(openBtn);
        this.cleanupActions.add(openBtn::cleanup);
    }

    /** Adds a label + text field pair and returns the next Y offset below the field. */
    private int addFormField(UIPanel parent, String labelId, String labelText,
            String fieldId, String placeholder, boolean isNameField, int y)
    {
        UILabel lbl = new UILabel(labelId);
        lbl.setText(labelText);
        lbl.setTextColor(0.58F, 0.62F, 0.68F, 1.0F);
        lbl.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F)
                .setPixelOffset((int) CONTENT_PAD, y).setPixelSize(-(int) (CONTENT_PAD * 2.0F), LABEL_H);
        parent.addChild(lbl);
        this.cleanupActions.add(lbl::cleanup);
        y += (int) (LABEL_H + 6.0F);

        UITextField field = new UITextField(fieldId, this.context);
        field.setPlaceholder(placeholder);
        field.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F)
                .setPixelOffset((int) CONTENT_PAD, y).setPixelSize(-(int) (CONTENT_PAD * 2.0F), FIELD_H);
        parent.addChild(field);
        this.cleanupActions.add(field::cleanup);

        if (isNameField) this.fieldName = field;
        else this.fieldPath = field;

        return y + (int) (FIELD_H + 14.0F);
    }

    /* ---- Project List ---- */

    /** Rebuilds the recent project rows from the current registry state. */
    public void refreshProjectList()
    {
        for (UIProjectRow row : this.activeRows)
        {
            row.cleanup();
        }
        this.activeRows.clear();
        this.projectList.clearChildren();

        List<ProjectEntry> entries = this.registry.getAll();
        float rowH = UIProjectRow.getRowHeight();
        for (int i = 0; i < entries.size(); i++)
        {
            UIProjectRow row = new UIProjectRow("pm_row_" + i, this.context, entries.get(i), this);
            row.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F)
                    .setPixelOffset(0, (int) (i * rowH)).setPixelSize(0.0F, rowH);
            this.projectList.addChild(row);
            this.activeRows.add(row);
        }

        this.projectList.setMaxContentHeight(entries.size() * rowH);
        this.projectList.setScrollSpeed(30.0F);
    }

    /* ---- Cleanup ---- */

    /** Releases all GPU resources held by this panel and its children. */
    public void cleanup()
    {
        for (UIProjectRow row : this.activeRows)
        {
            row.cleanup();
        }
        this.activeRows.clear();

        for (Runnable action : this.cleanupActions)
        {
            action.run();
        }
        this.cleanupActions.clear();
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        renderer.drawRect(getComputedX(), getComputedY(), getComputedWidth(), getComputedHeight(),
                0.036F, 0.040F, 0.052F, 1.0F);

        /* Thin vertical divider between left and right panels */
        renderer.drawRect(getComputedX() + LEFT_W, getComputedY(), 1.0F, getComputedHeight(),
                0.18F, 0.19F, 0.22F, 1.0F);
    }
}
