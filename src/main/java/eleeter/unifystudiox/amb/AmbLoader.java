package eleeter.unifystudiox.amb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.renderer.shading.TextureSampling;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.util.log.AniLogger;

public class AmbLoader
{
    public static void loadAndAddToScene(String path, Scene scene)
    {
        try
        {
            AmbModel model = load(path);
            String id = "amb_" + path.substring(path.lastIndexOf('/') + 1);
            AmbModelInstance instance = new AmbModelInstance(id, model);
            instance.setTexture(TextureGL.loadCached("/textures/Sword_baseColor.png", TextureSampling.PIXEL_PERFECT));

            Optional<SceneEntity> existing = scene.findEntity(id);
            if (existing.isPresent() && existing.get() instanceof AmbModelInstance oldInst)
            {
                instance.setPosition(oldInst.getPosition());
                instance.setRotation(oldInst.getRotation());
                instance.setScale(oldInst.getScale());
            } else
            {
                instance.setPosition(new Vector3f(0f, 0f, 0f));
            }

            scene.addEntity(instance);

            if (scene.getAnimationSystem() != null)
            {
                AmbModelAnimatableAdapter adapter = new AmbModelAnimatableAdapter(instance);
                scene.getAnimationSystem().register(id, adapter);
                scene.getAnimationSystem().registerSkeleton(id, adapter.getSkeleton());
            }

            AniLogger.info("AmbLoader", "Loaded AMB into scene: " + path);
        } catch (Exception e)
        {
            AniLogger.warn("AmbLoader", "Failed to load AMB '" + path + "': " + e.getMessage());
        }
    }

    public static AmbModel load(String path)
    {
        AmbModel model = new AmbModel(path);

        try (BufferedReader reader = new BufferedReader(new FileReader(path)))
        {
            String line = nextNonEmpty(reader, path);
            if (line == null)
            {
                throw new RuntimeException("[AmbLoader] File is empty: " + path);
            }

            int meshCount = parseHeaderInt(line, "MeshCount", path);

            line = nextNonEmpty(reader, path);
            int skeletonCount = parseHeaderInt(line, "SkeletonCount", path);

            for (int meshIndex = 0; meshIndex < meshCount; meshIndex++)
            {
                line = nextNonEmpty(reader, path);
                if (line == null)
                {
                    throw new RuntimeException("[AmbLoader] Expected Mesh header for mesh " + meshIndex
                            + " but reached end of file: " + path);
                }
                if (!line.startsWith("Mesh "))
                {
                    throw new RuntimeException("[AmbLoader] Expected 'Mesh \"name\"' at mesh " + meshIndex + ", got: '"
                            + line + "' in: " + path);
                }

                String meshName = parseQuotedName(line, path);

                line = nextNonEmpty(reader, path);
                int vertexCount = parseHeaderInt(line, "VertexCount", path);

                line = nextNonEmpty(reader, path);
                int indexCount = parseHeaderInt(line, "IndexCount", path);

                float[] vertexData = new float[vertexCount * 16];
                for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++)
                {
                    line = nextRaw(reader, path);
                    String[] tokens = line.trim().split("\\s+");
                    if (tokens.length != 16)
                    {
                        throw new RuntimeException("[AmbLoader] Vertex line " + vertexIndex + " in mesh '" + meshName
                                + "' has " + tokens.length + " tokens, expected 16. Line: '" + line + "' in: " + path);
                    }
                    int base = vertexIndex * 16;
                    for (int component = 0; component < 16; component++)
                    {
                        vertexData[base + component] = parseFloat(tokens[component], path, line);
                    }
                }

                int[] indices = new int[indexCount];
                for (int indexPos = 0; indexPos < indexCount; indexPos++)
                {
                    line = nextRaw(reader, path);
                    indices[indexPos] = parseInt(line.trim(), path, line);
                }

                model.meshes.add(new AmbMesh(meshName, vertexData, indices));
            }

            for (int skelIndex = 0; skelIndex < skeletonCount; skelIndex++)
            {
                line = nextNonEmpty(reader, path);
                if (line == null)
                {
                    throw new RuntimeException("[AmbLoader] Expected 'Skeleton' keyword for skeleton " + skelIndex
                            + " but reached end of file: " + path);
                }
                if (!line.equals("Skeleton"))
                {
                    throw new RuntimeException(
                            "[AmbLoader] Expected 'Skeleton' keyword, got: '" + line + "' in: " + path);
                }

                line = nextNonEmpty(reader, path);
                int boneCount = parseHeaderInt(line, "BoneCount", path);

                for (int boneIndex = 0; boneIndex < boneCount; boneIndex++)
                {
                    line = nextNonEmpty(reader, path);
                    if (line == null)
                    {
                        throw new RuntimeException(
                                "[AmbLoader] Expected bone line " + boneIndex + " but reached end of file: " + path);
                    }
                    if (!line.startsWith("\""))
                    {
                        throw new RuntimeException("[AmbLoader] Bone line " + boneIndex
                                + " must start with a quoted name. Got: '" + line + "' in: " + path);
                    }

                    int closeQuote = line.indexOf('"', 1);
                    if (closeQuote < 0)
                    {
                        throw new RuntimeException(
                                "[AmbLoader] Unclosed quote in bone name: '" + line + "' in: " + path);
                    }

                    String boneName = line.substring(1, closeQuote);
                    String remainder = line.substring(closeQuote + 1).trim();
                    String[] tokens = remainder.split("\\s+");

                    if (tokens.length < 17)
                    {
                        throw new RuntimeException("[AmbLoader] Bone '" + boneName + "' has only " + tokens.length
                                + " values after name, expected 17. Line: '" + line + "' in: " + path);
                    }

                    int parentIndex = parseInt(tokens[0], path, line);

                    float[] matValues = new float[16];
                    for (int component = 0; component < 16; component++)
                    {
                        matValues[component] = parseFloat(tokens[1 + component], path, line);
                    }
                    Matrix4f boneTransform = new Matrix4f(
                            matValues[0], matValues[1], matValues[2], matValues[3],
                            matValues[4], matValues[5], matValues[6], matValues[7],
                            matValues[8], matValues[9], matValues[10], matValues[11],
                            matValues[12], matValues[13], matValues[14], matValues[15]);

                    model.skeleton.bones.add(new AmbBone(boneName, parentIndex, boneTransform));
                }
            }
        } catch (IOException ioException)
        {
            throw new RuntimeException("[AmbLoader] I/O error reading file: " + path, ioException);
        }

        if (!model.skeleton.bones.isEmpty())
        {
            model.skeleton.calculateInverseBindMatrices();
            model.skeleton.convertGlobalToLocalBind();
            model.skeleton.calculateGlobalTransforms();
        }

        System.out.println("[AmbLoader] Loaded '" + path + "': " + model.meshes.size() + " mesh(es), "
                + model.skeleton.bones.size() + " bone(s).");

        return model;
    }

    private static String nextNonEmpty(BufferedReader reader, String path) throws IOException
    {
        String line;
        while ((line = reader.readLine()) != null)
        {
            line = line.trim();
            if (!line.isEmpty())
            {
                return line;
            }
        }
        return null;
    }

    private static String nextRaw(BufferedReader reader, String path) throws IOException
    {
        String line;
        while ((line = reader.readLine()) != null)
        {

            if (!line.trim().isEmpty())
            {
                return line;
            }
        }
        throw new RuntimeException("[AmbLoader] Unexpected end of file in: " + path);
    }

    private static int parseHeaderInt(String line, String expectedKey, String path)
    {
        if (line == null)
        {
            throw new RuntimeException(
                    "[AmbLoader] Expected header '" + expectedKey + "' but got null (end of file) in: " + path);
        }
        if (!line.startsWith(expectedKey + ":"))
        {
            throw new RuntimeException(
                    "[AmbLoader] Expected header '" + expectedKey + ":' but got: '" + line + "' in: " + path);
        }
        String valueStr = line.substring(expectedKey.length() + 1).trim();
        try
        {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException numberFormatException)
        {
            throw new RuntimeException("[AmbLoader] Cannot parse integer from header '" + line + "' in: " + path,
                    numberFormatException);
        }
    }

    private static String parseQuotedName(String line, String path)
    {
        int open = line.indexOf('"');
        int close = line.indexOf('"', open + 1);
        if (open < 0 || close < 0)
        {
            throw new RuntimeException("[AmbLoader] Cannot find quoted name in line: '" + line + "' in: " + path);
        }
        return line.substring(open + 1, close);
    }

    private static float parseFloat(String token, String path, String line)
    {
        try
        {
            return Float.parseFloat(token);
        } catch (NumberFormatException numberFormatException)
        {
            throw new RuntimeException(
                    "[AmbLoader] Cannot parse float '" + token + "' on line: '" + line + "' in: " + path,
                    numberFormatException);
        }
    }

    private static int parseInt(String token, String path, String line)
    {
        try
        {
            return Integer.parseInt(token);
        } catch (NumberFormatException numberFormatException)
        {
            throw new RuntimeException(
                    "[AmbLoader] Cannot parse int '" + token + "' on line: '" + line + "' in: " + path,
                    numberFormatException);
        }
    }
}
