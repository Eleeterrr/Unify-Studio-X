package eleeter.unifystudiox.ui.menu;

import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.font.GlyphMetrics;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.settings.menu.MenuBarRegistry;
import eleeter.unifystudiox.settings.menu.MenuCategory;
import eleeter.unifystudiox.ui.ShapeDraw;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.layout.Axis;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.CursorType;
import eleeter.unifystudiox.ui.theme.UITheme;
import eleeter.unifystudiox.ui.theme.UIWidgetData;
import eleeter.unifystudiox.ui.widgets.UILabel;
import java.util.ArrayList;
import java.util.List;


public class UIMenuBar extends UIElement
{
    private static final float BAR_HEIGHT = 22.0F;
    private static final float ITEM_PADDING = 16.0F;
    private static final float ICON_SLOT = 18.0F;
    private static final float ICON_SIZE = 12.0F;
    private static final float ICON_THICKNESS = 1.0F;

    private final MenuBarRegistry registry;
    private MenuCategory activeCategory = null;
    private final UIMenuDropdown dropdown;
    private final eleeter.unifystudiox.ui.framework.layout.UILayout barLayout;
    private final List<UILabel> categoryLabels = new ArrayList<>();
    private int lastCategoryCount = -1;
    private int hoveredCategoryIndex = -1;
    private float lastComputedWidth = -1.0F;

    public UIMenuBar(MenuBarRegistry registry)
    {
        super("top_menu_bar");
        this.registry = registry;
        this.setZIndex(1000);
        this.setBlocksInput(true);

        this.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setAnchor(0.0F, 0.0F).setPixelSize(0, (int) BAR_HEIGHT);

        this.barLayout = new eleeter.unifystudiox.ui.framework.layout.UILayout(this.getId() + "_layout", Axis.HORIZONTAL);
        this.barLayout.getTransform().set(0.0F, 0.0F, 1.0F, 1.0F).setPixelOffset(4, 0);
        this.addChild(this.barLayout);

        this.dropdown = new UIMenuDropdown();
        this.addChild(this.dropdown);
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        float currentWidth = this.getComputedWidth();

        if (this.registry.getCategories().size() != this.lastCategoryCount || Math.abs(currentWidth - this.lastComputedWidth) > 0.1F)
        {
            this.refresh();
            this.lastComputedWidth = currentWidth;
        }

        this.hoveredCategoryIndex = -1;
        float y = this.getComputedY();
        float h = this.getComputedHeight();

        for (int i = 0; i < this.categoryLabels.size(); i++)
        {
            UILabel label = this.categoryLabels.get(i);
            MenuCategory category = this.registry.getCategories().get(i);

            boolean isHovered = context.getMouseX() >= label.getComputedX() && context.getMouseX() < label.getComputedX() + label.getComputedWidth()
                    && context.getMouseY() >= y && context.getMouseY() < y + h;

            if (isHovered)
            {
                this.hoveredCategoryIndex = i;

                if (this.activeCategory != null && this.activeCategory != category)
                {
                    this.activeCategory = category;
                    float relX = label.getComputedX() - this.getComputedX();
                    this.dropdown.setup(this.activeCategory, relX, h);
                    this.dropdown.setVisible(true);
                }

                if (context.isMousePressed())
                {
                    this.activeCategory = (this.activeCategory == category) ? null : category;

                    if (this.activeCategory != null)
                    {
                        float relX = label.getComputedX() - this.getComputedX();
                        this.dropdown.setup(this.activeCategory, relX, h);
                        this.dropdown.setVisible(true);
                    } else
                    {
                        this.dropdown.setVisible(false);
                    }
                }
            }
        }

        if (context.isMousePressed() && this.hoveredCategoryIndex == -1 && !context.isHoveredWithin(this.dropdown))
        {
            this.activeCategory = null;
            this.dropdown.setVisible(false);
        }

        if (!this.dropdown.isVisible())
        {
            this.activeCategory = null;
        }
    }

    private void refresh()
    {
        this.barLayout.clearChildren();
        this.categoryLabels.clear();

        for (int i = 0; i < this.registry.getCategories().size(); i++)
        {
            MenuCategory category = this.registry.getCategories().get(i);
            Font f = FontManager.getFont("inter");
            float measuredW = 0;
            if (f != null)
            {
                for (int ci = 0; ci < category.getTitle().length(); ci++)
                {
                    GlyphMetrics gm = f.getGlyph(category.getTitle().codePointAt(ci));
                    if (gm != null) measuredW += gm.getXAdvance() * f.getNativeSize();
                }
            }
            float textWidth = Math.max(measuredW, category.getTitle().length() * 8.0F) + ITEM_PADDING * 2.0F;
            if (category.getIcon() != null)
            {
                textWidth += ICON_SLOT;
            }
            UILabel label = new UILabel(this.getId() + "_cat_" + i);
            label.setText(category.getTitle());
            label.setAlignment(UILabel.Align.CENTER);
            label.setTextColor(1.0F, 1.0F, 1.0F, 1.0F);
            label.getTransform().setPixelSize(textWidth, BAR_HEIGHT);

            UIWidgetData.setCursor(label, CursorType.HAND);
            UIWidgetData.setTooltip(label, "Open " + category.getTitle() + " menu");

            this.barLayout.addChild(label, 0.0F);
            this.categoryLabels.add(label);
        }

        this.lastCategoryCount = this.registry.getCategories().size();
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        if (Math.abs(w - this.lastComputedWidth) > 0.1F)
        {
            this.refresh();
            this.lastComputedWidth = w;
        }

        UITheme.Theme theme = UITheme.get();
        float[] bg = theme.surface();
        float[] accent = theme.accent();

        renderer.drawRect(x, y, w, h, bg[0], bg[1], bg[2], 0.95F);
        renderer.drawRect(x, y + h - 1.0F, w, 1.0F, 1.0F, 1.0F, 1.0F, 0.05F);

        for (int i = 0; i < this.categoryLabels.size(); i++)
        {
            UILabel label = this.categoryLabels.get(i);
            MenuCategory cat = this.registry.getCategories().get(i);

            boolean isHovered = i == this.hoveredCategoryIndex;
            boolean isActive = this.activeCategory == cat;

            if (isHovered || isActive)
            {
                renderer.drawRoundedRect(label.getComputedX() + 2.0F, y + 2.0F, label.getComputedWidth() - 4.0F, h - 4.0F, accent[0], accent[1], accent[2], 0.2F, 4.0F
                );
            }

            if (cat.getIcon() != null)
            {
                float iconCx = snapHalfPixel(label.getComputedX() + 10.0F);
                float iconCy = snapHalfPixel(y + h * 0.5F);
                float[] col = theme.textMuted();

                ShapeDraw sd = new ShapeDraw(renderer.getMatrixStack());
                cat.getIcon().draw(sd, iconCx, iconCy, ICON_SIZE, ICON_THICKNESS);
                renderer.drawShapeGeometry(sd.getVertices(), sd.getIndices(), col[0], col[1], col[2], 1.0F);
            }
        }
    }

    private static float snapHalfPixel(float value)
    {
        return (float) Math.floor(value) + 0.5F;
    }
}
