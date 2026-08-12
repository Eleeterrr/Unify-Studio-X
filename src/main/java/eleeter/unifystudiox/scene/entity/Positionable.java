package eleeter.unifystudiox.scene.entity;

import org.joml.Quaternionf;
import org.joml.Vector3f;


public interface Positionable
{
    Vector3f getPosition();

    void setPosition(Vector3f position);

    Quaternionf getRotation();

    void setRotation(Quaternionf rotation);

    Vector3f getScale();

    void setScale(Vector3f scale);

    default Vector3f getLocalPosition()
    {
        return getPosition();
    }

    default void setLocalPosition(Vector3f position)
    {
        setPosition(position);
    }

    default Quaternionf getLocalRotation()
    {
        return getRotation();
    }

    default void setLocalRotation(Quaternionf rotation)
    {
        setRotation(rotation);
    }

    default Vector3f getLocalScale()
    {
        return getScale();
    }

    default void setLocalScale(Vector3f scale)
    {
        setScale(scale);
    }
}
