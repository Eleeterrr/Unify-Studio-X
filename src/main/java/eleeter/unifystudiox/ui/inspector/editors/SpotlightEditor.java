package eleeter.unifystudiox.ui.inspector.editors;

import eleeter.unifystudiox.scene.entity.SpotlightEntity;
import eleeter.unifystudiox.ui.inspector.EntityEditor;
import eleeter.unifystudiox.ui.inspector.InspectorBuilder;

public class SpotlightEditor implements EntityEditor<SpotlightEntity>
{
    @Override
    public void onInspect(SpotlightEntity light, InspectorBuilder builder)
    {
        builder.addHeader("SPOTLIGHT");
        builder.addFloat("Intensity", light::getIntensity, v -> light.getData().setIntensity(v));
        builder.addFloat("Range",     light::getRange,     v -> light.getData().setRange(v));
        builder.addColor("Color (RGB)", light::getColor,   v -> light.getData().setColor(v.x, v.y, v.z));
    }
}
