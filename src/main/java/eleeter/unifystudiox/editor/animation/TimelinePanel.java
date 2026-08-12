package eleeter.unifystudiox.editor.animation;

import eleeter.unifystudiox.animation.data.AnimationTrack;
import eleeter.unifystudiox.animation.data.BoneInfo;
import eleeter.unifystudiox.animation.data.Keyframe;
import eleeter.unifystudiox.animation.data.Skeleton;
import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.graphics.Vao;
import eleeter.unifystudiox.graphics.VertexBuffer;
import eleeter.unifystudiox.graphics.buffer.GpuBufferUsage;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.lwjgl.glfw.GLFW;


public class TimelinePanel extends UIPanel
{
    private static final String FONT_KEY = "inter";
    private static final float RULER_HEIGHT = 24.0f;
    private static final float MAJOR_TICK_SECONDS = 0.5f;
    private static final float MINOR_TICK_SECONDS = 0.1f;
    private static final float MAJOR_TICK_H = 10.0f;
    private static final float MINOR_TICK_H = 5.0f;
    private static final float DRAG_TIME_THRESHOLD = 0.05f;
    private static final float PLAYHEAD_WIDTH = 1.0f;
    private static final float DEFAULT_TIME_SCALE = 100.0f;


    private AnimationEditorCallbacks callbacks;

    private List<AnimationTrack<?>> allTracks = new ArrayList<>();
    private List<AnimationTrack<?>> visibleTracks = new ArrayList<>();

    private String selectedBoneId = null;
    private String selectedTrackTargetId = null;
    private String selectedProperty = null;
    private float selectedKeyTime = -1.0f;

    private float currentTime = 0.0f;
    private float clipDuration = 10.0f;
    private float scrollX = 0.0f;
    private float scrollY = 0.0f;
    private float timeScale = DEFAULT_TIME_SCALE;
    private Skeleton skeleton = null;

    /* Playhead drag */
    private boolean isDraggingPlayhead = false;

    /* Keyframe drag */
    private boolean isDraggingKeyframe = false;
    private String dragBoneId = null;
    private String dragProperty = null;
    private float dragOriginalTime = 0.0f;
    private float dragCurrentTime = 0.0f;

    /* Text cache */
    private final Map<String, CachedTextMesh> textCache = new HashMap<>();
    private TextureGL fontAtlas = null;

    /* Group collapse state */
    private boolean rotationGroupCollapsed = false;
    private boolean positionGroupCollapsed = false;
    private boolean scaleGroupCollapsed = false;
    private boolean poseGroupCollapsed = false;


    public TimelinePanel(String id)
    {
        super(id);
        this.setBlocksInput(true);
    }

    public void setCallbacks(AnimationEditorCallbacks callbacks)
    {
        this.callbacks = callbacks;
    }

    public void setTracks(List<AnimationTrack<?>> tracks)
    {
        this.allTracks = new ArrayList<>(tracks);
        this.buildVisibleTracks();
    }

    public void setCurrentTime(float time)
    {
        this.currentTime = time;
    }

    public float getCurrentTime()
    {
        return this.currentTime;
    }

    public void setClipDuration(float dur)
    {
        this.clipDuration = dur;
    }

    public void setSelectedBone(String boneId)
    {
        this.selectedBoneId = boneId;
        this.buildVisibleTracks();
    }

    public void setSkeleton(Skeleton skeleton)
    {
        this.skeleton = skeleton;
    }


    /**
     * Returns the X screen coordinate for the label column boundary.
     */
    private float labelColumnEndX()
    {
        return this.getComputedX() + AnimationEditorTheme.LABEL_COLUMN_WIDTH;
    }

    /**
     * Returns the Y screen coordinate where track rows begin.
     */
    private float tracksStartY()
    {
        return this.getComputedY() + RULER_HEIGHT - this.scrollY;
    }



    private float pixelToTime(float pixelX)
    {
        return (pixelX - this.labelColumnEndX() + this.scrollX) / this.timeScale;
    }


    private float timeToPixel(float time)
    {
        return this.labelColumnEndX() + (time * this.timeScale) - this.scrollX;
    }


    private void buildVisibleTracks()
    {
        if (this.selectedBoneId == null || this.selectedBoneId.isEmpty())
        {
            this.visibleTracks = Collections.unmodifiableList(new ArrayList<>(this.allTracks));
        } else
        {
            List<AnimationTrack<?>> filtered = new ArrayList<>();
            for (AnimationTrack<?> track : this.allTracks)
            {
                if (track.getTargetId().equals(this.selectedBoneId)) filtered.add(track);
            }
            this.visibleTracks = Collections.unmodifiableList(filtered);
        }
    }


    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        boolean mouseInPanel = this.containsPoint(context.getMouseX(), context.getMouseY());
        boolean anyActive = this.isDraggingPlayhead || this.isDraggingKeyframe;
        if (!mouseInPanel && !anyActive) return;

        this.handleZoom(context);
        this.handlePan(context);
        this.clampScrollY();
        this.handlePlayheadDrag(context);
        this.handleGroupHeaderClick(context);
        this.handleKeyframeDrag(context);
        this.handleKeyframeClick(context);
    }

    private void handleGroupHeaderClick(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        if (!context.isMousePressed()) return;
        if (!this.containsPoint(context.getMouseX(), context.getMouseY())) return;

        float headerH = AnimationEditorTheme.TRACK_HEADER_HEIGHT;
        float rowH = AnimationEditorTheme.TRACK_ROW_HEIGHT;
        float cursorY = this.tracksStartY();
        String lastGroup = null;

        for (AnimationTrack<?> track : this.visibleTracks)
        {
            String group = KeyframeShapeRenderer.resolveGroup(track.getPropertyName());
            if (!group.equals(lastGroup))
            {
                lastGroup = group;
                float hdrTop = cursorY;
                cursorY += headerH;

                float my = context.getMouseY();
                if (my >= hdrTop && my < hdrTop + headerH
                        && context.getMouseX() < this.labelColumnEndX())
                {
                    this.toggleGroupCollapsed(group);
                    return;
                }
            }
            if (!this.isGroupCollapsed(group)) cursorY += rowH;
        }
    }

    private void toggleGroupCollapsed(String group)
    {
        switch (group)
        {
            case "rotation":
                this.rotationGroupCollapsed = !this.rotationGroupCollapsed;
                break;
            case "position":
                this.positionGroupCollapsed = !this.positionGroupCollapsed;
                break;
            case "scale":
                this.scaleGroupCollapsed = !this.scaleGroupCollapsed;
                break;
            case "pose":
                this.poseGroupCollapsed = !this.poseGroupCollapsed;
                break;
        }
    }

    private boolean isGroupCollapsed(String group)
    {
        switch (group)
        {
            case "rotation":
                return this.rotationGroupCollapsed;
            case "position":
                return this.positionGroupCollapsed;
            case "scale":
                return this.scaleGroupCollapsed;
            case "pose":
                return this.poseGroupCollapsed;
            default:
                return false;
        }
    }

    private void handleZoom(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        if (!this.containsPoint(context.getMouseX(), context.getMouseY())) return;
        if (!context.isKeyHeld(GLFW.GLFW_KEY_LEFT_CONTROL)) return;

        float delta = context.getScrollDelta();
        if (Math.abs(delta) < 0.001f) return;

        float zoomFactor = delta > 0 ? 1.1f : 0.9f;
        float newScale = this.timeScale * zoomFactor;
        this.timeScale = Math.max(10.0f, Math.min(2000.0f, newScale));
    }

    private void handlePan(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        if (!this.containsPoint(context.getMouseX(), context.getMouseY())) return;

        boolean shiftDown = context.isKeyHeld(GLFW.GLFW_KEY_LEFT_SHIFT);
        boolean ctrlDown = context.isKeyHeld(GLFW.GLFW_KEY_LEFT_CONTROL);

        if (shiftDown && Math.abs(context.getScrollDelta()) > 0.001f)
        {
            this.scrollX -= context.getScrollDelta() * 30.0f;
            this.clampScrollX();
            return;
        }

        if (!shiftDown && !ctrlDown && Math.abs(context.getScrollDelta()) > 0.001f)
        {
            this.scrollY -= context.getScrollDelta() * 30.0f;
            this.clampScrollY();
            return;
        }

        if (context.isRightMouseDown() && !this.isDraggingKeyframe && !this.isDraggingPlayhead)
        {
            KeyframeHit hit = (context.isMouseDragging()) ? this.hitTestKeyframe(context.getMouseX(), context.getMouseY()) : null;

            if (hit == null && context.isMouseDragging())
            {
                this.scrollX -= context.getMouseDeltaX();
                this.clampScrollX();
                this.scrollY -= context.getMouseDeltaY();
                this.clampScrollY();
            }
        }
    }

    private void clampScrollX()
    {
        float maxScrollX = Math.max(0f, this.clipDuration * this.timeScale - this.getComputedWidth()
                + AnimationEditorTheme.LABEL_COLUMN_WIDTH);
        this.scrollX = Math.max(0f, Math.min(this.scrollX, maxScrollX));
    }

    private void clampScrollY()
    {
        float totalH = 0;
        String lastGroup = null;
        for (AnimationTrack<?> track : this.visibleTracks)
        {
            String group = KeyframeShapeRenderer.resolveGroup(track.getPropertyName());
            if (!group.equals(lastGroup))
            {
                totalH += AnimationEditorTheme.TRACK_HEADER_HEIGHT;
                lastGroup = group;
            }
            if (!this.isGroupCollapsed(group))
            {
                totalH += AnimationEditorTheme.TRACK_ROW_HEIGHT;
            }
        }

        float maxScrollY = Math.max(0f, totalH - (this.getComputedHeight() - RULER_HEIGHT));
        this.scrollY = Math.max(0f, Math.min(this.scrollY, maxScrollY));
    }


    private void handlePlayheadDrag(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        if (context.isMiddleMousePressed()
                && this.containsPoint(context.getMouseX(), context.getMouseY()))
        {
            this.isDraggingPlayhead = true;
        }

        if (this.isDraggingPlayhead)
        {
            if (context.isMiddleMouseDown())
            {
                float newTime = Math.max(0f, Math.min(this.clipDuration,
                        this.pixelToTime(context.getMouseX())));
                this.currentTime = newTime;
                if (this.callbacks != null) this.callbacks.onTimeChanged(newTime);
            } else
            {
                this.isDraggingPlayhead = false;
            }
        }
    }

    private void handleKeyframeDrag(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        if (!this.isDraggingKeyframe)
        {
            if (!context.isMousePressed()) return;
            KeyframeHit hit = this.hitTestKeyframe(context.getMouseX(), context.getMouseY());
            if (hit == null) return;

            this.isDraggingKeyframe = true;
            this.dragBoneId = hit.targetId;
            this.dragProperty = hit.property;
            this.dragOriginalTime = hit.time;
            this.dragCurrentTime = hit.time;
            return;
        }

        if (context.isMouseDown())
        {
            this.dragCurrentTime = Math.max(0f,
                    Math.min(this.clipDuration, this.pixelToTime(context.getMouseX())));
        } else
        {
            float timeDelta = Math.abs(this.dragCurrentTime - this.dragOriginalTime);
            if (timeDelta < DRAG_TIME_THRESHOLD)
            {
                if (this.callbacks != null)
                {
                    this.callbacks.onKeyframeSelected(this.dragBoneId, this.dragProperty, this.dragOriginalTime);
                }
                this.selectedTrackTargetId = this.dragBoneId;
                this.selectedProperty = this.dragProperty;
                this.selectedKeyTime = this.dragOriginalTime;
            } else
            {
                if (this.callbacks != null)
                {
                    this.callbacks.onKeyframeMoved(this.dragBoneId, this.dragProperty, this.dragOriginalTime, this.dragCurrentTime);
                }
            }

            this.isDraggingKeyframe = false;
            this.dragBoneId = null;
            this.dragProperty = null;
        }
    }

    private void handleKeyframeClick(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
    }

    private KeyframeHit hitTestKeyframe(float mouseX, float mouseY)
    {
        float startY = this.tracksStartY();
        float rowH = AnimationEditorTheme.TRACK_ROW_HEIGHT;
        float headerH = AnimationEditorTheme.TRACK_HEADER_HEIGHT;
        float hitRadius = AnimationEditorTheme.KEYFRAME_DIAMOND_SIZE + 5.0f;

        float cursorY = startY;
        String lastGroup = null;

        for (AnimationTrack<?> track : this.visibleTracks)
        {
            String group = KeyframeShapeRenderer.resolveGroup(track.getPropertyName());
            if (!group.equals(lastGroup))
            {
                cursorY += headerH;
                lastGroup = group;
            }

            if (this.isGroupCollapsed(group)) continue;

            float rowTop = cursorY;
            cursorY += rowH;

            if (mouseY < rowTop || mouseY >= rowTop + rowH) continue;

            for (Keyframe<?> kf : track.getKeyframes())
            {
                float kx = this.timeToPixel(kf.getTime());
                if (Math.abs(mouseX - kx) <= hitRadius)
                    return new KeyframeHit(track.getTargetId(), track.getPropertyName(), kf.getTime());
            }
        }
        return null;
    }


    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        this.ensureFontAtlas();

        float[] bg = AnimationEditorTheme.PANEL_BG;
        renderer.drawRect(x, y, w, h, bg[0], bg[1], bg[2], 1.0f);

        renderer.pushClip(x, y, w, h);

        if (this.visibleTracks.isEmpty())
        {
            this.renderCenteredText(renderer, "No clip selected", x, y, w, h);
        } else
        {
            this.renderTimeGrid(renderer, x, y, w, h);
            this.renderTrackRows(renderer, x, y, w, h);
            this.renderRuler(renderer, x, y, w);
            this.renderPlayhead(renderer, y, h);
        }

        if (this.visibleTracks.isEmpty()) this.renderRuler(renderer, x, y, w);

        renderer.popClip();

        float[] border = AnimationEditorTheme.BORDER_COLOR;
        renderer.drawRect(x, y, w, 1f, border[0], border[1], border[2], 1.0f);
    }

    private void renderTrackRows(UIRenderer renderer, float panelX, float panelY,
                                 float panelW, float panelH)
    {
        float rowH = AnimationEditorTheme.TRACK_ROW_HEIGHT;
        float headerH = AnimationEditorTheme.TRACK_HEADER_HEIGHT;
        float labelEndX = this.labelColumnEndX();
        float labelW = AnimationEditorTheme.LABEL_COLUMN_WIDTH;
        float accentBarW = AnimationEditorTheme.TRACK_GROUP_ACCENT_BAR_W;
        float indent = AnimationEditorTheme.TRACK_SUBROW_INDENT;

        float cursorY = this.tracksStartY();
        String lastGroup = null;
        int subRowIndex = 0;

        for (AnimationTrack<?> track : this.visibleTracks)
        {
            String group = KeyframeShapeRenderer.resolveGroup(track.getPropertyName());
            float[] accent = resolveAccentColor(group);
            String groupLabel = resolveGroupLabel(group);

            if (!group.equals(lastGroup))
            {
                lastGroup = group;
                subRowIndex = 0;
                float hdrTop = cursorY;
                cursorY += headerH;

                boolean collapsed = this.isGroupCollapsed(group);

                float[] hbg = AnimationEditorTheme.TRACK_GROUP_HEADER_BG;
                renderer.drawRect(panelX, hdrTop, panelW, headerH,
                        hbg[0], hbg[1], hbg[2], 1.0f);

                renderer.drawRect(panelX, hdrTop, accentBarW, headerH,
                        accent[0], accent[1], accent[2], 1.0f);

                float chevX = (float) Math.round(panelX + accentBarW + 4f);
                float chevCY = (float) Math.round(hdrTop + headerH * 0.5f);
                this.drawChevron(renderer, chevX, chevCY, collapsed, accent);

                float iconX = panelX + accentBarW + 18f;
                float iconCY = hdrTop + headerH * 0.5f;
                this.drawChannelIcon(renderer, group, (float) Math.round(iconX), (float) Math.round(iconCY), accent);

                String boneName = this.cleanBoneName(track.getTargetId());
                String headerText = boneName + "  " + groupLabel;
                renderer.pushClip(panelX, hdrTop, AnimationEditorTheme.LABEL_COLUMN_WIDTH, headerH);
                this.renderText(renderer, headerText, iconX + 18f, hdrTop + 6f, 12.0f, AnimationEditorTheme.TEXT_PRIMARY);
                renderer.popClip();

                float[] div = AnimationEditorTheme.BORDER_COLOR;
                renderer.drawRect(panelX, hdrTop + headerH - 1f, panelW, 1f, div[0], div[1], div[2], 1.0f);
            }

            if (this.isGroupCollapsed(group)) continue;

            float rowTop = cursorY;
            cursorY += rowH;

            float[] rowBg = (subRowIndex % 2 == 0) ? AnimationEditorTheme.TRACK_BG_EVEN : AnimationEditorTheme.TRACK_BG_ODD;
            renderer.drawRect(panelX, rowTop, panelW, rowH, rowBg[0], rowBg[1], rowBg[2], 1.0f);

            float[] lbg = AnimationEditorTheme.TOOLBAR_BG;
            renderer.drawRect(panelX, rowTop, labelW, rowH, lbg[0], lbg[1], lbg[2], 1.0f);

            renderer.drawRect(panelX, rowTop, accentBarW, rowH,
                    accent[0], accent[1], accent[2], 0.45f);

            float[] dotColor = this.resolveAxisDotColor(track.getPropertyName());
            float dotCY = rowTop + rowH * 0.5f;
            float dotR = 5f;
            float dotX = (float) Math.round(panelX + indent + 3f);
            float dotY = (float) Math.round(dotCY - dotR * 0.5f);
            renderer.drawRect(dotX, dotY, dotR, dotR, dotColor[0], dotColor[1], dotColor[2], 1.0f);

            String subLabel = this.resolveSubLabel(track.getPropertyName());
            renderer.pushClip(panelX, rowTop, AnimationEditorTheme.LABEL_COLUMN_WIDTH, rowH);
            this.renderText(renderer, subLabel, dotX + dotR + 5f, rowTop + 5f, 11.0f, AnimationEditorTheme.TEXT_SECONDARY);
            renderer.popClip();

            float[] div = AnimationEditorTheme.DIVIDER_COLOR;
            renderer.drawRect(panelX, rowTop + rowH - 1f, panelW, 1f, div[0], div[1], div[2], 1.0f);

            this.renderKeyframesForTrack(renderer, track, dotCY);

            subRowIndex++;
        }

        float[] border = AnimationEditorTheme.BORDER_COLOR;
        renderer.drawRect(labelEndX, panelY, 1f, panelH, border[0], border[1], border[2], 1.0f);
    }

    private void drawChevron(UIRenderer renderer, float x, float cy, boolean collapsed, float[] c)
    {
        if (collapsed)
        {
            renderer.drawRect(x, cy - 5f, 2f, 10f, c[0], c[1], c[2], 1.0f);
            renderer.drawRect(x + 2f, cy - 3f, 2f, 6f, c[0], c[1], c[2], 1.0f);
            renderer.drawRect(x + 4f, cy - 1f, 2f, 2f, c[0], c[1], c[2], 1.0f);
        } else
        {
            renderer.drawRect(x, cy - 2f, 10f, 2f, c[0], c[1], c[2], 1.0f);
            renderer.drawRect(x + 2f, cy, 6f, 2f, c[0], c[1], c[2], 1.0f);
            renderer.drawRect(x + 4f, cy + 2f, 2f, 2f, c[0], c[1], c[2], 1.0f);
        }
    }

    private void drawChannelIcon(UIRenderer renderer, String group,
                                 float x, float cy, float[] c)
    {
        switch (group)
        {
            case "rotation":
                renderer.drawRect(x, cy - 5f, 11f, 3f, c[0], c[1], c[2], 1.0f); // top
                renderer.drawRect(x, cy + 3f, 11f, 3f, c[0], c[1], c[2], 1.0f); // bottom
                renderer.drawRect(x, cy - 5f, 3f, 11f, c[0], c[1], c[2], 1.0f); // left
                renderer.drawRect(x + 8f, cy - 5f, 3f, 11f, c[0], c[1], c[2], 1.0f); // right
                renderer.drawRect(x + 8f, cy - 7f, 5f, 2f, c[0], c[1], c[2], 1.0f);
                renderer.drawRect(x + 9f, cy - 7f, 2f, 5f, c[0], c[1], c[2], 1.0f);
                break;

            case "position":
                renderer.drawRect(x, cy - 2f, 14f, 4f, c[0], c[1], c[2], 1.0f); // horizontal
                renderer.drawRect(x + 5f, cy - 7f, 4f, 14f, c[0], c[1], c[2], 1.0f); // vertical
                break;

            case "scale":
                renderer.drawRect(x + 3f, cy - 3f, 7f, 6f, c[0], c[1], c[2], 1.0f); // center
                renderer.drawRect(x, cy - 7f, 5f, 2f, c[0], c[1], c[2], 1.0f); // TL horiz
                renderer.drawRect(x, cy - 7f, 2f, 5f, c[0], c[1], c[2], 1.0f); // TL vert
                renderer.drawRect(x + 8f, cy + 5f, 5f, 2f, c[0], c[1], c[2], 1.0f); // BR horiz
                renderer.drawRect(x + 11f, cy + 2f, 2f, 5f, c[0], c[1], c[2], 1.0f); // BR vert
                break;

            case "pose":
                renderer.drawRect(x + 4f, cy - 8f, 5f, 5f, c[0], c[1], c[2], 1.0f); // head
                renderer.drawRect(x + 6f, cy - 3f, 2f, 5f, c[0], c[1], c[2], 1.0f); // body
                renderer.drawRect(x + 1f, cy, 11f, 2f, c[0], c[1], c[2], 1.0f); // arms
                renderer.drawRect(x + 4f, cy + 3f, 2f, 5f, c[0], c[1], c[2], 1.0f); // leg L
                renderer.drawRect(x + 8f, cy + 3f, 2f, 5f, c[0], c[1], c[2], 1.0f); // leg R
                break;

            default:
                renderer.drawRect(x + 2f, cy - 4f, 9f, 8f, c[0], c[1], c[2], 0.7f);
                break;
        }
    }

    /**
     * Returns the accent color array for a given group string
     */
    private static float[] resolveAccentColor(String group)
    {
        switch (group)
        {
            case "rotation":
                return AnimationEditorTheme.TRACK_ROTATION_ACCENT;
            case "position":
                return AnimationEditorTheme.TRACK_POSITION_ACCENT;
            case "scale":
                return AnimationEditorTheme.TRACK_SCALE_ACCENT;
            case "pose":
                return AnimationEditorTheme.TRACK_POSE_ACCENT;
            default:
                return AnimationEditorTheme.TEXT_SECONDARY;
        }
    }

    private static String resolveGroupLabel(String group)
    {
        switch (group)
        {
            case "rotation":
                return "Rotation";
            case "position":
                return "Position";
            case "scale":
                return "Scale";
            case "pose":
                return "Pose";
            default:
                return group;
        }
    }

    private float[] resolveAxisDotColor(String property)
    {
        if (property == null) return AnimationEditorTheme.TEXT_SECONDARY;
        int lastColon = property.lastIndexOf(':');
        if (lastColon == -1) return AnimationEditorTheme.TEXT_SECONDARY;

        String comp = property.substring(lastColon + 1);
        if (comp.endsWith(".x")) return AnimationEditorTheme.AXIS_X_COLOR;
        if (comp.endsWith(".y")) return AnimationEditorTheme.AXIS_Y_COLOR;
        if (comp.endsWith(".z")) return AnimationEditorTheme.AXIS_Z_COLOR;
        return AnimationEditorTheme.TEXT_SECONDARY;
    }

    private String resolveSubLabel(String property)
    {
        if (property == null) return property;
        int lastColon = property.lastIndexOf(':');
        if (lastColon == -1) return property;

        String comp = property.substring(lastColon + 1);
        switch (comp)
        {
            case "rotation.x":
                return "Pitch (X) - tilt forward/back";
            case "rotation.y":
                return "Yaw (Y) - turn left/right";
            case "rotation.z":
                return "Roll (Z) - tilt sideways";
            case "position.x":
                return "X - left / right";
            case "position.y":
                return "Y - up / down";
            case "position.z":
                return "Z - forward / back";
            case "scale.x":
                return "X - width";
            case "scale.y":
                return "Y - height";
            case "scale.z":
                return "Z - depth";
            case "visible":
                return "Visible";
            default:
                return comp;
        }
    }


    private String cleanBoneName(String targetId)
    {
        if (targetId == null) return "Unknown";
        String displayName = targetId;
        if (this.skeleton != null)
        {
            BoneInfo bone = this.skeleton.getBone(targetId).orElse(null);
            if (bone != null) displayName = bone.getDisplayName();
        }
        if (displayName.startsWith("bone:")) displayName = displayName.substring(5);
        return displayName;
    }

    private void renderKeyframesForTrack(UIRenderer renderer, AnimationTrack<?> track, float rowCY)
    {
        for (Keyframe<?> keyframe : track.getKeyframes())
        {
            float kx = this.timeToPixel(keyframe.getTime());
            if (kx < this.labelColumnEndX() || kx > this.getComputedX() + this.getComputedWidth()) continue;

            boolean isSelected = track.getTargetId().equals(this.selectedTrackTargetId)
                    && track.getPropertyName().equals(this.selectedProperty)
                    && Float.compare(keyframe.getTime(), this.selectedKeyTime) == 0;

            float drawX = (this.isDraggingKeyframe && track.getTargetId().equals(this.dragBoneId) && track.getPropertyName().equals(this.dragProperty) && Float.compare(keyframe.getTime(), this.dragOriginalTime) == 0) ? this.timeToPixel(this.dragCurrentTime) : kx;

            KeyframeShapeRenderer.drawKeyframeMarker(renderer, drawX, rowCY, track.getPropertyName(), isSelected);
        }
    }

    private void renderRuler(UIRenderer renderer, float panelX, float panelY, float panelW)
    {
        float rulerY = panelY;
        float labelEndX = this.labelColumnEndX();

        float[] rulerBg = AnimationEditorTheme.TOOLBAR_BG;
        renderer.drawRect(panelX, rulerY, panelW, RULER_HEIGHT, rulerBg[0], rulerBg[1], rulerBg[2], 1.0f);

        float minorStart = (float) Math.floor((this.scrollX / this.timeScale) / MINOR_TICK_SECONDS) * MINOR_TICK_SECONDS;
        float minorEnd = this.pixelToTime(panelX + panelW) + MINOR_TICK_SECONDS;

        for (float t = minorStart; t <= minorEnd; t += MINOR_TICK_SECONDS)
        {
            if (t < 0) continue;
            float tx = this.timeToPixel(t);
            if (tx < labelEndX || tx > panelX + panelW) continue;

            boolean isMajor = Math.abs(t % MAJOR_TICK_SECONDS) < 0.001f;
            float tickH = isMajor ? MAJOR_TICK_H : MINOR_TICK_H;
            float[] col = AnimationEditorTheme.DIVIDER_COLOR;
            renderer.drawRect(tx, rulerY + RULER_HEIGHT - tickH, 1f, tickH,
                    col[0], col[1], col[2], 1.0f);

            if (isMajor)
            {
                String timeLabel = String.format(Locale.US, "%.1fs", t);
                this.renderText(renderer, timeLabel, tx + 2f, rulerY + 4f, 10.0f, AnimationEditorTheme.TEXT_SECONDARY);
            }
        }
    }

    private void renderTimeGrid(UIRenderer renderer, float panelX, float panelY, float panelW, float panelH)
    {
        float gridTop = panelY + RULER_HEIGHT;
        float gridBottom = panelY + panelH;
        float labelEndX = this.labelColumnEndX();

        float minorStart = (float) Math.floor((this.scrollX / this.timeScale) / MINOR_TICK_SECONDS) * MINOR_TICK_SECONDS;
        float minorEnd = this.pixelToTime(panelX + panelW) + MINOR_TICK_SECONDS;

        for (float t = minorStart; t <= minorEnd; t += MINOR_TICK_SECONDS)
        {
            if (t < 0) continue;
            float tx = this.timeToPixel(t);
            if (tx < labelEndX || tx > panelX + panelW) continue;

            boolean isMajor = Math.abs(t % MAJOR_TICK_SECONDS) < 0.001f;
            float[] col = isMajor ? AnimationEditorTheme.TIME_GRID_MAJOR : AnimationEditorTheme.TIME_GRID_LINE;
            renderer.drawRect(tx, gridTop, 1f, gridBottom - gridTop, col[0], col[1], col[2], col[3]);
        }
    }

    private void renderPlayhead(UIRenderer renderer, float panelY, float panelH)
    {
        float px = this.timeToPixel(this.currentTime);
        if (px < this.labelColumnEndX()) return;

        float[] c = AnimationEditorTheme.PLAYHEAD_COLOR;

        float triTop = panelY + 2f;
        float triH = 10f;
        float triW = 5f;
        renderer.drawRect(px - triW, triTop, triW * 2f, 3f, c[0], c[1], c[2], 1.0f);
        renderer.drawRect(px - triW + 1f, triTop + 3f, triW * 2f - 2f, 3f, c[0], c[1], c[2], 1.0f);
        renderer.drawRect(px - 1f, triTop + 6f, 3f, 4f, c[0], c[1], c[2], 1.0f);

        renderer.drawRect(px - 1f, panelY, 2f, panelH, c[0], c[1], c[2], 0.85f);
    }

    private void renderText(UIRenderer renderer, String text, float x, float y, float targetHeight, float[] color)
    {
        if (this.fontAtlas == null) return;
        CachedTextMesh mesh = this.getOrBuildText(text);
        if (mesh == null) return;

        float scale = 1.0f;
        if (mesh.height > 0) scale = targetHeight / mesh.height;

        Font font = FontManager.getFont(FONT_KEY);
        float ty = y;
        if (font != null) ty += font.getBaseline() * scale;

        renderer.drawText(mesh.data, this.fontAtlas,
                x, ty, scale, color[0], color[1], color[2], color[3]);
    }

    private void renderCenteredText(UIRenderer renderer, String text,
                                    float x, float y, float w, float h)
    {
        if (this.fontAtlas == null) return;
        CachedTextMesh mesh = this.getOrBuildText(text);
        if (mesh == null) return;

        float targetHeight = 16.0f;
        float scale = 1.0f;
        if (mesh.height > 0) scale = targetHeight / mesh.height;

        float tx = x + (w - mesh.width * scale) * 0.5f;
        float ty = y + (h - mesh.height * scale) * 0.5f;

        Font font = FontManager.getFont(FONT_KEY);
        if (font != null) ty += font.getBaseline() * scale;

        float[] col = AnimationEditorTheme.TEXT_SECONDARY;
        renderer.drawText(mesh.data, this.fontAtlas,
                tx, ty, scale, col[0], col[1], col[2], col[3]);
    }


    private void ensureFontAtlas()
    {
        if (this.fontAtlas == null)
            this.fontAtlas = FontManager.getAtlas(FONT_KEY);
    }

    private CachedTextMesh getOrBuildText(String text)
    {
        if (this.textCache.containsKey(text)) return this.textCache.get(text);

        Font font = FontManager.getFont(FONT_KEY);
        if (font == null) return null;

        TextShaper shaper = new TextShaper();
        TextLayout layout = shaper.shape(text, font, font.getNativeSize());
        MeshData data = TextMeshGenerator.generate(layout, font);
        if (data.indices.length == 0) return null;

        BufferLayout spec =
                BufferLayout.builder()
                        .add(0, 3, AttributeType.FLOAT)
                        .add(1, 2, AttributeType.FLOAT)
                        .build();

        VertexBuffer vbo = new VertexBuffer(
                data.vertices, GpuBufferUsage.STATIC);
        VertexBuffer ebo = new VertexBuffer(
                data.indices, GpuBufferUsage.STATIC);
        Vao vao = Vao.builder()
                .bindVertexBuffer(vbo, spec).elementBuffer(ebo).build();

        CachedTextMesh mesh = new CachedTextMesh(data, layout.getWidth(), layout.getHeight());
        this.textCache.put(text, mesh);
        return mesh;
    }

    public void cleanup()
    {
        for (CachedTextMesh mesh : this.textCache.values()) mesh.destroy();
        this.textCache.clear();
    }


    private void renderTrackLabel(UIRenderer renderer, TrackLabelInfo info,
                                  float panelX, float rowTop)
    {
        float rowH = AnimationEditorTheme.TRACK_ROW_HEIGHT;
        float iconCY = rowTop + rowH * 0.5f;
        float iconX = panelX + 5f;
        float iconSize = 7f;

        float[] iconColor = info.channelColor;

        switch (info.channelType)
        {
            case "position":
                renderer.drawRect(iconX, iconCY - iconSize * 0.5f, iconSize, iconSize, iconColor[0], iconColor[1], iconColor[2], 1.0f);
                break;

            case "rotation":
                float r = iconSize * 0.5f;
                float cx = iconX + r;
                renderer.drawRect(cx - r, iconCY - 2f, iconSize, 4f, iconColor[0], iconColor[1], iconColor[2], 1.0f);
                renderer.drawRect(cx - 2f, iconCY - r, 4f, iconSize, iconColor[0], iconColor[1], iconColor[2], 1.0f);
                // Diagonal bars to round it out
                renderer.drawRect(cx - r * 0.72f, iconCY - r * 0.72f, r * 1.44f, r * 1.44f, iconColor[0], iconColor[1], iconColor[2], 0.55f);
                break;

            case "scale":
                float hs = iconSize * 0.5f;
                float dcx = iconX + hs;
                renderer.drawRect(dcx - hs, iconCY - 1.5f, iconSize, 3f, iconColor[0], iconColor[1], iconColor[2], 1.0f);
                renderer.drawRect(dcx - 1.5f, iconCY - hs, 3f, iconSize, iconColor[0], iconColor[1], iconColor[2], 1.0f);
                renderer.drawRect(dcx - hs * 0.75f, iconCY - hs * 0.75f,
                        hs * 1.5f, hs * 1.5f, iconColor[0], iconColor[1], iconColor[2], 0.6f);
                break;

            default:
                renderer.drawRect(iconX, iconCY - iconSize * 0.5f, iconSize, iconSize, AnimationEditorTheme.TEXT_SECONDARY[0], AnimationEditorTheme.TEXT_SECONDARY[1], AnimationEditorTheme.TEXT_SECONDARY[2], 1.0f);
                break;
        }

        float textX = iconX + iconSize + 4f;
        this.renderText(renderer, info.boneName, textX, rowTop + 4f, 11.0f,
                AnimationEditorTheme.TEXT_PRIMARY);

        if (!info.axis.isEmpty())
        {
            float axisX = this.labelColumnEndX() - 14f;
            this.renderText(renderer, info.axis, axisX, rowTop + 4f, 11.0f, info.axisColor);
        }
    }

    private record TrackLabelInfo(String boneName, String channelType, String axis, float[] channelColor, float[] axisColor)
    {
        static final float[] COLOR_POSITION = {0.35f, 0.65f, 1.00f, 1.0f}; // blue
        static final float[] COLOR_ROTATION = {0.42f, 0.85f, 0.42f, 1.0f}; // green
        static final float[] COLOR_SCALE = {1.00f, 0.60f, 0.20f, 1.0f}; // orange
        static final float[] COLOR_GENERIC = {0.70f, 0.70f, 0.70f, 1.0f}; // grey

        static final float[] COLOR_AXIS_X = {0.95f, 0.30f, 0.30f, 1.0f}; // red
        static final float[] COLOR_AXIS_Y = {0.30f, 0.90f, 0.30f, 1.0f}; // green
        static final float[] COLOR_AXIS_Z = {0.30f, 0.50f, 1.00f, 1.0f}; // blue
        static final float[] COLOR_AXIS_DEF = {0.75f, 0.75f, 0.75f, 1.0f}; // grey


        static TimelinePanel.TrackLabelInfo parse(Skeleton skeleton, String targetId, String propertyName)
        {
            if (propertyName.startsWith("bone:"))
            {
                int lastColon = propertyName.lastIndexOf(':');
                if (lastColon <= 5)
                    return new TimelinePanel.TrackLabelInfo(targetId, propertyName, "", COLOR_GENERIC, COLOR_AXIS_DEF);

                String component = propertyName.substring(lastColon + 1); // e.g. "rotation.x"
                String[] sub = component.split("\\.", 2);
                String channel = sub.length == 2 ? sub[0] : component; // "rotation"
                String rawAxis = sub.length == 2 ? sub[1] : "";

                float[] chColor;
                switch (channel)
                {
                    case "position":
                        chColor = COLOR_POSITION;
                        break;
                    case "rotation":
                        chColor = COLOR_ROTATION;
                        break;
                    case "scale":
                        chColor = COLOR_SCALE;
                        break;
                    default:
                        chColor = COLOR_GENERIC;
                        break;
                }

                String axisLabel;
                float[] axColor;
                switch (rawAxis.toLowerCase(Locale.US))
                {
                    case "x":
                        axisLabel = "X";
                        axColor = COLOR_AXIS_X;
                        break;
                    case "y":
                        axisLabel = "Y";
                        axColor = COLOR_AXIS_Y;
                        break;
                    case "z":
                        axisLabel = "Z";
                        axColor = COLOR_AXIS_Z;
                        break;
                    default:
                        axisLabel = rawAxis.toUpperCase(Locale.US);
                        axColor = COLOR_AXIS_DEF;
                        break;
                }

                String displayName = targetId;
                if (skeleton != null)
                {
                    BoneInfo bone = skeleton.getBone(targetId).orElse(null);
                    if (bone != null) displayName = bone.getDisplayName();
                }
                if (displayName.startsWith("bone:")) displayName = displayName.substring(5);

                return new TimelinePanel.TrackLabelInfo(displayName, channel, axisLabel, chColor, axColor);
            }

            return new TimelinePanel.TrackLabelInfo(targetId, propertyName, "", COLOR_GENERIC, COLOR_AXIS_DEF);
        }
    }


    private record KeyframeHit(String targetId, String property, float time)
    {
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

        static CachedTextMesh upload(MeshData data, BufferLayout layout, float width, float height)
        {
            return new CachedTextMesh(data, width, height);
        }

        void destroy()
        {
        }
    }

}
