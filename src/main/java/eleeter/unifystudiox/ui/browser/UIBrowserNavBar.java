package eleeter.unifystudiox.ui.browser;

import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UIButton;
import eleeter.unifystudiox.ui.widgets.UILabel;
import java.util.function.Consumer;
import org.lwjgl.glfw.GLFW;


public class UIBrowserNavBar extends UIElement
{
    private static final float HEIGHT = 30.0F;
    private static final float BTN_SIZE = 22.0F;
    private static final float BTN_GAP = 4.0F;
    private static final float SEARCH_W = 160.0F;
    private static final float BG_R = 0.10F;
    private static final float BG_G = 0.10F;
    private static final float BG_B = 0.11F;

    private final UILabel breadcrumb;
    private final UILabel searchLabel;
    private final UIButton backButton;
    private final UIButton forwardButton;
    private final UIButton upButton;
    private final UIButton toggleViewButton;

    private String searchQuery = "";
    private boolean isSearchFocused = false;
    private boolean isGridView = true;

    private Runnable onBack;
    private Runnable onForward;
    private Runnable onUp;
    private Runnable onToggleView;
    private Consumer<String> onSearchChanged;

    public UIBrowserNavBar(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelSize(0, (int) HEIGHT);

        this.backButton = new UIButton(id + "_back", context);
        this.backButton.setText("<");
        this.backButton.setOnClick(() ->
        {
            if (this.onBack != null)
            {
                this.onBack.run();
            }
        });
        this.backButton.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F).setPixelOffset(4, 4).setPixelSize((int) BTN_SIZE, (int) BTN_SIZE);
        this.addChild(this.backButton);

        this.forwardButton = new UIButton(id + "_fwd", context);
        this.forwardButton.setText(">");
        this.forwardButton.setOnClick(() ->
        {
            if (this.onForward != null)
            {
                this.onForward.run();
            }
        });
        this.forwardButton.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F).setPixelOffset((int) (4 + BTN_SIZE + BTN_GAP), 4).setPixelSize((int) BTN_SIZE, (int) BTN_SIZE);
        this.addChild(this.forwardButton);

        this.upButton = new UIButton(id + "_up", context);
        this.upButton.setText("^");
        this.upButton.setOnClick(() ->
        {
            if (this.onUp != null)
            {
                this.onUp.run();
            }
        });
        this.upButton.getTransform().set(0.0F, 0.0F, 0.0F, 0.0F).setPixelOffset((int) (4 + (BTN_SIZE + BTN_GAP) * 2.0F), 4).setPixelSize((int) BTN_SIZE, (int) BTN_SIZE);
        this.addChild(this.upButton);

        this.breadcrumb = new UILabel(id + "_crumb");
        this.breadcrumb.setText("Assets");
        this.breadcrumb.setAlignment(UILabel.Align.LEFT);
        this.breadcrumb.setTextColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.breadcrumb.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelOffset((int) (4 + (BTN_SIZE + BTN_GAP) * 3.0F), 4).setPixelSize((int) -(SEARCH_W + BTN_SIZE + BTN_GAP * 3.0F + 4.0F), (int) BTN_SIZE);
        this.addChild(this.breadcrumb);

        this.searchLabel = new UILabel(id + "_search_hint");
        this.searchLabel.setText("Search...");
        this.searchLabel.setAlignment(UILabel.Align.LEFT);
        this.searchLabel.setTextColor(0.45F, 0.45F, 0.48F, 1.0F);
        this.searchLabel.getTransform().set(1.0F, 0.0F, 0.0F, 0.0F).setPixelOffset((int) -(SEARCH_W + BTN_SIZE + BTN_GAP + 4.0F), 4).setPixelSize((int) SEARCH_W - 8, (int) BTN_SIZE);
        this.addChild(this.searchLabel);

        this.toggleViewButton = new UIButton(id + "_toggle", context);
        this.toggleViewButton.setText("=");
        this.toggleViewButton.setOnClick(this::handleToggleView);
        this.toggleViewButton.getTransform().set(1.0F, 0.0F, 0.0F, 0.0F).setPixelOffset((int) -(BTN_SIZE + 4.0F), 4).setPixelSize((int) BTN_SIZE, (int) BTN_SIZE);
        this.addChild(this.toggleViewButton);
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float searchBoxX = x + w - SEARCH_W - BTN_SIZE - BTN_GAP - 4.0F;

        boolean clickedSearch = context.isMousePressed() && context.getMouseX() >= searchBoxX && context.getMouseX() < searchBoxX + SEARCH_W && context.getMouseY() >= y + 4.0F && context.getMouseY() < y + 4.0F + BTN_SIZE;

        if (clickedSearch)
        {
            this.isSearchFocused = true;
        } else if (context.isMousePressed())
        {
            this.isSearchFocused = false;
        }

        if (this.isSearchFocused)
        {
            String typed = context.consumeTextInput();
            if (!typed.isEmpty())
            {
                this.searchQuery = this.searchQuery + typed;
                updateSearchDisplay();
            }

            if (context.isKeyPressed(GLFW.GLFW_KEY_BACKSPACE) && !this.searchQuery.isEmpty())
            {
                this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
                updateSearchDisplay();
            }
        }
    }

    private void updateSearchDisplay()
    {
        if (this.searchQuery.isEmpty())
        {
            this.searchLabel.setText("Search...");
            this.searchLabel.setTextColor(0.45F, 0.45F, 0.48F, 1.0F);
        } else
        {
            this.searchLabel.setText(this.searchQuery);
            this.searchLabel.setTextColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (this.onSearchChanged != null)
        {
            this.onSearchChanged.accept(this.searchQuery);
        }
    }

    private void handleToggleView()
    {
        this.isGridView = !this.isGridView;
        this.toggleViewButton.setText(this.isGridView ? "=" : "#");
        if (this.onToggleView != null)
        {
            this.onToggleView.run();
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        renderer.drawRect(x, y, w, h, BG_R, BG_G, BG_B, 1.0F);
        renderer.drawRect(x, y + h - 1.0F, w, 1.0F, 1.0F, 1.0F, 1.0F, 0.04F);

        /* Search field background */
        float searchBoxX = x + w - SEARCH_W - BTN_SIZE - BTN_GAP - 4.0F;
        float fieldAlpha = this.isSearchFocused ? 0.18F : 0.10F;
        renderer.drawRoundedRect(searchBoxX, y + 4.0F, SEARCH_W, BTN_SIZE, 0.15F, 0.15F, 0.16F, fieldAlpha, 4.0F);
        renderer.drawRoundedRect(searchBoxX, y + 4.0F, SEARCH_W, BTN_SIZE, 1.0F, 1.0F, 1.0F, this.isSearchFocused ? 0.12F : 0.05F, 4.0F);
    }

    public void setBreadcrumb(String text)
    {
        this.breadcrumb.setText(text);
    }

    public boolean isGridView()
    {
        return this.isGridView;
    }

    public String getSearchQuery()
    {
        return this.searchQuery;
    }

    public void setOnBack(Runnable callback)
    {
        this.onBack = callback;
    }

    public void setOnForward(Runnable callback)
    {
        this.onForward = callback;
    }

    public void setOnUp(Runnable callback)
    {
        this.onUp = callback;
    }

    public void setOnToggleView(Runnable callback)
    {
        this.onToggleView = callback;
    }

    public void setOnSearchChanged(Consumer<String> callback)
    {
        this.onSearchChanged = callback;
    }

    public static float getHeight()
    {
        return HEIGHT;
    }
}
