package eleeter.unifystudiox.animation.data;

import org.joml.Quaternionf;
import org.joml.Vector3f;


public class Transform
{
    private final Vector3f translation;
    private final Quaternionf rotation;
    private final Vector3f scale;


    public Transform(Vector3f translation, Quaternionf rotation, Vector3f scale)
    {
        if (translation == null)
        {
            throw new IllegalArgumentException(
                    "Transform: translation must not be null.");
        }
        if (rotation == null)
        {
            throw new IllegalArgumentException(
                    "Transform: rotation must not be null.");
        }
        if (scale == null)
        {
            throw new IllegalArgumentException(
                    "Transform: scale must not be null.");
        }

        this.translation = new Vector3f(translation);
        this.rotation = new Quaternionf(rotation);
        this.scale = new Vector3f(scale);
    }


    public static Transform identity()
    {
        return new Transform(
                new Vector3f(0f, 0f, 0f),
                new Quaternionf().identity(),
                new Vector3f(1f, 1f, 1f)
        );
    }


    public Vector3f getTranslation()
    {
        return new Vector3f(this.translation);
    }


    public Quaternionf getRotation()
    {
        return new Quaternionf(this.rotation);
    }

    public Vector3f getScale()
    {
        return new Vector3f(this.scale);
    }


    @Override
    public String toString()
    {
        return "Transform{"
                + "translation=" + this.translation
                + ", rotation=" + this.rotation
                + ", scale=" + this.scale
                + '}';
    }
}
