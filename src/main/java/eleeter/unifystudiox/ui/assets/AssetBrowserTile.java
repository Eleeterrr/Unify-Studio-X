package eleeter.unifystudiox.ui.assets;

import java.util.function.Consumer;

import eleeter.unifystudiox.assets.browser.AssetBrowserItem;
import eleeter.unifystudiox.assets.browser.AssetBrowserItemType;
import eleeter.unifystudiox.ui.framework.UIElement;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.theme.UIDropShadow;
import eleeter.unifystudiox.ui.widgets.UILabel;

public class AssetBrowserTile extends UIElement
{
    private static final float PREVIEW_HEIGHT = 160.0F;
    private static final float TITLE_HEIGHT = 22.0F;
    private static final float SUBTITLE_HEIGHT = 18.0F;

    private final AssetBrowserItem item;
    private final ModelPreviewRenderer previewRenderer;
    private Consumer<AssetBrowserItem> onClickCallback;
    private final UILabel titleLabel;
    private final UILabel subtitleLabel;
    private float hoverProgress = 0.0F;

    private boolean hasClipRect = false;
    private float clipX = 0.0F;
    private float clipY = 0.0F;
    private float clipW = 0.0F;
    private float clipH = 0.0F;

    public AssetBrowserTile(String id, AssetBrowserItem item, ModelPreviewRenderer previewRenderer)
    {
        super(id);
        this.item = item;
        this.previewRenderer = previewRenderer;
        this.setBlocksInput(true);

        this.titleLabel = new UILabel(id + "_title");
        this.titleLabel.setText(this.item.getTitle());
        this.titleLabel.setAlignment(UILabel.Align.LEFT);
        this.titleLabel.setTextColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.titleLabel.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelOffset(8, (int) (PREVIEW_HEIGHT + 4.0F)).setPixelSize(-16, (int) TITLE_HEIGHT);
        this.addChild(this.titleLabel);

        this.subtitleLabel = new UILabel(id + "_subtitle");
        this.subtitleLabel.setText(Subtitle(this.item));
        this.subtitleLabel.setAlignment(UILabel.Align.LEFT);
        this.subtitleLabel.setTextColor(0.70F, 0.72F, 0.75F, 1.0F);
        this.subtitleLabel.getTransform().set(0.0F, 0.0F, 1.0F, 0.0F).setPixelOffset(8, (int) (PREVIEW_HEIGHT + TITLE_HEIGHT + 4.0F)).setPixelSize(-16, (int) SUBTITLE_HEIGHT);
        this.addChild(this.subtitleLabel);
    }

    public void setOnClick(Consumer<AssetBrowserItem> callback)
    {
        this.onClickCallback = callback;
    }

    public AssetBrowserItem getItem()
    {
        return this.item;
    }

    public void setClipRect(float x, float y, float w, float h)
    {
        this.hasClipRect = true;
        this.clipX = x;
        this.clipY = y;
        this.clipW = w;
        this.clipH = h;
    }

    @Override
    public boolean containsPoint(float x, float y)
    {
        if (!isVisible() || !isEnabled())
        {
            return false;
        }

        if (x < this.cx || x >= this.cx + this.cw || y < this.cy || y >= this.cy + this.ch)
        {
            return false;
        }


        if (this.hasClipRect)
        {
            return x >= this.clipX && x < this.clipX + this.clipW && y >= this.clipY && y < this.clipY + this.clipH;
        }

        return true;
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        boolean isHovered = context.isHovered(this);
        this.hoverProgress = this.approach(this.hoverProgress, isHovered ? 1.0F : 0.0F, (float) (deltaTime * 12.0D));

        if (this.onClickCallback != null && context.isClicked(this))
        {
            this.onClickCallback.accept(this.item);
        }
    }

    private float approach(float current, float target, float factor)
    {
        float next = current + (target - current) * factor;
        if (Math.abs(target - next) < 0.01F)
        {
            return target;
        }
        return next;
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        UIDropShadow.drawRounded(renderer, x, y, w, h, 0.0F, 2.0F, 0.15F * this.hoverProgress, 8.0F);

        float bgVal = 0.15F + 0.03F * this.hoverProgress;
        renderer.drawRoundedRect(x, y, w, h, bgVal, bgVal, bgVal + 0.01F, 0.9F, 8.0F);

        /* Draw a sleek  */
        float borderR = 0.20F + (0.25F - 0.20F) * this.hoverProgress;
        float borderG = 0.22F + (0.45F - 0.22F) * this.hoverProgress;
        float borderB = 0.25F + (0.95F - 0.25F) * this.hoverProgress;
        float borderA = 0.40F + (0.40F) * this.hoverProgress;
        renderer.drawRoundedRect(x - 0.5F, y - 0.5F, w + 1.0F, h + 1.0F, borderR, borderG, borderB, borderA, 8.0F);

        float previewX = x + 6.0F;
        float previewY = y + 6.0F;
        float previewW = w - 12.0F;
        float previewH = Math.max(1.0F, Math.min(PREVIEW_HEIGHT - 12.0F, h - TITLE_HEIGHT - SUBTITLE_HEIGHT - 16.0F));

        if (this.item.hasPreview())
        {
            int textureHandle = this.previewRenderer.getPreviewTextureHandle(this.item.getPreviewAssetId());
            if (textureHandle != 0)
            {
                renderer.drawRoundedRect(previewX, previewY, previewW, previewH, 0.10F, 0.10F, 0.11F, 1.0F, 6.0F);
                renderer.drawFramebufferTexture(previewX, previewY, previewW, previewH, textureHandle, 1.0F, 1.0F, 1.0F, 1.0F);
                renderer.drawRoundedRect(previewX, previewY, previewW, previewH, 1.0F, 1.0F, 1.0F, 0.05F, 6.0F);
            }
            else
            {
                this.drawPlaceholderPreview(renderer, previewX, previewY, previewW, previewH);
            }
        }
        else
        {
            this.drawPlaceholderPreview(renderer, previewX, previewY, previewW, previewH);
        }

        if (this.item.isAnimated())
        {
            /* Sleek accent bar at the bottom edge of the preview thumbnail slot */
            renderer.drawRoundedRect(previewX, previewY + previewH - 4.0F, previewW, 4.0F, 0.25F, 0.45F, 0.95F, 1.0F, 2.0F);
        }
    }

    private void drawPlaceholderPreview(UIRenderer renderer, float x, float y, float w, float h)
    {
        float tint = this.item.getType() == AssetBrowserItemType.TOOL ? 0.18F : 0.14F;
        renderer.drawRoundedRect(x, y, w, h, tint, tint, tint + 0.03F, 1.0F, 6.0F);
        renderer.drawRoundedRect(x, y, w, h, 1.0F, 1.0F, 1.0F, 0.04F, 6.0F);
    }

    private static String Subtitle(AssetBrowserItem item)
    {
        String subtitle = item.getSubtitle();
        if (item.isDragAndDropPlaceholder())
        {
            if (subtitle == null || subtitle.isEmpty())
            {
                /*TODO: Add I18n Key */
                return "Drag & drop";
            }
            return subtitle + " | Drag & drop";
        }
        return subtitle != null ? subtitle : "";
    }
}
