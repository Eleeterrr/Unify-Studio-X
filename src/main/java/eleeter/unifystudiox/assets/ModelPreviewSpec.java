package eleeter.unifystudiox.assets;

import java.util.function.Supplier;

import eleeter.unifystudiox.scene.entity.SceneEntity;

public class ModelPreviewSpec
{
    private final Supplier<SceneEntity> previewFactory;
    private final Bounds bounds;
    private final float baseRenderScale;

    public ModelPreviewSpec(Supplier<SceneEntity> previewFactory, Bounds bounds, float baseRenderScale)
    {
        this.previewFactory = previewFactory;
        this.bounds = bounds;
        this.baseRenderScale = baseRenderScale;
    }

    public SceneEntity createPreviewEntity()
    {
        return this.previewFactory.get();
    }

    public Bounds getBounds()
    {
        return this.bounds;
    }

    public float getBaseRenderScale()
    {
        return this.baseRenderScale;
    }

    public static final class Bounds
    {
        private final float centerX;
        private final float centerY;
        private final float centerZ;
        private final float sizeX;
        private final float sizeY;
        private final float sizeZ;

        public Bounds(float centerX, float centerY, float centerZ, float sizeX, float sizeY, float sizeZ)
        {
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        public float getCenterX()
        {
            return this.centerX;
        }

        public float getCenterY()
        {
            return this.centerY;
        }

        public float getCenterZ()
        {
            return this.centerZ;
        }

        public float getSizeX()
        {
            return this.sizeX;
        }

        public float getSizeY()
        {
            return this.sizeY;
        }

        public float getSizeZ()
        {
            return this.sizeZ;
        }

        public boolean isValid()
        {
            return this.sizeX > 0.0f || this.sizeY > 0.0f || this.sizeZ > 0.0f;
        }
    }
}
