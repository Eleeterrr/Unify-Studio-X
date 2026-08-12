package eleeter.unifystudiox.scene.entity;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.ecs.Entity;
import eleeter.unifystudiox.ecs.EntityWorld;
import eleeter.unifystudiox.ecs.components.IdentityComponent;
import eleeter.unifystudiox.ecs.components.TransformComponent;
import eleeter.unifystudiox.ecs.components.VisibilityComponent;
import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.scene.io.SerializeProperty;

public abstract class BaseSceneEntity implements SceneEntity, Positionable
{

    private TextureGL texture;
    @SerializeProperty
    private boolean visible = true;

    protected Entity ecsEntity;

    @SerializeProperty
    protected TransformComponent transform;

    private final Vector3f defaultPosition = new Vector3f();
    private final Quaternionf defaultRotation = new Quaternionf();
    private final Vector3f defaultScale = new Vector3f(1.0f, 1.0f, 1.0f);
    private final Matrix4f fallbackMatrix = new Matrix4f();

    @Override
    public void initEcs(EntityWorld world)
    {
        this.ecsEntity = world.createEntity();
        this.transform = new TransformComponent();

        this.transform.localPosition.set(this.defaultPosition);
        this.transform.localRotation.set(this.defaultRotation);
        this.transform.localScale.set(this.defaultScale);
        this.transform.isDirty = true;

        this.ecsEntity.add(this.transform);
        this.ecsEntity.add(new IdentityComponent(getId(), getAssetPath()));
        this.ecsEntity.add(new VisibilityComponent(this.visible));
    }

    @Override
    public String getAssetPath()
    {
        return null;
    }

    @Override
    public void setTexture(TextureGL texture)
    {
        this.texture = texture;
    }

    @Override
    public TextureGL getTexture()
    {
        return this.texture;
    }

    @Override
    public boolean isVisible()
    {
        return this.visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
        if (this.ecsEntity != null)
        {
            VisibilityComponent vc = this.ecsEntity.get(VisibilityComponent.class);
            if (vc != null)
            {
                vc.visible = visible;
            }
        }
    }

    @Override
    public void cleanup()
    {
        if (this.ecsEntity != null)
        {
            this.ecsEntity.destroy();
        }
    }

    @Override
    public Matrix4f getModelMatrix()
    {
        return this.transform != null ? this.transform.modelMatrix : this.fallbackMatrix.identity();
    }

    @Override
    public Vector3f getPosition()
    {
        return this.transform != null ? this.transform.localPosition : this.defaultPosition;
    }

    @Override
    public void setPosition(Vector3f position)
    {
        if (this.transform != null)
        {
            this.transform.localPosition.set(position);
            this.transform.isDirty = true;

        } else
        {
            this.defaultPosition.set(position);
        }
    }

    @Override
    public Quaternionf getRotation()
    {
        return this.transform != null ? this.transform.localRotation : this.defaultRotation;
    }

    @Override
    public void setRotation(Quaternionf rotation)
    {
        if (this.transform != null)
        {
            this.transform.localRotation.set(rotation);
            this.transform.isDirty = true;

        } else
        {
            this.defaultRotation.set(rotation);
        }
    }

    @Override
    public Vector3f getScale()
    {
        return this.transform != null ? this.transform.localScale : this.defaultScale;
    }

    @Override
    public void setScale(Vector3f scale)
    {
        if (this.transform != null)
        {
            this.transform.localScale.set(scale);
            this.transform.isDirty = true;

        } else
        {
            this.defaultScale.set(scale);
        }
    }
}
