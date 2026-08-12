package eleeter.unifystudiox.cubic;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.cubic.render.CubeRuntimeModel;
import eleeter.unifystudiox.cubic.render.CubeRuntimeNode;

public class CubicLoader
{
    public static CubeRuntimeModel load(String path)
    {
        CubicParser parser = new CubicParser();
        CubicModel model = parser.parse(path);

        Map<CubicGroup, CubeRuntimeNode> nodeMap = new HashMap<>();
        CubeRuntimeNode runtimeRoot = convertNode(model.root, nodeMap);

        CubeRuntimeModel runtimeModel = new CubeRuntimeModel(model.sourceName, runtimeRoot);
        runtimeModel.animations.addAll(model.animations);

        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        collectVertices(model.root, nodeMap, vertices, indices, model.textureWidth, model.textureHeight);

        float[] vData = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) vData[i] = vertices.get(i);

        int[] iData = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) iData[i] = indices.get(i);

        runtimeModel.vertexData = vData;
        runtimeModel.indexData = iData;

        return runtimeModel;
    }

    private static CubeRuntimeNode convertNode(CubicGroup group, Map<CubicGroup, CubeRuntimeNode> nodeMap)
    {
        CubeRuntimeNode node = new CubeRuntimeNode(group.id, group.name, group.pivot);
        node.boneIndex = group.boneIndex;
        node.rotation.identity().rotateXYZ((float) Math.toRadians(group.rotation.x), (float) Math.toRadians(group.rotation.y), (float) Math.toRadians(group.rotation.z)
        );
        node.elements.addAll(group.elements);
        nodeMap.put(group, node);

        for (CubicGroup child : group.children)
        {
            CubeRuntimeNode runtimeChild = convertNode(child, nodeMap);
            runtimeChild.parent = node;
            node.children.add(runtimeChild);
        }
        return node;
    }

    private static void collectVertices(CubicGroup group, Map<CubicGroup, CubeRuntimeNode> nodeMap, List<Float> vertices, List<Integer> indices, int tw, int th)
    {
        CubeRuntimeNode node = nodeMap.get(group);
        int boneIndex = node != null ? node.boneIndex : 0;

        for (CubeElement element : group.elements)
        {
            if (element instanceof CubicElement cube)
            {
                addCube(cube, boneIndex, vertices, indices, tw, th);
            }
        }

        for (CubicGroup child : group.children)
        {
            collectVertices(child, nodeMap, vertices, indices, tw, th);
        }
    }

    private static void addCube(CubicElement cube, int boneIndex, List<Float> vertices, List<Integer> indices, int tw, int th)
    {
        int vBase = vertices.size() / 16;
        float x1 = cube.from.x;
        float y1 = cube.from.y;
        float z1 = cube.from.z;
        float x2 = cube.to.x;
        float y2 = cube.to.y;
        float z2 = cube.to.z;

        Matrix4f cubeTransform = new Matrix4f().translate(cube.origin).rotateXYZ((float) Math.toRadians(cube.rotation.x), (float) Math.toRadians(cube.rotation.y), (float) Math.toRadians(cube.rotation.z)).translate(-cube.origin.x, -cube.origin.y, -cube.origin.z);

        /* North (-Z) */
        addFace(vertices, indices, vBase, boneIndex, x2, y2, z1, x1, y2, z1, x1, y1, z1, x2, y1, z1, 0, 0, -1, cube.faces.get("north"), tw, th, cubeTransform);

        /* South (+Z) */
        addFace(vertices, indices, vBase + 4, boneIndex, x1, y2, z2, x2, y2, z2, x2, y1, z2, x1, y1, z2, 0, 0, 1, cube.faces.get("south"), tw, th, cubeTransform);

        /* East (+X) */
        addFace(vertices, indices, vBase + 8, boneIndex, x2, y2, z2, x2, y2, z1, x2, y1, z1, x2, y1, z2, 1, 0, 0, cube.faces.get("east"), tw, th, cubeTransform);

        /* West (-X) */
        addFace(vertices, indices, vBase + 12, boneIndex, x1, y2, z1, x1, y2, z2, x1, y1, z2, x1, y1, z1, -1, 0, 0, cube.faces.get("west"), tw, th, cubeTransform);

        /* Up (+Y) */
        addFace(vertices, indices, vBase + 16, boneIndex, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, 0, 1, 0, cube.faces.get("up"), tw, th, cubeTransform);

        /* Down (-Y) */
        addFace(vertices, indices, vBase + 20, boneIndex, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, 0, -1, 0, cube.faces.get("down"), tw, th, cubeTransform);
    }

    private static void addFace(List<Float> vertices, List<Integer> indices, int vBase, int boneIndex, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float nx, float ny, float nz, CubicFace face, int tw, int th, Matrix4f cubeTransform)
    {
        Vector3f v1 = new Vector3f(x1, y1, z1);
        Vector3f v2 = new Vector3f(x2, y2, z2);
        Vector3f v3 = new Vector3f(x3, y3, z3);
        Vector3f v4 = new Vector3f(x4, y4, z4);
        cubeTransform.transformPosition(v1);
        cubeTransform.transformPosition(v2);
        cubeTransform.transformPosition(v3);
        cubeTransform.transformPosition(v4);

        Vector3f norm = new Vector3f(nx, ny, nz);
        cubeTransform.transformDirection(norm);

        float u1 = 0, u1v = 0, u2 = 0, u2v = 0;
        if (face != null)
        {
            u1 = face.uv.x / tw;
            u1v = 1.0f - (face.uv.y / th);
            u2 = face.uv.z / tw;
            u2v = 1.0f - (face.uv.w / th);
        }

        /* V1 */
        addVertex(vertices, v1.x, v1.y, v1.z, u1, u1v, norm.x, norm.y, norm.z, boneIndex);
        /* V2 */
        addVertex(vertices, v2.x, v2.y, v2.z, u2, u1v, norm.x, norm.y, norm.z, boneIndex);
        /* V3 */
        addVertex(vertices, v3.x, v3.y, v3.z, u2, u2v, norm.x, norm.y, norm.z, boneIndex);
        /* V4 */
        addVertex(vertices, v4.x, v4.y, v4.z, u1, u2v, norm.x, norm.y, norm.z, boneIndex);

        indices.add(vBase);
        indices.add(vBase + 1);
        indices.add(vBase + 2);
        indices.add(vBase);
        indices.add(vBase + 2);
        indices.add(vBase + 3);
    }

    private static void addVertex(List<Float> v, float x, float y, float z, float u, float v_coord, float nx, float ny, float nz, int bone)
    {
        v.add(x);
        v.add(y);
        v.add(z); /* Pos */
        v.add(u);
        v.add(v_coord); /* UV */
        v.add(nx);
        v.add(ny);
        v.add(nz); /* Normal */
        v.add(1.0f);
        v.add(0.0f);
        v.add(0.0f);
        v.add(0.0f); /* Weights */
        v.add((float) bone);
        v.add(0.0f);
        v.add(0.0f);
        v.add(0.0f); /* Indices */
    }
}
