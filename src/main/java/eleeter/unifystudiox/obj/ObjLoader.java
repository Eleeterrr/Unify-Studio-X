package eleeter.unifystudiox.obj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joml.Vector3f;

import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.util.log.AniLogger;

public class ObjLoader
{
    public static void loadAndAddToScene(String path, Scene scene)
    {
        try
        {
            ObjModel model = load(path);
            String id = "obj_" + path.substring(path.lastIndexOf('/') + 1);
            ObjModelInstance instance = new ObjModelInstance(id, model);

            Optional<SceneEntity> existing = scene.findEntity(id);
            if (existing.isPresent() && existing.get() instanceof ObjModelInstance oldInst)
            {
                instance.setPosition(oldInst.getPosition());
                instance.setRotation(oldInst.getRotation());
                instance.setScale(oldInst.getScale());
            } else
            {
                instance.setPosition(new Vector3f(0.0F, 0.0F, 0.0F));
            }

            scene.addEntity(instance);
            AniLogger.info("ObjLoader", "Loaded OBJ into scene: " + path);
        } catch (Exception e)
        {
            AniLogger.warn("ObjLoader", "Failed to load OBJ '" + path + "': " + e.getMessage());
        }
    }

    public static ObjModel load(String path)
    {
        ObjModel model = new ObjModel(path);
        File objFile = new File(path);
        String parentDir = objFile.getParent();

        if (parentDir == null)
        {
            parentDir = "";
        } else
        {
            parentDir = parentDir + File.separator;
        }

        List<Float> positions = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Float> normals = new ArrayList<>();

        Map<String, ObjMaterial> materials = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(objFile)))
        {
            String line;
            String currentMeshName = "default";
            ObjMaterial currentMaterial = null;

            List<Integer> currentPositionIndices = new ArrayList<>();
            List<Integer> currentTexCoordIndices = new ArrayList<>();
            List<Integer> currentNormalIndices = new ArrayList<>();

            while ((line = reader.readLine()) != null)
            {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#"))
                {
                    continue;
                }

                String[] tokens = line.split("\\s+");
                String directive = tokens[0];

                if (directive.equals("mtllib"))
                {
                    if (tokens.length > 1)
                    {
                        String mtlFileName = tokens[1];
                        loadMaterials(parentDir + mtlFileName, materials);
                    }
                }

                if (directive.equals("usemtl"))
                {
                    if (tokens.length > 1)
                    {
                        currentMaterial = materials.get(tokens[1]);
                    }
                }

                if (directive.equals("o") || directive.equals("g"))
                {
                    if (!currentPositionIndices.isEmpty())
                    {
                        ObjMesh mesh = buildMesh(currentMeshName, positions, texCoords, normals, currentPositionIndices,
                                currentTexCoordIndices, currentNormalIndices);
                        mesh.setMaterial(currentMaterial);
                        model.addMesh(mesh);

                        currentPositionIndices.clear();
                        currentTexCoordIndices.clear();
                        currentNormalIndices.clear();
                    }

                    if (tokens.length > 1)
                    {
                        currentMeshName = tokens[1];
                    }
                }

                if (directive.equals("v"))
                {
                    positions.add(Float.parseFloat(tokens[1]));
                    positions.add(Float.parseFloat(tokens[2]));
                    positions.add(Float.parseFloat(tokens[3]));
                }

                if (directive.equals("vt"))
                {
                    texCoords.add(Float.parseFloat(tokens[1]));

                    if (tokens.length > 2)
                    {
                        texCoords.add(Float.parseFloat(tokens[2]));
                    } else
                    {
                        texCoords.add(0F);
                    }
                }

                if (directive.equals("vn"))
                {
                    normals.add(Float.parseFloat(tokens[1]));
                    normals.add(Float.parseFloat(tokens[2]));
                    normals.add(Float.parseFloat(tokens[3]));
                }

                if (directive.equals("f"))
                {
                    List<Integer> facePosIndices = new ArrayList<>();
                    List<Integer> faceTexIndices = new ArrayList<>();
                    List<Integer> faceNormIndices = new ArrayList<>();

                    for (int i = 1; i < tokens.length; i++)
                    {
                        String[] vertexTokens = tokens[i].split("/");

                        int posIndex = Integer.parseInt(vertexTokens[0]) - 1;
                        facePosIndices.add(posIndex);

                        if (vertexTokens.length > 1 && !vertexTokens[1].isEmpty())
                        {
                            int texIndex = Integer.parseInt(vertexTokens[1]) - 1;
                            faceTexIndices.add(texIndex);
                        } else
                        {
                            faceTexIndices.add(-1);
                        }

                        if (vertexTokens.length > 2 && !vertexTokens[2].isEmpty())
                        {
                            int normIndex = Integer.parseInt(vertexTokens[2]) - 1;
                            faceNormIndices.add(normIndex);
                        } else
                        {
                            faceNormIndices.add(-1);
                        }
                    }

                    for (int i = 1; i < facePosIndices.size() - 1; i++)
                    {
                        currentPositionIndices.add(facePosIndices.get(0));
                        currentPositionIndices.add(facePosIndices.get(i));
                        currentPositionIndices.add(facePosIndices.get(i + 1));

                        currentTexCoordIndices.add(faceTexIndices.get(0));
                        currentTexCoordIndices.add(faceTexIndices.get(i));
                        currentTexCoordIndices.add(faceTexIndices.get(i + 1));

                        currentNormalIndices.add(faceNormIndices.get(0));
                        currentNormalIndices.add(faceNormIndices.get(i));
                        currentNormalIndices.add(faceNormIndices.get(i + 1));
                    }
                }
            }

            if (!currentPositionIndices.isEmpty())
            {
                ObjMesh mesh = buildMesh(currentMeshName, positions, texCoords, normals, currentPositionIndices,
                        currentTexCoordIndices, currentNormalIndices);
                mesh.setMaterial(currentMaterial);
                model.addMesh(mesh);
            }

        } catch (IOException e)
        {
            throw new RuntimeException("Failed to load OBJ file: " + path, e);
        }

        return model;
    }

    private static void loadMaterials(String mtlPath, Map<String, ObjMaterial> materials)
    {
        File mtlFile = new File(mtlPath);

        if (!mtlFile.exists())
        {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(mtlFile)))
        {
            String line;
            ObjMaterial currentMaterial = null;

            while ((line = reader.readLine()) != null)
            {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#"))
                {
                    continue;
                }

                String[] tokens = line.split("\\s+");
                String directive = tokens[0];

                if (directive.equals("newmtl"))
                {
                    if (tokens.length > 1)
                    {
                        currentMaterial = new ObjMaterial(tokens[1]);
                        materials.put(tokens[1], currentMaterial);
                    }
                }

                if (directive.equals("map_Kd") && currentMaterial != null)
                {
                    if (tokens.length > 1)
                    {
                        String textureName = tokens[1];
                        String parentDir = mtlFile.getParent();

                        if (parentDir != null)
                        {
                            currentMaterial.setDiffuseTexturePath(parentDir + File.separator + textureName);
                        } else
                        {
                            currentMaterial.setDiffuseTexturePath(textureName);
                        }
                    }
                }
            }
        } catch (IOException e)
        {
            System.err.println("Failed to load MTL file: " + mtlPath);
        }
    }

    private static ObjMesh buildMesh(String name, List<Float> positions, List<Float> texCoords, List<Float> normals,
                                     List<Integer> posIndices, List<Integer> texIndices, List<Integer> normIndices)
    {
        List<Float> interleavedData = new ArrayList<>();
        List<Integer> finalIndices = new ArrayList<>();
        Map<String, Integer> vertexCache = new HashMap<>();

        int currentIndex = 0;

        for (int i = 0; i < posIndices.size(); i++)
        {
            int pIdx = posIndices.get(i);
            int tIdx = texIndices.get(i);
            int nIdx = normIndices.get(i);

            String hash = pIdx + "/" + tIdx + "/" + nIdx;

            if (vertexCache.containsKey(hash))
            {
                finalIndices.add(vertexCache.get(hash));
            } else
            {
                int baseP = pIdx * 3;
                interleavedData.add(positions.get(baseP));
                interleavedData.add(positions.get(baseP + 1));
                interleavedData.add(positions.get(baseP + 2));

                if (tIdx >= 0)
                {
                    int baseT = tIdx * 2;
                    interleavedData.add(texCoords.get(baseT));
                    interleavedData.add(1F - texCoords.get(baseT + 1));
                } else
                {
                    interleavedData.add(0F);
                    interleavedData.add(0F);
                }

                if (nIdx >= 0)
                {
                    int baseN = nIdx * 3;
                    interleavedData.add(normals.get(baseN));
                    interleavedData.add(normals.get(baseN + 1));
                    interleavedData.add(normals.get(baseN + 2));
                } else
                {
                    interleavedData.add(0F);
                    interleavedData.add(1F);
                    interleavedData.add(0F);
                }

                vertexCache.put(hash, currentIndex);
                finalIndices.add(currentIndex);
                currentIndex++;
            }
        }

        float[] vertexArray = new float[interleavedData.size()];

        for (int i = 0; i < interleavedData.size(); i++)
        {
            vertexArray[i] = interleavedData.get(i);
        }

        int[] indexArray = new int[finalIndices.size()];

        for (int i = 0; i < finalIndices.size(); i++)
        {
            indexArray[i] = finalIndices.get(i);
        }

        return new ObjMesh(name, vertexArray, indexArray);
    }
}
