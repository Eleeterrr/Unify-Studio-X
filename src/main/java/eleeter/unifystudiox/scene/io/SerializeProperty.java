package eleeter.unifystudiox.scene.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be automatically serialized and deserialized by the JsonSceneSerializer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerializeProperty
{
    /**
     * Optional custom key name in JSON. If empty, the field name is used.
     */
    String value() default "";
}
