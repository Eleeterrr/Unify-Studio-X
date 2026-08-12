package eleeter.unifystudiox.editor.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ModelHierarchyNode
{
    private final String id;
    private final String name;
    private final int boneIndex;
    private final List<ModelHierarchyNode> children;
    private boolean isExpanded;


    public ModelHierarchyNode(String id, String name, int boneIndex)
    {
        this.id = id;
        this.name = name;
        this.boneIndex = boneIndex;
        this.children = new ArrayList<>();
        this.isExpanded = true;
    }


    public void addChild(ModelHierarchyNode child)
    {
        this.children.add(child);
    }

    public String getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public int getBoneIndex()
    {
        return this.boneIndex;
    }

    public List<ModelHierarchyNode> getChildren()
    {
        return Collections.unmodifiableList(this.children);
    }

    public boolean isExpanded()
    {
        return this.isExpanded;
    }

    public void setExpanded(boolean expanded)
    {
        this.isExpanded = expanded;
    }
}
