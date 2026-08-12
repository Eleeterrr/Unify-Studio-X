package eleeter.unifystudiox.obj;

import java.util.HashMap;
import java.util.Map;

import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import eleeter.unifystudiox.gizmo.Ray;
import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.scene.SelectionResult;
import eleeter.unifystudiox.scene.entity.Pickable;
import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.io.SerializeProperty;

public class ObjModelInstance implements SceneEntity, Positionable, Pickable
{
    private final String id;
    private final ObjModel model;
    private final Matrix4f modelMatrix;
    private final Map<ObjMesh, TextureGL> meshTextures;

    @SerializeProperty
    private final Vector3f position;
    @SerializeProperty
    private final Vector3f rotation;
    @SerializeProperty
    private final Vector3f scale;

    private TextureGL baseTexture;
    private boolean dirty;

    public ObjModelInstance(String id, ObjModel model)
    {
        this.id = id;
        this.model = model;
        this.modelMatrix = new Matrix4f();
        this.meshTextures = new HashMap<>();

        this.position = new Vector3f(0F, 0F, 0F);
        this.rotation = new Vector3f(0F, 0F, 0F);
        this.scale = new Vector3f(1F, 1F, 1F);

        for (ObjMesh mesh : model.getMeshes())
        {
            ObjMaterial material = mesh.getMaterial();
            if (material != null && material.getDiffuseTexturePath() != null)
            {
                String texPath = material.getDiffuseTexturePath().replace("\\", "/");
                this.meshTextures.put(mesh, TextureGL.loadCached(texPath));
            }
        }

        this.dirty = true;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public Matrix4f getModelMatrix()
    {
        if (this.dirty)
        {
            this.modelMatrix.identity();
            this.modelMatrix.translate(this.position);
            this.modelMatrix.rotateX(this.rotation.x);
            this.modelMatrix.rotateY(this.rotation.y);
            this.modelMatrix.rotateZ(this.rotation.z);
            this.modelMatrix.scale(this.scale);

            this.dirty = false;
        }

        return this.modelMatrix;
    }

    @Override
    public void update(double deltaTime)
    {
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public void cleanup()
    {
        for (TextureGL texture : this.meshTextures.values())
        {
            texture.cleanup();
        }

        this.meshTextures.clear();
    }

    public ObjModel getModel()
    {
        return this.model;
    }

    @Override
    public Vector3f getPosition()
    {
        return this.position;
    }

    @Override
    public void setPosition(Vector3f position)
    {
        this.position.set(position);
        this.dirty = true;
    }

    @Override
    public Quaternionf getRotation()
    {
        return new Quaternionf().rotationXYZ(this.rotation.x, this.rotation.y, this.rotation.z);
    }

    @Override
    public void setRotation(Quaternionf rotation)
    {
        rotation.getEulerAnglesXYZ(this.rotation);
        this.dirty = true;
    }

    public void setRotation(Vector3f rotation)
    {
        this.rotation.set(rotation);
        this.dirty = true;
    }

    @Override
    public Vector3f getScale()
    {
        return this.scale;
    }

    @Override
    public void setScale(Vector3f scale)
    {
        this.scale.set(scale);
        this.dirty = true;
    }

    public void setTexture(ObjMesh mesh, TextureGL texture)
    {
        this.meshTextures.put(mesh, texture);
    }

    public TextureGL getTexture(ObjMesh mesh)
    {
        TextureGL tex = this.meshTextures.get(mesh);
        return (tex != null) ? tex : this.baseTexture;
    }

    @Override
    public void setTexture(TextureGL texture)
    {
        this.baseTexture = texture;
        for (ObjMesh mesh : this.model.getMeshes())
        {
            this.meshTextures.put(mesh, texture);
        }
    }

    @Override
    public TextureGL getTexture()
    {
        return this.baseTexture;
    }

    @Override
    public String getAssetPath()
    {
        return this.model.getSourcePath();
    }

    @Override
    public SelectionResult pick(Ray ray)
    {
        Matrix4f invModel = new Matrix4f(this.getModelMatrix()).invert();

        Vector3f localOrigin = new Vector3f();
        invModel.transformPosition(ray.origin(), localOrigin);

        Vector3f localDir = new Vector3f();
        invModel.transformDirection(ray.direction(), localDir).normalize();

        float closestT = Float.MAX_VALUE;
        boolean hit = false;

        Vector2f aabbResult = new Vector2f();

        for (ObjMesh mesh : this.model.getMeshes())
        {
            if (Intersectionf.intersectRayAab(localOrigin, localDir, mesh.getAabbMin(), mesh.getAabbMax(),
                    aabbResult))
            {
                float[] v = mesh.getVertexData();
                int[] idx = mesh.getIndices();
                int stride = 8;

                Vector3f v0 = new Vector3f();
                Vector3f v1 = new Vector3f();
                Vector3f v2 = new Vector3f();

                for (int i = 0; i < idx.length; i += 3)
                {
                    int i0 = idx[i] * stride;
                    int i1 = idx[i + 1] * stride;
                    int i2 = idx[i + 2] * stride;

                    v0.set(v[i0], v[i0 + 1], v[i0 + 2]);
                    v1.set(v[i1], v[i1 + 1], v[i1 + 2]);
                    v2.set(v[i2], v[i2 + 1], v[i2 + 2]);

                    float t = Intersectionf.intersectRayTriangle(localOrigin, localDir, v0, v1, v2, 1e-5f);
                    if (t >= 0.0f && t < closestT)
                    {
                        closestT = t;
                        hit = true;
                    }
                }
            }
        }

        if (hit)
        {
            Vector3f localHitPoint = new Vector3f(localDir).mul(closestT).add(localOrigin);
            Vector3f worldHitPoint = new Vector3f();
            this.getModelMatrix().transformPosition(localHitPoint, worldHitPoint);
            float worldT = worldHitPoint.distance(ray.origin());
            return new SelectionResult(this, worldT, -1, null);
        }

        return SelectionResult.empty();
    }
}
