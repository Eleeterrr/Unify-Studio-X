package eleeter.unifystudiox.scene.entity;

import org.joml.Vector3f;

public class BaseplateEntity extends BaseSceneEntity
{
    private final String id;
    private float width, depth, thickness;

    private final Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

    public BaseplateEntity(String id, float width, float depth, float height)
    {
        this.id = id;
        this.width = width;
        this.depth = depth;
        this.thickness = height;
        setScale(new Vector3f(width, height, depth));
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public void update(double deltaTime)
    {
    }

    @Override
    public void cleanup()
    {
        if (this.getTexture() != null)
        {
            this.getTexture().cleanup();
        }
    }


    public float getWidth()
    {
        return this.width;
    }

    public float getDepth()
    {
        return this.depth;
    }

    public Vector3f getColor()
    {
        return this.color;
    }

    public void setSize(float w, float d)
    {
        this.width = w;
        this.depth = d;
        setScale(new Vector3f(this.width, this.thickness, this.depth));
    }

    public void setColor(float r, float g, float b)
    {
        this.color.set(r, g, b);
    }
}
