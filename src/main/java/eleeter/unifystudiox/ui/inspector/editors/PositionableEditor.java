package eleeter.unifystudiox.ui.inspector.editors;

import eleeter.unifystudiox.scene.entity.Positionable;
import eleeter.unifystudiox.ui.inspector.EntityEditor;
import eleeter.unifystudiox.ui.inspector.InspectorBuilder;

public class PositionableEditor implements EntityEditor<Positionable>
{
    @Override
    public void onInspect(Positionable entity, InspectorBuilder builder)
    {
        builder.addHeader("TRANSFORM");
        builder.addVector3("Position", entity::getLocalPosition, entity::setLocalPosition);
        builder.addRotation("Rotation", entity::getLocalRotation, entity::setLocalRotation);
        builder.addVector3("Scale", entity::getLocalScale, entity::setLocalScale);
    }
}
