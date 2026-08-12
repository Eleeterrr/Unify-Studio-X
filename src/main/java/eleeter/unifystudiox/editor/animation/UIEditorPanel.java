package eleeter.unifystudiox.editor.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.animation.api.AnimatableObject;
import eleeter.unifystudiox.animation.api.AnimationSystem;
import eleeter.unifystudiox.animation.data.AnimationClip;
import eleeter.unifystudiox.animation.data.AnimationTrack;
import eleeter.unifystudiox.animation.data.BoneInfo;
import eleeter.unifystudiox.animation.data.EasingType;
import eleeter.unifystudiox.animation.data.Keyframe;
import eleeter.unifystudiox.animation.data.PoseKeyframe;
import eleeter.unifystudiox.animation.data.Skeleton;
import eleeter.unifystudiox.animation.data.Transform;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.framework.render.UIRenderer;

public class UIEditorPanel extends UIPanel implements AnimationEditorCallbacks, ViewportSelectionListener
{
    private final AnimationSystem animationSystem;

    private final BonesPanel bonesPanel;
    private final AnimationToolbar toolbar;
    private final KeyframeTypePanel keyframeTypePanel;
    private final TimelinePanel timelinePanel;
    private final PropertiesPanel propertiesPanel;

    private String selectedObjectId = null;
    private String selectedClipName = null;
    private String selectedBoneId = null;
    private boolean isPlaying = false;
    private boolean editorVisible = true;

    private float leftColWidth = AnimationEditorTheme.LEFT_COL_WIDTH;
    private float rightColWidth = AnimationEditorTheme.RIGHT_COL_WIDTH;
    private float topRowRatio = AnimationEditorTheme.TOP_ROW_RATIO;

    private enum DragHandle
    {NONE, LEFT_COL, RIGHT_COL, TOP_ROW}

    private DragHandle activeDrag = DragHandle.NONE;
    private DragHandle hoverDrag = DragHandle.NONE;

    private static final float RESIZE_MARGIN = 6.0f;
    private static final float MIN_PANEL_W = 150.0f;
    private static final float MIN_TIMELINE_H = 150.0f;
    private static final float MIN_VIEWPORT_H = 100.0f;

    public UIEditorPanel(String id, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, AnimationSystem animationSystem)
    {
        super(id);
        this.animationSystem = animationSystem;

        this.setBlocksInput(false);
        this.getTransform().set(0f, 0f, 1f, 1f);

        this.bonesPanel = new BonesPanel(id + "_bones");
        this.toolbar = new AnimationToolbar(id + "_toolbar", context);
        this.keyframeTypePanel = new KeyframeTypePanel(id + "_kf_type");
        this.timelinePanel = new TimelinePanel(id + "_timeline");
        this.propertiesPanel = new PropertiesPanel(id + "_props", context);

        this.bonesPanel.setCallbacks(this);
        this.toolbar.setCallbacks(this);
        this.keyframeTypePanel.setCallbacks(this);
        this.timelinePanel.setCallbacks(this);
        this.propertiesPanel.setCallbacks(this);

        this.addChild(this.bonesPanel);
        this.addChild(this.toolbar);
        this.addChild(this.keyframeTypePanel);
        this.addChild(this.timelinePanel);
        this.addChild(this.propertiesPanel);

        this.refreshObjectList();
    }

    public void onEditorOpened()
    {
        List<String> objectIds = this.animationSystem.getRegisteredObjects();
        if (objectIds != null && !objectIds.isEmpty())
        {
            this.selectedObjectId = objectIds.get(0);
        }

        if (this.selectedObjectId != null)
        {
            List<AnimationClip> clips = this.animationSystem.getClips(this.selectedObjectId);
            if (clips.isEmpty())
            {
                AnimationClip defaultShot = new AnimationClip.Builder()
                        .setName("Scene_001")
                        .setDuration(30.0f)
                        .build();
                this.animationSystem.addClip(this.selectedObjectId, defaultShot);
                this.selectedClipName = "Scene_001";
            } else
            {
                this.selectedClipName = clips.get(0).getName();
            }

            List<BoneInfo> boneList = this.animationSystem.getBonesFor(this.selectedObjectId);
            Skeleton skeleton = this.buildSkeleton(this.selectedObjectId);
            this.bonesPanel.setSkeleton(skeleton);

            List<AnimationTrack<?>> tracks = this.getTracksForSelectedClip();
            this.timelinePanel.setTracks(tracks);
            this.timelinePanel.setCurrentTime(0f);
            this.timelinePanel.setClipDuration(30f);

            this.toolbar.setSelectedObject(this.selectedObjectId);
            this.toolbar.setShotName(this.selectedClipName);
        }
    }


    @Override
    public void updateLayout(float parentX, float parentY, float parentW, float parentH)
    {
        this.computeLayout(parentW, parentH);
        super.updateLayout(parentX, parentY, parentW, parentH);
    }

    public void computeLayout(float screenWidth, float screenHeight)
    {
        float topRowHeight = screenHeight * this.topRowRatio;
        float bottomRowHeight = screenHeight - topRowHeight;
        float centerWidth = screenWidth - this.leftColWidth - this.rightColWidth;
        float viewportHeight = topRowHeight - AnimationEditorTheme.TOOLBAR_HEIGHT;

        this.applyBounds(this.bonesPanel, 0f, 0f, this.leftColWidth, topRowHeight);

        this.applyBounds(this.keyframeTypePanel, 0f, topRowHeight, this.leftColWidth, bottomRowHeight);

        this.applyBounds(this.propertiesPanel, this.leftColWidth + centerWidth, 0f, this.rightColWidth, screenHeight);

        this.applyBounds(this.toolbar, this.leftColWidth, viewportHeight, centerWidth, AnimationEditorTheme.TOOLBAR_HEIGHT);

        this.applyBounds(this.timelinePanel, this.leftColWidth, topRowHeight, centerWidth + this.rightColWidth, bottomRowHeight);
    }

    private void applyBounds(UIPanel panel, float x, float y, float w, float h)
    {
        panel.getTransform().set(0f, 0f, 0f, 0f).setPixelOffset((int) x, (int) y).setPixelSize(w, h);
        panel.markDirty();
    }

    public float[] getViewportBounds()
    {
        float screenW = this.getComputedWidth();
        float screenH = this.getComputedHeight();
        float topRowH = screenH * this.topRowRatio;
        float centerW = screenW - this.leftColWidth - this.rightColWidth;
        float viewportH = topRowH - AnimationEditorTheme.TOOLBAR_HEIGHT;

        return new float[]
        {
                this.getComputedX() + this.leftColWidth,
                this.getComputedY(), centerW, viewportH
        };
    }

    public void toggleMode()
    {
        this.editorVisible = !this.editorVisible;
        this.bonesPanel.setVisible(this.editorVisible);
        this.toolbar.setVisible(this.editorVisible);
        this.keyframeTypePanel.setVisible(this.editorVisible);
        this.timelinePanel.setVisible(this.editorVisible);
        this.propertiesPanel.setVisible(this.editorVisible);
        if (this.editorVisible)
        {
            this.onEditorOpened();
        }
    }


    @Override
    protected void updateSelfLogic(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context, double deltaTime)
    {
        if (!this.editorVisible) return;

        this.handleResizing(context);

        if (this.selectedObjectId == null) return;

        float currentTime = this.animationSystem.getCurrentTime(this.selectedObjectId);
        this.toolbar.setCurrentTime(currentTime);
        this.timelinePanel.setCurrentTime(currentTime);
    }

    private void handleResizing(eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        float mx = context.getMouseX() - this.getComputedX();
        float my = context.getMouseY() - this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        float leftEdge = this.leftColWidth;
        float rightEdge = w - this.rightColWidth;
        float topEdge = h * this.topRowRatio;

        if (!context.isMouseDown())
        {
            if (Math.abs(mx - leftEdge) <= RESIZE_MARGIN) this.hoverDrag = DragHandle.LEFT_COL;
            else if (Math.abs(mx - rightEdge) <= RESIZE_MARGIN) this.hoverDrag = DragHandle.RIGHT_COL;
            else if (Math.abs(my - topEdge) <= RESIZE_MARGIN) this.hoverDrag = DragHandle.TOP_ROW;
            else this.hoverDrag = DragHandle.NONE;
        }

        if (context.isMousePressed() && this.hoverDrag != DragHandle.NONE)
        {
            this.activeDrag = this.hoverDrag;
        } else if (!context.isMouseDown())
        {
            this.activeDrag = DragHandle.NONE;
        }

        if (this.activeDrag != DragHandle.NONE)
        {
            if (this.activeDrag == DragHandle.LEFT_COL)
            {
                float minL = MIN_PANEL_W;
                float maxL = w - this.rightColWidth - MIN_PANEL_W;
                this.leftColWidth = Math.max(minL, Math.min(maxL, mx));
            } else if (this.activeDrag == DragHandle.RIGHT_COL)
            {
                float minR = MIN_PANEL_W;
                float maxR = w - this.leftColWidth - MIN_PANEL_W;
                this.rightColWidth = Math.max(minR, Math.min(maxR, w - mx));
            } else if (this.activeDrag == DragHandle.TOP_ROW)
            {
                float minTopH = AnimationEditorTheme.TOOLBAR_HEIGHT + MIN_VIEWPORT_H;
                float maxTopH = h - MIN_TIMELINE_H;
                float topRowH = Math.max(minTopH, Math.min(maxTopH, my));
                this.topRowRatio = topRowH / h;
            }
            this.computeLayout(w, h);
        }
    }

    @Override
    protected void renderSelf(UIRenderer renderer)
    {
        if (!this.editorVisible) return;

        float x = this.getComputedX();
        float y = this.getComputedY();
        float w = this.getComputedWidth();
        float h = this.getComputedHeight();

        if (this.activeDrag != DragHandle.NONE)
        {
            float[] accent = AnimationEditorTheme.ACCENT_COLOR;
            if (this.activeDrag == DragHandle.LEFT_COL)
            {
                renderer.drawRect(x + this.leftColWidth - 1f, y, 2f, h, accent[0], accent[1], accent[2], 1.0f);
            } else if (this.activeDrag == DragHandle.RIGHT_COL)
            {
                renderer.drawRect(x + w - this.rightColWidth - 1f, y, 2f, h, accent[0], accent[1], accent[2], 1.0f);
            } else if (this.activeDrag == DragHandle.TOP_ROW)
            {
                float topY = y + (h * this.topRowRatio);
                renderer.drawRect(x, topY - 1f, w, 2f, accent[0], accent[1], accent[2], 1.0f);
            }
        }
    }


    @Override
    public void onPlayRequested()
    {
        if (this.selectedObjectId == null || this.selectedClipName == null) return;
        this.animationSystem.play(this.selectedObjectId, this.selectedClipName);
        this.isPlaying = true;
        this.toolbar.setPlayingState(true);
    }

    @Override
    public void onPauseRequested()
    {
        if (this.selectedObjectId == null) return;
        this.animationSystem.pause(this.selectedObjectId);
        this.isPlaying = false;
        this.toolbar.setPlayingState(false);
    }

    @Override
    public void onStopRequested()
    {
        if (this.selectedObjectId == null) return;
        this.animationSystem.pause(this.selectedObjectId);
        this.animationSystem.seekToTime(this.selectedObjectId, 0f);
        this.isPlaying = false;
        this.toolbar.setPlayingState(false);
        this.timelinePanel.setCurrentTime(0f);
        this.toolbar.setCurrentTime(0f);
    }

    @Override
    public void onAddKeyframeRequested(float time)
    {
        if (this.selectedObjectId == null) return;

        KeyframeType activeType = this.keyframeTypePanel.getSelectedType();
        if (activeType == KeyframeType.PROPERTY && this.selectedBoneId == null) return;

        this.ensureClipSelected();
        if (activeType == KeyframeType.POSE)
        {
            this.onAddPoseKeyframeRequested(time);
        } else
        {
            this.addPropertyKeyframesForBone(this.selectedBoneId, time);
        }
        this.refreshClipList();
        this.refreshTimeline();
    }

    @Override
    public void onAddPoseKeyframeRequested(float time)
    {
        if (this.selectedObjectId == null) return;
        this.ensureClipSelected();

        AnimatableObject animatable =
                this.animationSystem.getAnimatableObject(this.selectedObjectId);
        if (animatable == null) return;

        List<BoneInfo> bones =
                this.animationSystem.getBonesFor(this.selectedObjectId);

        Map<String, Transform> transforms = new HashMap<>();
        for (BoneInfo bone : bones)
        {
            String id = bone.getId();

            Float px = (Float) animatable.getProperty("bone:" + id + ":position.x");
            Float py = (Float) animatable.getProperty("bone:" + id + ":position.y");
            Float pz = (Float) animatable.getProperty("bone:" + id + ":position.z");

            Float rx = (Float) animatable.getProperty("bone:" + id + ":rotation.x");
            Float ry = (Float) animatable.getProperty("bone:" + id + ":rotation.y");
            Float rz = (Float) animatable.getProperty("bone:" + id + ":rotation.z");

            Float sx = (Float) animatable.getProperty("bone:" + id + ":scale.x");
            Float sy = (Float) animatable.getProperty("bone:" + id + ":scale.y");
            Float sz = (Float) animatable.getProperty("bone:" + id + ":scale.z");

            Transform t = new Transform(new Vector3f(px != null ? px : 0f, py != null ? py : 0f, pz != null ? pz : 0f), new Quaternionf().identity().rotateXYZ(rx != null ? rx : 0f, ry != null ? ry : 0f, rz != null ? rz : 0f), new Vector3f(sx != null ? sx : 1f, sy != null ? sy : 1f, sz != null ? sz : 1f)
            );
            transforms.put(id, t);
        }

        PoseKeyframe pose = new PoseKeyframe(time, EasingType.LINEAR, transforms);
        this.animationSystem.addPoseKeyframe(this.selectedObjectId, this.selectedClipName, pose);
        this.refreshClipList();
        this.refreshTimeline();
    }

    private void ensureClipSelected()
    {
        if (this.selectedClipName == null)
        {
            String newClipName = "Scene_001";

            boolean exists = false;
            for (AnimationClip clip : this.animationSystem.getClips(this.selectedObjectId))
            {
                if (clip.getName().equals(newClipName))
                {
                    exists = true;
                    break;
                }
            }
            if (!exists)
            {
                AnimationClip defaultClip = new AnimationClip.Builder().setName(newClipName).setDuration(30.0f).build();
                this.animationSystem.addClip(this.selectedObjectId, defaultClip);
            }

            this.onClipSelected(newClipName);
            this.refreshClipList();
        }
    }

    private void addPropertyKeyframesForBone(String boneId, float time)
    {
        if (this.selectedObjectId == null) return;
        AnimatableObject animatable =
                this.animationSystem.getAnimatableObject(this.selectedObjectId);
        if (animatable == null) return;

        String[] posProps =
        {"bone:" + boneId + ":position.x",
                "bone:" + boneId + ":position.y",
                "bone:" + boneId + ":position.z"};
        String[] rotProps =
        {"bone:" + boneId + ":rotation.x",
                "bone:" + boneId + ":rotation.y",
                "bone:" + boneId + ":rotation.z"};
        String[] scaleProps =
        {"bone:" + boneId + ":scale.x",
                "bone:" + boneId + ":scale.y",
                "bone:" + boneId + ":scale.z"};

        for (int i = 0; i < 3; i++)
        {
            Float posVal = (Float) animatable.getProperty(posProps[i]);
            Float rotVal = (Float) animatable.getProperty(rotProps[i]);
            Float scaleVal = (Float) animatable.getProperty(scaleProps[i]);

            this.animationSystem.addKeyframe(this.selectedObjectId, this.selectedClipName, boneId, posProps[i], new Keyframe<>(time, posVal != null ? posVal : 0f, EasingType.LINEAR));
            this.animationSystem.addKeyframe(this.selectedObjectId, this.selectedClipName, boneId, rotProps[i], new Keyframe<>(time, rotVal != null ? rotVal : 0f, EasingType.LINEAR));
            this.animationSystem.addKeyframe(this.selectedObjectId, this.selectedClipName, boneId, scaleProps[i], new Keyframe<>(time, scaleVal != null ? scaleVal : 1f, EasingType.LINEAR));
        }
    }

    @Override
    public void onBoneSelected(String boneId)
    {
        this.selectedBoneId = boneId;

        AnimatableObject animatableObject = this.animationSystem.getAnimatableObject(this.selectedObjectId);

        Map<String, Object> liveValues = new LinkedHashMap<>();
        liveValues.put("rotation.x", animatableObject.getProperty("bone:" + boneId + ":rotation.x"));
        liveValues.put("rotation.y", animatableObject.getProperty("bone:" + boneId + ":rotation.y"));
        liveValues.put("rotation.z", animatableObject.getProperty("bone:" + boneId + ":rotation.z"));
        liveValues.put("position.x", animatableObject.getProperty("bone:" + boneId + ":position.x"));
        liveValues.put("position.y", animatableObject.getProperty("bone:" + boneId + ":position.y"));
        liveValues.put("position.z", animatableObject.getProperty("bone:" + boneId + ":position.z"));
        liveValues.put("scale.x", animatableObject.getProperty("bone:" + boneId + ":scale.x"));
        liveValues.put("scale.y", animatableObject.getProperty("bone:" + boneId + ":scale.y"));
        liveValues.put("scale.z", animatableObject.getProperty("bone:" + boneId + ":scale.z"));

        for (String key : liveValues.keySet())
        {
            if (key.startsWith("rotation."))
            {
                Float radians = (Float) liveValues.get(key);
                if (radians != null)
                {
                    liveValues.put(key, (float) Math.toDegrees(radians));
                }
            }
        }

        this.propertiesPanel.setData(boneId, liveValues);
        this.timelinePanel.setSelectedBone(boneId);
        this.bonesPanel.setSelectedBone(boneId);

        List<AnimationTrack<?>> boneTracks = this.getTracksForBone(boneId);
        this.timelinePanel.setTracks(boneTracks);
    }

    @Override
    public void onKeyframeSelected(String boneId, String property, float time)
    {
        this.selectedBoneId = boneId;
        this.bonesPanel.setSelectedBone(boneId);

        Map<String, Object> liveValues = new LinkedHashMap<>();
        List<Keyframe<?>> keysPx = this.animationSystem.getKeyframesFor(this.selectedObjectId, boneId, "bone:" + boneId + ":position.x");
        List<Keyframe<?>> keysPy = this.animationSystem.getKeyframesFor(this.selectedObjectId, boneId, "bone:" + boneId + ":position.y");
        List<Keyframe<?>> keysPz = this.animationSystem.getKeyframesFor(this.selectedObjectId, boneId, "bone:" + boneId + ":position.z");
        List<Keyframe<?>> keysRx = this.animationSystem.getKeyframesFor(this.selectedObjectId, boneId, "bone:" + boneId + ":rotation.x");
        List<Keyframe<?>> keysRy = this.animationSystem.getKeyframesFor(this.selectedObjectId, boneId, "bone:" + boneId + ":rotation.y");
        List<Keyframe<?>> keysRz = this.animationSystem.getKeyframesFor(this.selectedObjectId, boneId, "bone:" + boneId + ":rotation.z");
        List<Keyframe<?>> keysSx = this.animationSystem.getKeyframesFor(this.selectedObjectId, boneId, "bone:" + boneId + ":scale.x");
        List<Keyframe<?>> keysSy = this.animationSystem.getKeyframesFor(this.selectedObjectId, boneId, "bone:" + boneId + ":scale.y");
        List<Keyframe<?>> keysSz = this.animationSystem.getKeyframesFor(this.selectedObjectId, boneId, "bone:" + boneId + ":scale.z");

        liveValues.put("position.x", this.findValueAtTime(keysPx, time));
        liveValues.put("position.y", this.findValueAtTime(keysPy, time));
        liveValues.put("position.z", this.findValueAtTime(keysPz, time));
        liveValues.put("rotation.x", this.findValueAtTime(keysRx, time));
        liveValues.put("rotation.y", this.findValueAtTime(keysRy, time));
        liveValues.put("rotation.z", this.findValueAtTime(keysRz, time));
        liveValues.put("scale.x", this.findValueAtTime(keysSx, time));
        liveValues.put("scale.y", this.findValueAtTime(keysSy, time));
        liveValues.put("scale.z", this.findValueAtTime(keysSz, time));

        for (String key : liveValues.keySet())
        {
            if (key.startsWith("rotation."))
            {
                Float radians = (Float) liveValues.get(key);
                if (radians != null)
                {
                    liveValues.put(key, (float) Math.toDegrees(radians));
                }
            }
        }

        this.propertiesPanel.setData(boneId, liveValues);
    }

    private Float findValueAtTime(List<Keyframe<?>> keys, float time)
    {
        for (Keyframe<?> kf : keys)
        {
            if (Float.compare(kf.getTime(), time) == 0)
            {
                return (Float) kf.getValue();
            }
        }
        return 0f;
    }

    @Override
    public void onKeyframeMoved(String boneId, String property, float oldTime, float newTime)
    {
        if (this.selectedObjectId == null || this.selectedClipName == null) return;

        List<Keyframe<?>> keyframes = this.animationSystem.getKeyframesFor(
                this.selectedObjectId, boneId, property);

        for (Keyframe<?> kf : keyframes)
        {
            if (Float.compare(kf.getTime(), oldTime) == 0)
            {
                @SuppressWarnings("unchecked")
                Keyframe<Object> typed = (Keyframe<Object>) kf;
                this.animationSystem.removeKeyframe(this.selectedObjectId, this.selectedClipName, boneId, property, oldTime);
                this.animationSystem.addKeyframe(this.selectedObjectId, this.selectedClipName, boneId, property,
                        new Keyframe<>(newTime, typed.getValue(), typed.getEasingType()));
                break;
            }
        }
        this.refreshTimeline();
    }

    @Override
    public void onTimeChanged(float newTime)
    {
        if (this.selectedObjectId == null) return;
        this.animationSystem.seekToTime(this.selectedObjectId, newTime);
        this.toolbar.setCurrentTime(newTime);
        this.timelinePanel.setCurrentTime(newTime);
    }

    @Override
    public void onPropertyChanged(String boneId, String property, float time, Object newValue)
    {
        if (this.selectedObjectId == null || this.selectedClipName == null) return;

        if (newValue instanceof Float)
        {
            float val = (Float) newValue;
            if (property.contains("rotation."))
            {
                val = (float) Math.toRadians(val);
            }
            this.animationSystem.addKeyframe(this.selectedObjectId, this.selectedClipName, boneId, property,
                    new Keyframe<>(time, val, EasingType.LINEAR));
            this.animationSystem.seekToTime(this.selectedObjectId, time);
        }
        this.refreshTimeline();
    }

    @Override
    public void onObjectSelected(String objectId)
    {
        this.selectedObjectId = objectId;
        this.selectedBoneId = null;
        this.selectedClipName = null;

        Skeleton skeleton = this.buildSkeleton(objectId);
        this.bonesPanel.setSkeleton(skeleton);
        this.timelinePanel.setSkeleton(skeleton);

        this.refreshClipList();

        this.propertiesPanel.clearSelection();
        this.timelinePanel.setTracks(Collections.emptyList());
        this.timelinePanel.setSelectedBone(null);
    }

    private void refreshClipList()
    {
        if (this.selectedObjectId == null) return;
        List<AnimationClip> clips = this.animationSystem.getClips(this.selectedObjectId);
        List<String> clipNames = new ArrayList<>();
        for (AnimationClip clip : clips) clipNames.add(clip.getName());
        this.toolbar.setClipList(Collections.unmodifiableList(clipNames));

        if (this.selectedClipName == null && !clipNames.isEmpty())
        {
            this.onClipSelected(clipNames.get(0));
            this.toolbar.setClipList(Collections.unmodifiableList(clipNames));
        }
    }

    @Override
    public void onClipSelected(String clipName)
    {
        this.selectedClipName = clipName;
        this.toolbar.setShotName(clipName);

        if (this.selectedObjectId != null)
        {
            boolean wasPlaying = this.isPlaying;
            float currentTime = this.animationSystem.getCurrentTime(this.selectedObjectId);

            this.animationSystem.play(this.selectedObjectId, clipName);
            if (!wasPlaying)
            {
                this.animationSystem.pause(this.selectedObjectId);
            }
            this.animationSystem.seekToTime(this.selectedObjectId, currentTime);
        }

        this.refreshTimeline();

        if (this.selectedObjectId != null)
        {
            for (AnimationClip clip : this.animationSystem.getClips(this.selectedObjectId))
            {
                if (clip.getName().equals(clipName))
                {
                    this.timelinePanel.setClipDuration(clip.getDurationSeconds());
                    break;
                }
            }
        }
    }

    @Override
    public void onKeyframeTypeSelected(KeyframeType type)
    {
    }


    private void refreshObjectList()
    {
        List<String> ids = this.animationSystem.getRegisteredObjects();
        this.toolbar.setObjectList(Collections.unmodifiableList(ids));
    }

    private void refreshTimeline()
    {
        if (this.selectedObjectId == null || this.selectedClipName == null) return;

        List<AnimationTrack<?>> tracks;
        if (this.selectedBoneId != null)
        {
            tracks = this.getTracksForBone(this.selectedBoneId);
        } else
        {
            tracks = this.getTracksForSelectedClip();
        }

        this.timelinePanel.setTracks(tracks);
    }

    private List<AnimationTrack<?>> getTracksForSelectedClip()
    {
        if (this.selectedObjectId == null || this.selectedClipName == null)
            return Collections.emptyList();

        for (AnimationClip clip : this.animationSystem.getClips(this.selectedObjectId))
        {
            if (clip.getName().equals(this.selectedClipName))
                return clip.getTracks();
        }
        return Collections.emptyList();
    }

    private List<AnimationTrack<?>> getTracksForBone(String boneId)
    {
        List<AnimationTrack<?>> all = this.getTracksForSelectedClip();
        List<AnimationTrack<?>> result = new ArrayList<>();
        for (AnimationTrack<?> track : all)
        {
            if (track.getTargetId().equals(boneId)) result.add(track);
        }
        return Collections.unmodifiableList(result);
    }

    private Skeleton buildSkeleton(String objectId)
    {
        List<BoneInfo> bones =
                this.animationSystem.getBonesFor(objectId);
        if (bones.isEmpty()) return new Skeleton(Collections.emptyList());
        return new Skeleton(bones);
    }

    private Map<String, Transform> buildPoseAtTime(float time)
    {
        Map<String, Transform> result = new HashMap<>();
        if (this.selectedObjectId == null) return result;

        List<BoneInfo> bones =
                this.animationSystem.getBonesFor(this.selectedObjectId);

        for (BoneInfo bone : bones)
        {
            result.put(bone.getId(), bone.getRestPose());
        }
        return result;
    }

    public void cleanup()
    {
        this.bonesPanel.cleanup();
        this.toolbar.cleanup();
        this.keyframeTypePanel.cleanup();
        this.timelinePanel.cleanup();
        this.propertiesPanel.cleanup();
    }


    @Override
    public void onViewportBoneSelected(String fullId)
    {
        int firstColon = fullId.indexOf(':');
        if (firstColon == -1) return;

        String entityId = fullId.substring(0, firstColon);
        String boneId = fullId.substring(firstColon + 1);

        if (this.selectedObjectId == null || !this.selectedObjectId.equals(entityId))
        {
            this.toolbar.setSelectedObject(entityId);
            this.onObjectSelected(entityId);
        }

        this.bonesPanel.setSelectedBone(boneId);
        this.onBoneSelected(boneId);
    }

    @Override
    public void onGizmoTransformChanged(String fullId,
                                        float px, float py, float pz,
                                        float rx, float ry, float rz,
                                        float sx, float sy, float sz)
    {
        int firstColon = fullId.indexOf(':');
        if (firstColon == -1) return;

        String entityId = fullId.substring(0, firstColon);
        String boneId = fullId.substring(firstColon + 1);

        if (entityId.equals(this.selectedObjectId) && boneId.equals(this.selectedBoneId) && this.selectedClipName != null)
        {
            float time = this.timelinePanel.getCurrentTime();
            if (this.animationSystem != null)
            {
                String pxProp = "bone:" + boneId + ":position.x";
                String pyProp = "bone:" + boneId + ":position.y";
                String pzProp = "bone:" + boneId + ":position.z";
                String rxProp = "bone:" + boneId + ":rotation.x";
                String ryProp = "bone:" + boneId + ":rotation.y";
                String rzProp = "bone:" + boneId + ":rotation.z";
                String sxProp = "bone:" + boneId + ":scale.x";
                String syProp = "bone:" + boneId + ":scale.y";
                String szProp = "bone:" + boneId + ":scale.z";

                this.animationSystem.addKeyframe(entityId, this.selectedClipName, boneId, pxProp, new Keyframe<>(time, px, EasingType.LINEAR));
                this.animationSystem.addKeyframe(entityId, this.selectedClipName, boneId, pyProp, new Keyframe<>(time, py, EasingType.LINEAR));
                this.animationSystem.addKeyframe(entityId, this.selectedClipName, boneId, pzProp, new Keyframe<>(time, pz, EasingType.LINEAR));

                this.animationSystem.addKeyframe(entityId, this.selectedClipName, boneId, rxProp, new Keyframe<>(time, rx, EasingType.LINEAR));
                this.animationSystem.addKeyframe(entityId, this.selectedClipName, boneId, ryProp, new Keyframe<>(time, ry, EasingType.LINEAR));
                this.animationSystem.addKeyframe(entityId, this.selectedClipName, boneId, rzProp, new Keyframe<>(time, rz, EasingType.LINEAR));

                this.animationSystem.addKeyframe(entityId, this.selectedClipName, boneId, sxProp, new Keyframe<>(time, sx, EasingType.LINEAR));
                this.animationSystem.addKeyframe(entityId, this.selectedClipName, boneId, syProp, new Keyframe<>(time, sy, EasingType.LINEAR));
                this.animationSystem.addKeyframe(entityId, this.selectedClipName, boneId, szProp, new Keyframe<>(time, sz, EasingType.LINEAR));

                this.refreshTimeline();
            }

            Map<String, Object> liveValues = new LinkedHashMap<>();
            liveValues.put("position.x", px);
            liveValues.put("position.y", py);
            liveValues.put("position.z", pz);
            liveValues.put("rotation.x", (float) Math.toDegrees(rx));
            liveValues.put("rotation.y", (float) Math.toDegrees(ry));
            liveValues.put("rotation.z", (float) Math.toDegrees(rz));
            liveValues.put("scale.x", sx);
            liveValues.put("scale.y", sy);
            liveValues.put("scale.z", sz);

            this.propertiesPanel.setData(boneId, liveValues);
        }
    }
}
