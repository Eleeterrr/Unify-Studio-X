package eleeter.unifystudiox.cubic;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.anchor.BoneTarget;
import eleeter.unifystudiox.cubic.render.CubeRuntimeModel;
import eleeter.unifystudiox.cubic.render.CubeRuntimeNode;
import eleeter.unifystudiox.gizmo.Ray;
import eleeter.unifystudiox.scene.SelectionResult;
import eleeter.unifystudiox.scene.entity.BaseSceneEntity;
import eleeter.unifystudiox.scene.entity.HierarchicalEntity;
import eleeter.unifystudiox.scene.entity.Pickable;
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.RiggedEntity;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.entity.SkeletalData;

public class CubicModelInstance extends BaseSceneEntity implements Positionable, Pickable, HierarchicalEntity, RiggedEntity, BoneTarget
{
    private final String id;
    private final CubeRuntimeModel model;
    private final CubicAnimationController animationController = new CubicAnimationController();
    private final CubicSkeletalData skeletalData;
    private final List<CubicNodeInstance> subEntities = new ArrayList<>();

    private int selectedElementIndex = -1;
    private Object selectedElement;

    private int hoveredBoneIndex = -1;

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

    private final Vector3f translation = new Vector3f(0);
    private final Quaternionf rotation = new Quaternionf(); // Internal stable rotation
    private final Vector3f scale = new Vector3f(1);
    private final Matrix4f worldTransform = new Matrix4f();

    public final float[] boneMatrices = new float[100 * 16];


    public CubicModelInstance(String id, CubeRuntimeModel model)
    {
        this.id = id;
        this.model = new CubeRuntimeModel(model.name, model.root.copy());
        this.model.vertexData = model.vertexData;
        this.model.indexData = model.indexData;
        this.model.mesh = model.mesh;
        this.model.animations.addAll(model.animations);

        this.skeletalData = new CubicSkeletalData(this.model, this.boneMatrices, this.worldTransform);

        updateGlobalMatrices();
        initializeSubEntities(this.model.root);
    }

    private void initializeSubEntities(CubeRuntimeNode node)
    {
        this.subEntities.add(new CubicNodeInstance(this.id, node, this));
        for (CubeRuntimeNode child : node.children)
        {
            initializeSubEntities(child);
        }
    }

    public void playAnimation(String name)
    {
        this.animationController.play(name);
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    private final Matrix4f cachedScaledMatrix = new Matrix4f();

    public Matrix4f getWorldMatrix()
    {
        this.worldTransform.mul(new Matrix4f().scale(0.0625f), cachedScaledMatrix);
        return cachedScaledMatrix;
    }

    @Override
    public Matrix4f getModelMatrix()
    {
        return this.worldTransform;
    }

    private boolean animationPaused = false;

    public void setAnimationPaused(boolean paused)
    {
        this.animationController.setPaused(paused);
    }

    public boolean isAnimationPaused()
    {
        return this.animationController.isPaused();
    }

    @Override
    public void update(double deltaTime)
    {
        this.animationController.update(this.model, deltaTime);
        updateGlobalMatrices();
    }

    @Override
    public SkeletalData getSkeletalData()
    {
        return this.skeletalData;
    }

    private void updateGlobalMatrices()
    {
        this.worldTransform.identity().translate(this.translation).rotate(this.rotation).scale(this.scale);

        this.model.update();

        for (int i = 0; i < this.model.flattenedNodes.size() && i < 100; i++)
        {
            CubeRuntimeNode node = this.model.flattenedNodes.get(i);
            if (node != null)
            {
                node.worldMatrix.get(this.boneMatrices, i * 16);
            }
        }
    }

    public CubeRuntimeModel getModel()
    {
        return this.model;
    }

    @Override
    public String getAssetPath()
    {
        return null;
    }

    @Override
    public SelectionResult pick(Ray ray)
    {
        return CubicRayPicker.pick(this, ray);
    }

    public int getSelectedElementIndex()
    {
        return selectedElementIndex;
    }

    @Override
    public int getSelectedBoneIndex()
    {
        return selectedElementIndex;
    }

    @Override
    public void setSelectedBoneIndex(int index)
    {
        this.selectedElementIndex = index;
    }

    public Object getSelectedElement()
    {
        return selectedElement;
    }

    @Override
    public Matrix4f getBoneWorldMatrix(String boneName)
    {
        CubeRuntimeNode node = this.model.nodesByName.get(boneName);
        if (node != null)
        {
            return new Matrix4f(this.worldTransform).scale(0.0625f).mul(node.worldMatrix).translate(node.pivot);
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
        return new Matrix4f(this.worldTransform);
    }

    @Override
    public void cleanup()
    {
        this.model.cleanup();
    }

    @Override
    public SceneEntity getSubEntity(int index)
    {
        for (CubicNodeInstance instance : this.subEntities)
        {
            if (instance.getNode().boneIndex == index)
            {
                return instance;
            }
        }
        return null;
    }

    public List<CubicNodeInstance> getSubEntities()
    {
        return this.subEntities;
    }

    @Override
    public Vector3f getPosition()
    {
        return this.translation;
    }

    @Override
    public void setPosition(Vector3f position)
    {
        this.translation.set(position);
        updateGlobalMatrices();
    }

    @Override
    public Quaternionf getRotation()
    {
        return new Quaternionf(this.rotation);
    }

    @Override
    public void setRotation(Quaternionf q)
    {
        this.rotation.set(q);
        updateGlobalMatrices();
    }

    @Override
    public Vector3f getScale()
    {
        return this.scale;
    }

    @Override
    public void setScale(Vector3f s)
    {
        this.scale.set(s);
        updateGlobalMatrices();
    }
}
