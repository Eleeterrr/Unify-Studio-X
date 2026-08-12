package eleeter.unifystudiox.cubic.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eleeter.unifystudiox.cubic.CubicAnimation;

public class CubeRuntimeModel
{
    public final String name;
    public final CubeRuntimeNode root;
    public final Map<String, CubeRuntimeNode> nodesById = new HashMap<>();
    public final Map<String, CubeRuntimeNode> nodesByName = new HashMap<>();
    public final List<CubeRuntimeNode> flattenedNodes = new ArrayList<>();

    public final List<CubicAnimation> animations = new ArrayList<>();
    public float[] vertexData;
    public int[] indexData;
    public CubeGLMesh mesh;

    public CubeRuntimeModel(String name, CubeRuntimeNode root)
    {
        this.name = name;
        this.root = root;
        indexHierarchy(root);
    }

    private void indexHierarchy(CubeRuntimeNode node)
    {
        this.nodesById.put(node.id, node);
        this.nodesByName.put(node.name, node);
        
        while (this.flattenedNodes.size() <= node.boneIndex)
        {
            this.flattenedNodes.add(null);
        }
        this.flattenedNodes.set(node.boneIndex, node);
        
        for (CubeRuntimeNode child : node.children)
        {
            indexHierarchy(child);
        }
    }

    public void update()
    {
        this.root.updateMatrices();
    }
    
    public void cleanup()
    {
        if (this.mesh != null)
        {
            this.mesh.cleanup();
        }
    }
}
