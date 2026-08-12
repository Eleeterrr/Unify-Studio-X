package eleeter.unifystudiox.cubic.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.*;

import eleeter.unifystudiox.cubic.CubeElement;

public class CubeRuntimeNode
{
    public final String id;
    public final String name;
    public final Vector3f pivot;
    
    public CubeRuntimeNode parent;
    public final List<CubeRuntimeNode> children = new ArrayList<>();
    
    public final Vector3f translation = new Vector3f(0, 0, 0);
    public final Quaternionf rotation = new Quaternionf();
    public final Vector3f scale = new Vector3f(1, 1, 1);
    
    public final Matrix4f localMatrix = new Matrix4f();
    public final Matrix4f worldMatrix = new Matrix4f();
    
    public int boneIndex = -1;
    public boolean isManuallyControlled = false;

    public final List<CubeElement> elements = new ArrayList<>();

    public CubeRuntimeNode(String id, String name, Vector3f pivot)
    {
        this.id = id;
        this.name = name;
        this.pivot = new Vector3f(pivot);
    }

    public void updateMatrices()
    {

        this.localMatrix.identity().translate(this.translation).translate(this.pivot).rotate(this.rotation).scale(this.scale).translate(-this.pivot.x, -this.pivot.y, -this.pivot.z);

        if (this.parent != null)
        {
            this.parent.worldMatrix.mul(this.localMatrix, this.worldMatrix);
        }
        else
        {
            this.worldMatrix.set(this.localMatrix);
        }
        
        for (CubeRuntimeNode child : this.children)
        {
            child.updateMatrices();
        }
    }

    public CubeRuntimeNode copy()
    {
        CubeRuntimeNode newNode = new CubeRuntimeNode(this.id, this.name, this.pivot);
        newNode.boneIndex = this.boneIndex;
        newNode.isManuallyControlled = this.isManuallyControlled;
        newNode.elements.addAll(this.elements);
        newNode.translation.set(this.translation);
        newNode.rotation.set(this.rotation);
        newNode.scale.set(this.scale);
        
        for (CubeRuntimeNode child : this.children)
        {
            CubeRuntimeNode childCopy = child.copy();
            childCopy.parent = newNode;
            newNode.children.add(childCopy);
        }
        return newNode;
    }
}
