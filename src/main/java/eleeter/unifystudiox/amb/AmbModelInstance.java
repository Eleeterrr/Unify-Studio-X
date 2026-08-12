package eleeter.unifystudiox.amb;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.anchor.BoneTarget;
import eleeter.unifystudiox.gizmo.Ray;
import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.scene.SelectionResult;
import eleeter.unifystudiox.scene.entity.HierarchicalEntity;
import eleeter.unifystudiox.scene.entity.Pickable;
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.RiggedEntity;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.entity.SkeletalData;
import eleeter.unifystudiox.scene.io.SerializeProperty;

public class AmbModelInstance
        implements SceneEntity, Positionable, Pickable, HierarchicalEntity, RiggedEntity, BoneTarget
{
    private final String id;

    private TextureGL texture;
    private int selectedBoneIndex = -1;

    private final Matrix4f correction = new Matrix4f().rotateX((float) Math.toRadians(0));

    private int hoveredBoneIndex = -1;
    private final AmbSkeletalData skeletalData;

    @Override
    public SkeletalData getSkeletalData()
    {
        return this.skeletalData;
    }

    @Override
    public int getHoveredBoneIndex()
    {
        return this.hoveredBoneIndex;
    }

    @Override
    public void setHoveredBoneIndex(int index)
    {
        this.hoveredBoneIndex = index;
    }

    private final List<AmbBoneInstance> boneInstances = new ArrayList<>();

    @Override
    public String getAssetPath()
    {
        return this.sourceModel.filePath;
    }

    public TextureGL getTexture()
    {
        return this.texture;
    }

    public void setTexture(TextureGL texture)
    {
        this.texture = texture;
    }

    @SerializeProperty
    public final AnimatrixGroup group;
    public final AmbModel sourceModel;
    private final AmbSkeleton skeleton;
    public final float[] boneMatrices;

    public AmbModelInstance(String id, AmbModel model)
    {
        this.id = id;
        this.sourceModel = model;
        this.skeleton = model.skeleton.copy();
        this.group = new AnimatrixGroup(model.filePath, new Vector3f(0.0F, 0.0F, 0.0F));
        this.group.localTransform.identity();
        this.group.worldTransform.identity();
        this.boneMatrices = new float[model.skeleton.bones.size() * 16];
        this.skeletalData = new AmbSkeletalData(this);

        for (AmbBone bone : this.skeleton.bones)
        {
            this.boneInstances.add(new AmbBoneInstance(this.id, bone, this));
        }
    }

    @Override
    public SceneEntity getSubEntity(int index)
    {
        if (index >= 0 && index < this.boneInstances.size())
        {
            return this.boneInstances.get(index);
        }
        return null;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    public Matrix4f getWorldMatrix()
    {
        return getModelMatrix();
    }

    @Override
    public Matrix4f getModelMatrix()
    {
        return new Matrix4f(group.localTransform).mul(correction);
    }

    private boolean animationPaused = false;

    public void setAnimationPaused(boolean paused)
    {
        this.animationPaused = paused;
    }

    @Override
    public void update(double deltaTime)
    {
        if (!this.animationPaused)
        {
            /* For animation */
        }
        this.update();
        this.group.worldTransform.set(this.group.localTransform);
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    public void update()
    {
        this.skeleton.calculateGlobalTransforms();
        int boneCount = Math.min(this.skeleton.bones.size(), 100);
        for (int i = 0; i < boneCount; i++)
        {
            AmbBone bone = this.skeleton.bones.get(i);
            Matrix4f finalTransform = new Matrix4f();
            bone.globalMatrix.mul(bone.inverseBindMatrix, finalTransform);

            finalTransform.get(this.boneMatrices, i * 16);
        }
    }

    @Override
    public Vector3f getPosition()
    {
        return this.group.translation;
    }

    @Override
    public void setPosition(Vector3f position)
    {
        this.group.translation.set(position);
        updateLocalTransform();
    }

    @Override
    public Quaternionf getRotation()
    {
        return new Quaternionf().rotationXYZ(this.group.rotation.x, this.group.rotation.y, this.group.rotation.z);
    }

    @Override
    public void setRotation(Quaternionf rotation)
    {
        rotation.getEulerAnglesXYZ(this.group.rotation);
        updateLocalTransform();
    }

    @Override
    public Vector3f getScale()
    {
        return this.group.scale;
    }

    @Override
    public void setScale(Vector3f scale)
    {
        this.group.scale.set(scale);
        updateLocalTransform();
    }

    private void updateLocalTransform()
    {
        this.group.localTransform.identity().translate(this.group.translation)
                .rotateXYZ(this.group.rotation.x, this.group.rotation.y, this.group.rotation.z).scale(this.group.scale);
        this.group.worldTransform.set(this.group.localTransform);
    }

    @Override
    public SelectionResult pick(Ray ray)
    {
        return AmbBonePicker.pick(ray, this);
    }

    @Override
    public int getSelectedBoneIndex()
    {
        return this.selectedBoneIndex;
    }

    @Override
    public void setSelectedBoneIndex(int index)
    {
        this.selectedBoneIndex = index;
    }

    @Override
    public Matrix4f getBoneWorldMatrix(String boneName)
    {
        List<AmbBone> bones = this.skeleton.bones;
        for (int i = 0; i < bones.size(); i++)
        {
            if (boneName.equals(bones.get(i).name))
            {
                return this.skeletalData.getBoneWorldMatrix(i);
            }
        }
        return null;
    }

    @Override
    public Matrix4f getBoneWorldMatrix(int boneIndex)
    {
        return this.skeletalData.getBoneWorldMatrix(boneIndex);
    }

    @Override
    public Matrix4f getRootWorldMatrix()
    {
        return new Matrix4f(getModelMatrix());
    }

    public AmbSkeleton getSkeleton()
    {
        return this.skeleton;
    }

    @Override
    public void cleanup()
    {
        /* nothing to clean LOL */
    }
}
