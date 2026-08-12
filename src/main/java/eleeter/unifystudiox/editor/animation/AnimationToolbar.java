package eleeter.unifystudiox.editor.animation;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.layout.AttributeType;
import eleeter.unifystudiox.graphics.layout.BufferLayout;
import eleeter.unifystudiox.graphics.text.render.MeshData;
import eleeter.unifystudiox.graphics.text.render.TextMeshGenerator;
import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.shaping.TextLayout;
import eleeter.unifystudiox.graphics.text.render.shaping.TextShaper;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;
import eleeter.unifystudiox.ui.widgets.UIButton;
import eleeter.unifystudiox.ui.widgets.UILabel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AnimationToolbar extends UIPanel
{
    private static final float BUTTON_W = 56.0f;
    private static final float BUTTON_H = 28.0f;
    private static final float BUTTON_PAD = 6.0f;
    private static final float CYCLE_W = 140.0f;

    private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
    private AnimationEditorCallbacks callbacks;

    private final UIButton playButton;
    private final UIButton pauseButton;
    private final UIButton stopButton;
    private final UIButton addKeyframeButton;
    private final UIButton addPoseKeyframeButton;
    private final UILabel timeLabel;

    private final CycleSelector objectSelector;
    private final CycleSelector clipSelector;

    private float currentTime = 0.0f;
    private boolean isPlaying = false;

    public AnimationToolbar(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        super(id);
        this.context = context;
        this.setBlocksInput(true);

        float[] bg = AnimationEditorTheme.BUTTON_BG;
        float[] hov = AnimationEditorTheme.BUTTON_HOVER_BG;
        float[] act = AnimationEditorTheme.BUTTON_ACTIVE_BG;

        this.playButton = this.makeButton("toolbar_play", "", bg, hov, act);
        this.pauseButton = this.makeButton("toolbar_pause", "", bg, hov, act);
        this.stopButton = this.makeButton("toolbar_stop", "", bg, hov, act);
        this.addKeyframeButton = this.makeButton("toolbar_add_kf", "+ Key", bg, hov, act);
        this.addPoseKeyframeButton = this.makeButton("toolbar_add_pose", "+ Pose", bg, hov, act);

        this.timeLabel = new UILabel("toolbar_time");
        this.timeLabel.setText("0.00s");
        float[] accentCol = AnimationEditorTheme.ACCENT_COLOR;
        this.timeLabel.setTextColor(accentCol[0], accentCol[1], accentCol[2], accentCol[3]);

        this.objectSelector = new CycleSelector("toolbar_obj_cycle", context);
        this.clipSelector = new CycleSelector("toolbar_clip_cycle", context);

        this.addChild(this.playButton);
        this.addChild(this.pauseButton);
        this.addChild(this.stopButton);
        this.addChild(this.addKeyframeButton);
        this.addChild(this.addPoseKeyframeButton);
        this.addChild(this.timeLabel);
        this.addChild(this.objectSelector);
        this.addChild(this.clipSelector);

        this.playButton.setOnClick(() ->
        {
            if (this.callbacks != null)
            {
                this.callbacks.onPlayRequested();
            }
        });
        this.pauseButton.setOnClick(() ->
        {
            if (this.callbacks != null)
            {
                this.callbacks.onPauseRequested();
            }
        });
        this.stopButton.setOnClick(() ->
        {
            if (this.callbacks != null)
            {
                this.callbacks.onStopRequested();
            }
        });
        this.addKeyframeButton.setOnClick(() ->
        {
            if (this.callbacks != null)
            {
                this.callbacks.onAddKeyframeRequested(this.currentTime);
            }
        });
        this.addPoseKeyframeButton.setOnClick(() ->
        {
            if (this.callbacks != null)
                this.callbacks.onAddPoseKeyframeRequested(this.currentTime);
        });

        this.objectSelector.setOnSelectionChanged(value ->
        {
            if (this.callbacks != null)
            {
                this.callbacks.onObjectSelected(value);
            }
        });
        this.clipSelector.setOnSelectionChanged(value ->
        {
            if (this.callbacks != null)
            {
                this.callbacks.onClipSelected(value);
            }
        });
    }

    private UIButton makeButton(String id, String text, float[] bg, float[] hov, float[] act)
    {
        UIButton btn = new UIButton(id, this.context);
        btn.setText(text);
        btn.setColors(bg[0], bg[1], bg[2], hov[0], hov[1], hov[2], act[0], act[1], act[2]);
        return btn;
    }

    public void setCallbacks(AnimationEditorCallbacks callbacks)
    {
        this.callbacks = callbacks;
    }

    public void setCurrentTime(float time)
    {
        this.currentTime = time;
        this.timeLabel.setText(String.format(Locale.US, "%.2fs", time));
    }

    public void setPlayingState(boolean playing)
    {
        this.isPlaying = playing;
        float[] bg = AnimationEditorTheme.BUTTON_BG;
        float[] hov = AnimationEditorTheme.BUTTON_HOVER_BG;
        float[] act = AnimationEditorTheme.BUTTON_ACTIVE_BG;
        float[] accent = AnimationEditorTheme.ACCENT_COLOR;

        if (playing)
        {
            this.playButton.setColors(accent[0], accent[1], accent[2], accent[0], accent[1], accent[2], accent[0],
                    accent[1], accent[2]);
        } else
        {
            this.playButton.setColors(bg[0], bg[1], bg[2], hov[0], hov[1], hov[2], act[0], act[1], act[2]);
        }
        this.playButton.markDirty();
    }

    public void setObjectList(List<String> ids)
    {
        this.objectSelector.setOptions(Collections.unmodifiableList(new ArrayList<>(ids)));
    }

    public void setClipList(List<String> names)
    {
        this.clipSelector.setOptions(Collections.unmodifiableList(new ArrayList<>(names)));
    }

    public void setShotName(String name)
    {
        this.clipSelector.setSelection(name);
    }

    public void setSelectedObject(String id)
    {
        this.objectSelector.setSelection(id);
    }

    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext ctx, double deltaTime)
    {
        this.layoutChildren();
    }

    private void layoutChildren()
    {
        float panelX = this.getComputedX();
        float panelY = this.getComputedY();
        float panelH = this.getComputedHeight();

        float buttonY = panelY + (panelH - BUTTON_H) * 0.5f;
        float cursor = panelX + BUTTON_PAD;

        this.positionChild(this.playButton, cursor, buttonY);
        cursor += BUTTON_W + BUTTON_PAD;
        this.positionChild(this.pauseButton, cursor, buttonY);
        cursor += BUTTON_W + BUTTON_PAD;
        this.positionChild(this.stopButton, cursor, buttonY);
        cursor += BUTTON_W + BUTTON_PAD * 2;
        this.positionChild(this.addKeyframeButton, cursor, buttonY);
        cursor += BUTTON_W + BUTTON_PAD;
        this.positionChild(this.addPoseKeyframeButton, cursor, buttonY);
        cursor += BUTTON_W + BUTTON_PAD * 2;

        this.positionCycle(this.objectSelector, cursor, buttonY);
        cursor += CYCLE_W + BUTTON_PAD;
        this.positionCycle(this.clipSelector, cursor, buttonY);
        cursor += CYCLE_W + BUTTON_PAD;

        float labelW = 70.0f;
        float labelX = this.getComputedX() + this.getComputedWidth() - labelW - BUTTON_PAD;
        this.timeLabel.getTransform().set(0f, 0f, 0f, 0f)
                .setPixelOffset((int) (labelX - this.getComputedX()), (int) (buttonY - this.getComputedY()))
                .setPixelSize(labelW, BUTTON_H);
        this.timeLabel.markDirty();
    }

    private void positionChild(UIButton button, float x, float y)
    {
        button.getTransform().set(0f, 0f, 0f, 0f).setPixelOffset((int) (x - this.getComputedX()), (int) (y - this.getComputedY()))
                .setPixelSize(BUTTON_W, BUTTON_H);
        button.markDirty();
    }

    private void positionCycle(CycleSelector cycle, float x, float y)
    {
        cycle.getTransform().set(0f, 0f, 0f, 0f).setPixelOffset((int) (x - this.getComputedX()), (int) (y - this.getComputedY()))
                .setPixelSize(CYCLE_W, BUTTON_H);
        cycle.markDirty();
    }

    @Override
    public void render(UIRenderer renderer)
    {
        super.render(renderer);

        float x = this.getComputedX();
        float y = this.getComputedY();
        float h = this.getComputedHeight();

        float buttonY = y + (h - BUTTON_H) * 0.5f;
        float cursor = x + BUTTON_PAD;

        float[] icon = {0.88f, 0.88f, 0.88f, 1.0f};

        float playCX = cursor + BUTTON_W * 0.5f;
        float playCY = buttonY + BUTTON_H * 0.5f;

        if (this.isPlaying)
        {
            renderer.drawRect(playCX - 5f, playCY - 6f, 4f, 12f, icon[0], icon[1], icon[2], 1.0f);
            renderer.drawRect(playCX + 1f, playCY - 6f, 4f, 12f, icon[0], icon[1], icon[2], 1.0f);
        } else
        {
            renderer.drawRect(playCX - 5f, playCY - 7f, 4f, 14f, icon[0], icon[1], icon[2], 1.0f);
            renderer.drawRect(playCX - 1f, playCY - 5f, 4f, 10f, icon[0], icon[1], icon[2], 1.0f);
            renderer.drawRect(playCX + 3f, playCY - 3f, 4f, 6f, icon[0], icon[1], icon[2], 1.0f);
            renderer.drawRect(playCX + 7f, playCY - 1f, 2f, 2f, icon[0], icon[1], icon[2], 1.0f);
        }

        cursor += BUTTON_W + BUTTON_PAD;

        float pauseCX = cursor + BUTTON_W * 0.5f;
        float pauseCY = buttonY + BUTTON_H * 0.5f;
        renderer.drawRect(pauseCX - 5f, pauseCY - 6f, 4f, 12f, icon[0], icon[1], icon[2], 1.0f);
        renderer.drawRect(pauseCX + 1f, pauseCY - 6f, 4f, 12f, icon[0], icon[1], icon[2], 1.0f);
        cursor += BUTTON_W + BUTTON_PAD;

        float stopCX = cursor + BUTTON_W * 0.5f;
        float stopCY = buttonY + BUTTON_H * 0.5f;
        renderer.drawRect(stopCX - 6f, stopCY - 6f, 12f, 12f, icon[0], icon[1], icon[2], 1.0f);
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        float[] bg = AnimationEditorTheme.TOOLBAR_BG;
        renderer.drawRect(x, y, w, h, bg[0], bg[1], bg[2], 1.0f);

        float[] border = AnimationEditorTheme.BORDER_COLOR;
        renderer.drawRect(x, y, w, 1.0f, border[0], border[1], border[2], 1.0f);

        float sep1X = x + BUTTON_PAD + 3f * (BUTTON_W + BUTTON_PAD) + BUTTON_PAD;
        float sepH = h * 0.55f;
        float sepY = y + (h - sepH) * 0.5f;
        renderer.drawRect(sep1X, sepY, 1f, sepH, border[0], border[1], border[2], 0.6f);

        float sep2X = sep1X + BUTTON_PAD + 2f * (BUTTON_W + BUTTON_PAD) + BUTTON_PAD;
        renderer.drawRect(sep2X, sepY, 1f, sepH, border[0], border[1], border[2], 0.6f);
    }

    public void cleanup()
    {
        this.playButton.cleanup();
        this.pauseButton.cleanup();
        this.stopButton.cleanup();
        this.addKeyframeButton.cleanup();
        this.addPoseKeyframeButton.cleanup();
        this.timeLabel.cleanup();
        this.objectSelector.cleanup();
        this.clipSelector.cleanup();
    }

    private static final class CycleSelector extends UIPanel
    {
        @FunctionalInterface
        interface SelectionChangedListener
        {
            void onChanged(String value);
        }

        private static final float ARROW_W = 20.0f;

        private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;
        private List<String> options = Collections.emptyList();
        private int selectedIndex = 0;
        private SelectionChangedListener listener;

        private String cachedLabelText = null;
        private CachedTextMesh labelMesh = null;
        private TextureGL atlas = null;

        public CycleSelector(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
        {
            super(id);
            this.context = context;
            this.setBlocksInput(true);
        }

        public void setOptions(List<String> options)
        {
            this.options = options;
            this.selectedIndex = 0;
            this.invalidateLabelCache();
        }

        public void setSelection(String value)
        {
            if (this.options.isEmpty() || value == null)
            {
                return;
            }
            for (int i = 0; i < this.options.size(); i++)
            {
                if (this.options.get(i).equals(value))
                {
                    this.selectedIndex = i;
                    this.invalidateLabelCache();
                    return;
                }
            }
        }

        public void setOnSelectionChanged(SelectionChangedListener listener)
        {
            this.listener = listener;
        }

        String getSelected()
        {
            if (this.options.isEmpty())
            {
                return "";
            }
            return this.options.get(this.selectedIndex);
        }

        private void invalidateLabelCache()
        {
            if (this.labelMesh != null)
            {
                this.labelMesh.destroy();
                this.labelMesh = null;
            }
            this.cachedLabelText = null;
        }

        @Override
        protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext ctx, double deltaTime)
        {
            if (this.options.isEmpty())
            {
                return;
            }

            if (ctx.isClicked(this))
            {
                float midX = this.getComputedX() + ARROW_W;
                float rightArrowX = this.getComputedX() + this.getComputedWidth() - ARROW_W;

                if (ctx.getMouseX() < midX)
                {
                    this.selectedIndex = (this.selectedIndex - 1 + this.options.size()) % this.options.size();
                    this.invalidateLabelCache();
                    if (this.listener != null)
                    {
                        this.listener.onChanged(this.getSelected());
                    }
                } else if (ctx.getMouseX() >= rightArrowX)
                {
                    this.selectedIndex = (this.selectedIndex + 1) % this.options.size();
                    this.invalidateLabelCache();

                    if (this.listener != null)
                    {
                        this.listener.onChanged(this.getSelected());
                    }
                }
            }
        }

        @Override
        protected void renderSelf(UIRenderer renderer)
        {
            float x = this.getComputedX();
            float y = this.getComputedY();
            float w = this.getComputedWidth();
            float h = this.getComputedHeight();

            float[] bg = this.context.isHovered(this) ? AnimationEditorTheme.BUTTON_HOVER_BG
                    : AnimationEditorTheme.BUTTON_BG;
            renderer.drawRect(x, y, w, h, bg[0], bg[1], bg[2], 1.0f);

            float[] border = AnimationEditorTheme.BORDER_COLOR;
            renderer.drawRect(x, y, w, h, border[0], border[1], border[2], 0.5f);

            String label = this.options.isEmpty() ? "---" : this.getSelected();
            String displayText = "< " + label + " >";
            this.ensureLabelMesh(displayText);

            if (this.labelMesh != null && this.atlas != null)
            {
                Font font = FontManager.getFont("inter");
                float scale = 1.0f;
                float scaledW = this.labelMesh.width * scale;
                if (scaledW > w - 4f)
                {
                    scale = (w - 4f) / this.labelMesh.width;
                }

                float tx = x + (w - this.labelMesh.width * scale) * 0.5f;
                float ty = y + (h - this.labelMesh.height * scale) * 0.5f;

                if (font != null)
                {
                    ty += font.getBaseline() * scale;
                }

                float[] col = AnimationEditorTheme.TEXT_PRIMARY;
                renderer.pushClip(x, y, w, h);
                renderer.drawText(this.labelMesh.data, this.atlas, tx, ty, scale, col[0],
                        col[1], col[2], col[3]);
                renderer.popClip();
            }
        }

        private void ensureLabelMesh(String text)
        {
            if (text.equals(this.cachedLabelText) && this.labelMesh != null)
            {
                return;
            }

            this.invalidateLabelCache();

            Font font = FontManager.getFont("inter");
            if (font == null)
            {
                return;
            }

            TextShaper shaper = new TextShaper();
            TextLayout layout = shaper.shape(text, font, font.getNativeSize());
            MeshData data = TextMeshGenerator.generate(layout, font);
            if (data.indices.length == 0)
            {
                return;
            }

            BufferLayout spec = BufferLayout.builder().add(0, 3, AttributeType.FLOAT).add(1, 2, AttributeType.FLOAT)
                    .build();

            this.labelMesh = new CachedTextMesh(data, layout.getWidth(),
                    layout.getHeight());
            this.cachedLabelText = text;
            this.atlas = FontManager.getAtlas("inter");
        }

        void cleanup()
        {
            this.invalidateLabelCache();
        }
    }

    private static final class CachedTextMesh
    {
        final MeshData data;
        final float width;
        final float height;

        private CachedTextMesh(MeshData data, float width, float height)
        {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        public void destroy()
        {
        }
    }
}
