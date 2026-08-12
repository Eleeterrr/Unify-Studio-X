package eleeter.unifystudiox.editor.animation;

import java.util.ArrayList;
import java.util.List;

import eleeter.unifystudiox.amb.AmbBone;
import eleeter.unifystudiox.amb.AmbModelInstance;
import eleeter.unifystudiox.cubic.CubicModelInstance;
import eleeter.unifystudiox.cubic.render.CubeRuntimeNode;
import eleeter.unifystudiox.scene.entity.SceneEntity;


public  class ModelHierarchyBuilder
{
    private ModelHierarchyBuilder()
    {
    }


    public static ModelHierarchyNode build(SceneEntity entity)
    {
        ModelHierarchyNode modelRoot = new ModelHierarchyNode(entity.getId(), entity.getId(), -1);

        if (entity instanceof CubicModelInstance cubicInstance)
        {
            CubeRuntimeNode cubicRoot = cubicInstance.getModel().root;
            if (cubicRoot != null)
            {
                modelRoot.addChild(buildCubicSubtree(cubicRoot));
            }
        } else if (entity instanceof AmbModelInstance ambInstance)
        {
            List<AmbBone> bones = ambInstance.getSkeleton().bones;
            if (bones != null && !bones.isEmpty())
            {
                List<ModelHierarchyNode> boneNodes = new ArrayList<>(bones.size());
                for (int i = 0; i < bones.size(); i++)
                {
                    AmbBone bone = bones.get(i);
                    boneNodes.add(new ModelHierarchyNode(bone.name, bone.name, i));
                }

                for (int i = 0; i < bones.size(); i++)
                {
                    AmbBone bone = bones.get(i);
                    ModelHierarchyNode currentNode = boneNodes.get(i);
                    if (bone.parentIndex == -1)
                    {
                        modelRoot.addChild(currentNode);
                    } else if (bone.parentIndex >= 0 && bone.parentIndex < bones.size())
                    {
                        ModelHierarchyNode parentNode = boneNodes.get(bone.parentIndex);
                        parentNode.addChild(currentNode);
                    }
                }
            }
        }

        return modelRoot;
    }

    private static ModelHierarchyNode buildCubicSubtree(CubeRuntimeNode cubeNode)
    {
        ModelHierarchyNode node = new ModelHierarchyNode(cubeNode.id, cubeNode.name, cubeNode.boneIndex);
        for (CubeRuntimeNode child : cubeNode.children)
        {
            node.addChild(buildCubicSubtree(child));
        }
        return node;
    }
}
