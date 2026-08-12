package eleeter.unifystudiox.scene.entity;

import eleeter.unifystudiox.gizmo.Ray;
import eleeter.unifystudiox.scene.SelectionResult;


public interface Pickable
{
    SelectionResult pick(Ray ray);
}
