package eleeter.unifystudiox.animation.api;

import java.util.List;


public interface AnimatableObject
{
    String getObjectId();

    Object getProperty(String propertyName);

    void setProperty(String propertyName, Object value);

    List<String> getSupportedProperties();
}
