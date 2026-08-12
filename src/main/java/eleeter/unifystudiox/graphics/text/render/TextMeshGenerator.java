package eleeter.unifystudiox.graphics.text.render;

import eleeter.unifystudiox.graphics.text.render.font.Font;
import eleeter.unifystudiox.graphics.text.render.shaping.TextLayout;

public class TextMeshGenerator
{
    public static MeshData generate(TextLayout layout, Font font)
    {
        eleeter.elfontlib.render.MeshData rawMesh = eleeter.elfontlib.render.TextMeshGenerator.generate(layout.getHandle(), font.getHandle());
        return rawMesh != null ? new MeshData(rawMesh) : null;
    }
}
