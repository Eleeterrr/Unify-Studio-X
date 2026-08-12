package eleeter.unifystudiox.gltf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.amb.AmbBone;
import eleeter.unifystudiox.amb.AmbMesh;
import eleeter.unifystudiox.amb.AmbModel;
import eleeter.unifystudiox.gltf.io.BufferAccessor;
import eleeter.unifystudiox.gltf.io.GlbReader;
import eleeter.unifystudiox.gltf.json.JsonParser;
import eleeter.unifystudiox.gltf.model.GltfAccessor;
import eleeter.unifystudiox.gltf.model.GltfBuffer;
import eleeter.unifystudiox.gltf.model.GltfBufferView;
import eleeter.unifystudiox.gltf.model.GltfMesh;
import eleeter.unifystudiox.gltf.model.GltfModel;
import eleeter.unifystudiox.gltf.model.GltfNode;
import eleeter.unifystudiox.gltf.model.GltfPrimitive;
import eleeter.unifystudiox.gltf.model.GltfSkin;

public class GltfLoader
{

    public static final boolean CONVERT_Z_UP_TO_Y_UP = false;

    /**
     * Loads a GLTF or GLB file directly into an AmbModel in memory.
     */
    public static AmbModel load(String pathStr)
    {
        GltfModel gltfModel = loadGltf(pathStr);
        return convertToAmbModel(gltfModel, pathStr);
    }

    private static AmbModel convertToAmbModel(GltfModel model, String path)
    {
        AmbModel ambModel = new AmbModel(path);

        List<GltfNode> meshNodes = new ArrayList<>();
        for (GltfNode node : model.nodes)
        {
            if (node.meshIndex >= 0 && node.meshIndex < model.meshes.size())
            {
                meshNodes.add(node);
            }
        }

        for (int mn = 0; mn < meshNodes.size(); mn++)
        {
            GltfNode node = meshNodes.get(mn);
            GltfMesh mesh = model.meshes.get(node.meshIndex);

            String meshName = node.name != null && !node.name.startsWith("node_") ? node.name : mesh.name;
            if (meshName == null) meshName = "mesh_" + mn;

            List<float[]> allVertices = new ArrayList<>();
            List<Integer> allIndices = new ArrayList<>();
            int vertexOffset = 0;

            for (GltfPrimitive prim : mesh.primitives)
            {
                if (prim.mode != 4) continue;

                Integer posAccIdx = prim.attributes.get("POSITION");
                if (posAccIdx == null) continue;

                float[] posData = BufferAccessor.readFloats(model, model.accessors.get(posAccIdx));
                int vertexCount = posData.length / 3;

                float[] normData = null;
                if (prim.attributes.containsKey("NORMAL"))
                {
                    normData = BufferAccessor.readFloats(model, model.accessors.get(prim.attributes.get("NORMAL")));
                }

                float[] uvData = null;
                if (prim.attributes.containsKey("TEXCOORD_0"))
                {
                    uvData = BufferAccessor.readFloats(model, model.accessors.get(prim.attributes.get("TEXCOORD_0")));
                }

                int[] jointsData = null;
                if (prim.attributes.containsKey("JOINTS_0"))
                {
                    jointsData = BufferAccessor.readInts(model, model.accessors.get(prim.attributes.get("JOINTS_0")));
                }

                float[] weightsData = null;
                if (prim.attributes.containsKey("WEIGHTS_0"))
                {
                    weightsData = BufferAccessor.readFloats(model, model.accessors.get(prim.attributes.get("WEIGHTS_0")));
                }

                int[] primIndices = null;
                if (prim.indicesAccessorIndex >= 0)
                {
                    primIndices = BufferAccessor.readInts(model, model.accessors.get(prim.indicesAccessorIndex));
                } else
                {
                    primIndices = new int[vertexCount];
                    for (int i = 0; i < vertexCount; i++) primIndices[i] = i;
                }

                if (normData == null)
                {
                    normData = new float[vertexCount * 3];
                    for (int i = 0; i < primIndices.length; i += 3)
                    {
                        int i1 = primIndices[i];
                        int i2 = primIndices[i + 1];
                        int i3 = primIndices[i + 2];

                        Vector3f v1 = new Vector3f(posData[i1 * 3], posData[i1 * 3 + 1], posData[i1 * 3 + 2]);
                        Vector3f v2 = new Vector3f(posData[i2 * 3], posData[i2 * 3 + 1], posData[i2 * 3 + 2]);
                        Vector3f v3 = new Vector3f(posData[i3 * 3], posData[i3 * 3 + 1], posData[i3 * 3 + 2]);

                        Vector3f edge1 = new Vector3f(v2).sub(v1);
                        Vector3f edge2 = new Vector3f(v3).sub(v1);
                        Vector3f normal = new Vector3f();
                        edge1.cross(edge2, normal).normalize();

                        for (int idx : new int[]{i1, i2, i3})
                        {
                            normData[idx * 3] = normal.x;
                            normData[idx * 3 + 1] = normal.y;
                            normData[idx * 3 + 2] = normal.z;
                        }
                    }
                }

                for (int v = 0; v < vertexCount; v++)
                {
                    float[] vert = new float[16];

                    vert[0] = posData[v * 3];
                    vert[1] = posData[v * 3 + 1];
                    vert[2] = posData[v * 3 + 2];

                    vert[3] = normData[v * 3];
                    vert[4] = normData[v * 3 + 1];
                    vert[5] = normData[v * 3 + 2];

                    if (uvData != null)
                    {
                        vert[6] = uvData[v * 2];
                        vert[7] = uvData[v * 2 + 1];
                    }

                    if (jointsData != null)
                    {
                        vert[8] = jointsData[v * 4];
                        vert[9] = jointsData[v * 4 + 1];
                        vert[10] = jointsData[v * 4 + 2];
                        vert[11] = jointsData[v * 4 + 3];
                    }

                    if (weightsData != null)
                    {
                        float w0 = weightsData[v * 4];
                        float w1 = weightsData[v * 4 + 1];
                        float w2 = weightsData[v * 4 + 2];
                        float w3 = weightsData[v * 4 + 3];
                        float sum = w0 + w1 + w2 + w3;
                        if (sum > 0.000001f)
                        {
                            vert[12] = w0 / sum;
                            vert[13] = w1 / sum;
                            vert[14] = w2 / sum;
                            vert[15] = w3 / sum;
                        } else
                        {
                            vert[12] = 1.0f;
                        }
                    } else
                    {
                        vert[12] = 1.0f;
                    }

                    if (CONVERT_Z_UP_TO_Y_UP)
                    {
                        float tempY = vert[1];
                        vert[1] = vert[2];
                        vert[2] = -tempY;
                        tempY = vert[4];
                        vert[4] = vert[5];
                        vert[5] = -tempY;
                    }

                    allVertices.add(vert);
                }

                for (int idx : primIndices)
                {
                    allIndices.add(idx + vertexOffset);
                }
                vertexOffset += vertexCount;
            }

            float[] finalVerts = new float[allVertices.size() * 16];
            for (int i = 0; i < allVertices.size(); i++)
            {
                System.arraycopy(allVertices.get(i), 0, finalVerts, i * 16, 16);
            }
            int[] finalInds = new int[allIndices.size()];
            for (int i = 0; i < allIndices.size(); i++)
            {
                finalInds[i] = allIndices.get(i);
            }

            ambModel.meshes.add(new AmbMesh(meshName, finalVerts, finalInds));
        }

        int skinCount = model.skins.size();
        if (skinCount == 0 && !meshNodes.isEmpty())
        {
            ambModel.skeleton.bones.add(new AmbBone("root", -1, new Matrix4f().identity()));
        } else if (skinCount > 0)
        {
            GltfSkin skin = model.skins.get(0);

            float[] ibmFloats = null;
            if (skin.inverseBindMatricesAccessorIndex >= 0)
            {
                ibmFloats = BufferAccessor.readFloats(model, model.accessors.get(skin.inverseBindMatricesAccessorIndex));
            }

            for (int j = 0; j < skin.joints.size(); j++)
            {
                int jointNodeIndex = skin.joints.get(j);
                GltfNode jointNode = model.nodes.get(jointNodeIndex);

                int parentJointIndex = -1;
                for (int pj = 0; pj < skin.joints.size(); pj++)
                {
                    int pNodeIdx = skin.joints.get(pj);
                    GltfNode pNode = model.nodes.get(pNodeIdx);
                    if (pNode.children.contains(jointNodeIndex))
                    {
                        parentJointIndex = pj;
                        break;
                    }
                }

                String bName = jointNode.name != null ? jointNode.name : "bone_" + j;

                Matrix4f globalBind = new Matrix4f();
                if (ibmFloats != null && ibmFloats.length >= (j + 1) * 16)
                {
                    int o = j * 16;
                    Matrix4f ibm = new Matrix4f(ibmFloats[o], ibmFloats[o + 1], ibmFloats[o + 2], ibmFloats[o + 3], ibmFloats[o + 4], ibmFloats[o + 5], ibmFloats[o + 6], ibmFloats[o + 7], ibmFloats[o + 8], ibmFloats[o + 9], ibmFloats[o + 10], ibmFloats[o + 11], ibmFloats[o + 12], ibmFloats[o + 13], ibmFloats[o + 14], ibmFloats[o + 15]
                    );
                    ibm.invert(globalBind);
                } else
                {
                    globalBind.identity();
                }

                if (CONVERT_Z_UP_TO_Y_UP)
                {
                    Matrix4f fix = new Matrix4f().rotateX((float) -Math.PI / 2.0f);
                    globalBind.mulLocal(fix);
                }

                ambModel.skeleton.bones.add(new AmbBone(bName, parentJointIndex, globalBind));
            }
        }

        if (!ambModel.skeleton.bones.isEmpty())
        {
            ambModel.skeleton.calculateInverseBindMatrices();
            ambModel.skeleton.convertGlobalToLocalBind();
            ambModel.skeleton.calculateGlobalTransforms();
        }

        return ambModel;
    }

    @SuppressWarnings("unchecked")
    private static GltfModel loadGltf(String pathStr)
    {
        Path path = Paths.get(pathStr);
        String jsonString;
        byte[] glbBinChunk = null;

        if (pathStr.toLowerCase().endsWith(".glb"))
        {
            GlbReader glb = GlbReader.read(pathStr);
            if (glb == null) throw new RuntimeException("[GltfToAmb] Invalid GLB: " + pathStr);
            jsonString = glb.jsonChunk;
            glbBinChunk = glb.binChunk;
        } else
        {
            try
            {
                jsonString = new String(Files.readAllBytes(path), "UTF-8");
            } catch (IOException e)
            {
                throw new RuntimeException("[GltfToAmb] Failed to read GLTF: " + pathStr, e);
            }
        }

        Map<String, Object> root = JsonParser.parse(jsonString);
        GltfModel model = new GltfModel();

        List<Map<String, Object>> buffersArray = getList(root, "buffers");
        if (buffersArray != null)
        {
            for (int i = 0; i < buffersArray.size(); i++)
            {
                Map<String, Object> bMap = buffersArray.get(i);
                GltfBuffer buffer = new GltfBuffer();
                buffer.index = i;
                buffer.byteLength = getInt(bMap, "byteLength", 0);
                buffer.uri = getString(bMap, "uri", null);

                if (buffer.uri == null && i == 0 && glbBinChunk != null)
                {
                    buffer.data = glbBinChunk;
                } else if (buffer.uri != null)
                {
                    if (buffer.uri.startsWith("data:"))
                    {
                        int comma = buffer.uri.indexOf(',');
                        if (comma > 0)
                        {
                            String b64 = buffer.uri.substring(comma + 1);
                            buffer.data = Base64.getDecoder().decode(b64);
                        }
                    } else
                    {
                        Path binPath = path.getParent().resolve(buffer.uri);
                        try
                        {
                            buffer.data = Files.readAllBytes(binPath);
                        } catch (IOException e)
                        {
                            throw new RuntimeException("[GltfToAmb] Failed to read bin file: " + binPath, e);
                        }
                    }
                }
                model.buffers.add(buffer);
            }
        }

        List<Map<String, Object>> viewsArray = getList(root, "bufferViews");
        if (viewsArray != null)
        {
            for (int i = 0; i < viewsArray.size(); i++)
            {
                Map<String, Object> vMap = viewsArray.get(i);
                GltfBufferView view = new GltfBufferView();
                view.index = i;
                view.bufferIndex = getInt(vMap, "buffer", -1);
                view.byteOffset = getInt(vMap, "byteOffset", 0);
                view.byteLength = getInt(vMap, "byteLength", 0);
                view.byteStride = getInt(vMap, "byteStride", 0);
                model.bufferViews.add(view);
            }
        }

        List<Map<String, Object>> accessorsArray = getList(root, "accessors");
        if (accessorsArray != null)
        {
            for (int i = 0; i < accessorsArray.size(); i++)
            {
                Map<String, Object> aMap = accessorsArray.get(i);
                GltfAccessor acc = new GltfAccessor();
                acc.index = i;
                acc.bufferViewIndex = getInt(aMap, "bufferView", -1);
                acc.byteOffset = getInt(aMap, "byteOffset", 0);
                acc.componentType = getInt(aMap, "componentType", 0);
                acc.count = getInt(aMap, "count", 0);
                acc.type = getString(aMap, "type", "");
                model.accessors.add(acc);
            }
        }

        List<Map<String, Object>> meshesArray = getList(root, "meshes");
        if (meshesArray != null)
        {
            for (int i = 0; i < meshesArray.size(); i++)
            {
                Map<String, Object> mMap = meshesArray.get(i);
                GltfMesh mesh = new GltfMesh();
                mesh.index = i;
                mesh.name = getString(mMap, "name", "mesh_" + i);

                List<Map<String, Object>> primsArray = getList(mMap, "primitives");
                if (primsArray != null)
                {
                    for (Map<String, Object> pMap : primsArray)
                    {
                        GltfPrimitive prim = new GltfPrimitive();
                        prim.indicesAccessorIndex = getInt(pMap, "indices", -1);
                        prim.materialIndex = getInt(pMap, "material", -1);
                        prim.mode = getInt(pMap, "mode", 4);

                        Map<String, Object> attrMap = (Map<String, Object>) pMap.get("attributes");
                        if (attrMap != null)
                        {
                            for (Map.Entry<String, Object> entry : attrMap.entrySet())
                            {
                                prim.attributes.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                            }
                        }
                        mesh.primitives.add(prim);
                    }
                }
                model.meshes.add(mesh);
            }
        }

        List<Map<String, Object>> nodesArray = getList(root, "nodes");
        if (nodesArray != null)
        {
            for (int i = 0; i < nodesArray.size(); i++)
            {
                Map<String, Object> nMap = nodesArray.get(i);
                GltfNode node = new GltfNode();
                node.index = i;
                node.name = getString(nMap, "name", "node_" + i);
                node.meshIndex = getInt(nMap, "mesh", -1);
                node.skinIndex = getInt(nMap, "skin", -1);

                List<Object> matArray = getList(nMap, "matrix");
                if (matArray != null)
                {
                    node.matrix = new float[16];
                    for (int m = 0; m < 16; m++) node.matrix[m] = ((Number) matArray.get(m)).floatValue();
                }

                List<Object> transArray = getList(nMap, "translation");
                if (transArray != null)
                {
                    node.translation = new float[3];
                    for (int m = 0; m < 3; m++) node.translation[m] = ((Number) transArray.get(m)).floatValue();
                }

                List<Object> rotArray = getList(nMap, "rotation");
                if (rotArray != null)
                {
                    node.rotation = new float[4];
                    for (int m = 0; m < 4; m++) node.rotation[m] = ((Number) rotArray.get(m)).floatValue();
                }

                List<Object> scaleArray = getList(nMap, "scale");
                if (scaleArray != null)
                {
                    node.scale = new float[3];
                    for (int m = 0; m < 3; m++) node.scale[m] = ((Number) scaleArray.get(m)).floatValue();
                }

                List<Object> childArray = getList(nMap, "children");
                if (childArray != null)
                {
                    for (Object childObj : childArray)
                    {
                        node.children.add(((Number) childObj).intValue());
                    }
                }

                model.nodes.add(node);
            }
        }

        List<Map<String, Object>> skinsArray = getList(root, "skins");
        if (skinsArray != null)
        {
            for (int i = 0; i < skinsArray.size(); i++)
            {
                Map<String, Object> sMap = skinsArray.get(i);
                GltfSkin skin = new GltfSkin();
                skin.index = i;
                skin.name = getString(sMap, "name", "skin_" + i);
                skin.skeletonNodeIndex = getInt(sMap, "skeleton", -1);
                skin.inverseBindMatricesAccessorIndex = getInt(sMap, "inverseBindMatrices", -1);

                List<Object> jointsArray = getList(sMap, "joints");
                if (jointsArray != null)
                {
                    for (Object jObj : jointsArray)
                    {
                        skin.joints.add(((Number) jObj).intValue());
                    }
                }
                model.skins.add(skin);
            }
        }

        Map<String, Object> sceneObj = null;
        int sceneIdx = getInt(root, "scene", -1);
        List<Map<String, Object>> scenesArray = getList(root, "scenes");
        if (scenesArray != null && sceneIdx >= 0 && sceneIdx < scenesArray.size())
        {
            sceneObj = scenesArray.get(sceneIdx);
        } else if (scenesArray != null && !scenesArray.isEmpty())
        {
            sceneObj = scenesArray.get(0);
        }

        if (sceneObj != null)
        {
            List<Object> sNodes = getList(sceneObj, "nodes");
            if (sNodes != null)
            {
                for (Object nObj : sNodes)
                {
                    model.sceneRoots.add(((Number) nObj).intValue());
                }
            }
        }

        return model;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getList(Map<String, Object> map, String key)
    {
        return (T) map.get(key);
    }

    private static int getInt(Map<String, Object> map, String key, int def)
    {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        return def;
    }

    private static String getString(Map<String, Object> map, String key, String def)
    {
        Object v = map.get(key);
        if (v instanceof String) return (String) v;
        return def;
    }
}
