package eleeter.unifystudiox.editor.animation;

import eleeter.unifystudiox.renderer.shading.ShaderProgram;
import eleeter.unifystudiox.scene.entity.RiggedEntity;
import eleeter.unifystudiox.scene.entity.SkeletalData;
import java.nio.FloatBuffer;
import java.util.Arrays;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL45C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL45C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL45C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL45C.GL_FLOAT;
import static org.lwjgl.opengl.GL45C.GL_LINES;
import static org.lwjgl.opengl.GL45C.glBindBuffer;
import static org.lwjgl.opengl.GL45C.glBindVertexArray;
import static org.lwjgl.opengl.GL45C.glBufferData;
import static org.lwjgl.opengl.GL45C.glCreateBuffers;
import static org.lwjgl.opengl.GL45C.glCreateVertexArrays;
import static org.lwjgl.opengl.GL45C.glDeleteBuffers;
import static org.lwjgl.opengl.GL45C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL45C.glDisable;
import static org.lwjgl.opengl.GL45C.glDrawArrays;
import static org.lwjgl.opengl.GL45C.glEnable;
import static org.lwjgl.opengl.GL45C.glEnableVertexArrayAttrib;
import static org.lwjgl.opengl.GL45C.glLineWidth;
import static org.lwjgl.opengl.GL45C.glNamedBufferData;
import static org.lwjgl.opengl.GL45C.glVertexArrayAttribBinding;
import static org.lwjgl.opengl.GL45C.glVertexArrayAttribFormat;
import static org.lwjgl.opengl.GL45C.glVertexArrayVertexBuffer;


public class BoneOverlayRenderer
{
    /* Bone-stick line appearance */
    private static final float BONE_LINE_R = 0.85F;
    private static final float BONE_LINE_G = 0.82F;
    private static final float BONE_LINE_B = 0.70F;
    private static final float BONE_LINE_W = 2.0F;

    /* Hovered bone outline colour */
    private static final float BONE_HOVER_R = 0.35F;
    private static final float BONE_HOVER_G = 0.80F;
    private static final float BONE_HOVER_B = 1.00F;

    private static final float BONE_SEL_R = 1.00F;
    private static final float BONE_SEL_G = 0.65F;
    private static final float BONE_SEL_B = 0.10F;

    private static final int MAX_BONES = 200;

    private static final int FLOATS_PER_VERTEX = 3;

    private static final int LINE_VBO_FLOAT_COUNT = MAX_BONES * 2 * FLOATS_PER_VERTEX;

    private ShaderProgram lineShader;

    private int vaoLines;
    private int vboLines;

    private FloatBuffer matrixBuf;

    private final float[] normalVerts = new float[LINE_VBO_FLOAT_COUNT];
    private final float[] hoverVerts = new float[LINE_VBO_FLOAT_COUNT];
    private final float[] selVerts = new float[LINE_VBO_FLOAT_COUNT];

    private boolean initialized = false;

    public void init()
    {
        if (this.initialized)
        {
            return;
        }

        this.matrixBuf = MemoryUtil.memAllocFloat(16);

        this.lineShader = ShaderProgram.builder()
                .vertex("/shaders/gizmo_simple.vert")
                .fragment("/shaders/gizmo_simple.frag")
                .build();

        this.buildLineVao();

        this.initialized = true;
    }


    public void draw(RiggedEntity entity, Matrix4f modelMx, Matrix4f view, Matrix4f proj)
    {
        if (!this.initialized)
        {
            return;
        }

        SkeletalData skeletal = entity.getSkeletalData();
        int boneCount = Math.min(skeletal.getBoneCount(), MAX_BONES);
        if (boneCount == 0)
        {
            return;
        }

        this.drawBoneSticks(entity, skeletal, modelMx, view, proj, boneCount);

        glBindVertexArray(0);
    }


    public void cleanup()
    {
        if (!this.initialized)
        {
            return;
        }

        if (this.lineShader != null)
        {
            this.lineShader.cleanup();
            this.lineShader = null;
        }

        glDeleteVertexArrays(this.vaoLines);
        glDeleteBuffers(this.vboLines);

        if (this.matrixBuf != null)
        {
            MemoryUtil.memFree(this.matrixBuf);
            this.matrixBuf = null;
        }

        this.initialized = false;
    }


    private void drawBoneSticks(RiggedEntity entity, SkeletalData skeletal, Matrix4f modelMx,
                                Matrix4f view, Matrix4f proj, int boneCount)
    {
        int normalIdx = 0;
        int hoverIdx = 0;
        int selIdx = 0;

        int selectedBone = entity.getSelectedBoneIndex();
        int hoveredBone = entity.getHoveredBoneIndex();

        Vector3f parentPos = new Vector3f();
        Vector3f childPos = new Vector3f();
        Matrix4f boneWorld = new Matrix4f();

        for (int i = 0; i < boneCount; i++)
        {
            int parentIdx = skeletal.getParentIndex(i);
            if (parentIdx < 0)
            {
                continue;
            }

            /* Parent joint world position */
            skeletal.getBoneWorldMatrix(parentIdx).getTranslation(parentPos);

            /* Child joint world position */
            boneWorld.set(skeletal.getBoneWorldMatrix(i));
            boneWorld.getTranslation(childPos);

            if (i == selectedBone)
            {
                if (selIdx + 6 <= LINE_VBO_FLOAT_COUNT)
                {
                    this.selVerts[selIdx++] = parentPos.x;
                    this.selVerts[selIdx++] = parentPos.y;
                    this.selVerts[selIdx++] = parentPos.z;
                    this.selVerts[selIdx++] = childPos.x;
                    this.selVerts[selIdx++] = childPos.y;
                    this.selVerts[selIdx++] = childPos.z;
                }
            } else if (i == hoveredBone)
            {
                if (hoverIdx + 6 <= LINE_VBO_FLOAT_COUNT)
                {
                    this.hoverVerts[hoverIdx++] = parentPos.x;
                    this.hoverVerts[hoverIdx++] = parentPos.y;
                    this.hoverVerts[hoverIdx++] = parentPos.z;
                    this.hoverVerts[hoverIdx++] = childPos.x;
                    this.hoverVerts[hoverIdx++] = childPos.y;
                    this.hoverVerts[hoverIdx++] = childPos.z;
                }
            } else
            {
                if (normalIdx + 6 <= LINE_VBO_FLOAT_COUNT)
                {
                    this.normalVerts[normalIdx++] = parentPos.x;
                    this.normalVerts[normalIdx++] = parentPos.y;
                    this.normalVerts[normalIdx++] = parentPos.z;
                    this.normalVerts[normalIdx++] = childPos.x;
                    this.normalVerts[normalIdx++] = childPos.y;
                    this.normalVerts[normalIdx++] = childPos.z;
                }
            }
        }

        Matrix4f identity = new Matrix4f();

        glDisable(GL_DEPTH_TEST);
        this.lineShader.bind();
        this.lineShader.setUniform("uProjection", proj, this.matrixBuf);
        this.lineShader.setUniform("uView", view, this.matrixBuf);
        this.lineShader.setUniform("uModel", identity, this.matrixBuf);

        glLineWidth(BONE_LINE_W);
        glBindVertexArray(this.vaoLines);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboLines);

        if (normalIdx > 0)
        {
            glBufferData(GL_ARRAY_BUFFER, Arrays.copyOf(this.normalVerts, normalIdx), GL_DYNAMIC_DRAW);
            this.lineShader.setUniform("uColor", BONE_LINE_R, BONE_LINE_G, BONE_LINE_B);
            glDrawArrays(GL_LINES, 0, normalIdx / FLOATS_PER_VERTEX);
        }

        if (hoverIdx > 0)
        {
            glBufferData(GL_ARRAY_BUFFER, Arrays.copyOf(this.hoverVerts, hoverIdx), GL_DYNAMIC_DRAW);
            this.lineShader.setUniform("uColor", BONE_HOVER_R, BONE_HOVER_G, BONE_HOVER_B);
            glDrawArrays(GL_LINES, 0, hoverIdx / FLOATS_PER_VERTEX);
        }

        if (selIdx > 0)
        {
            glBufferData(GL_ARRAY_BUFFER, Arrays.copyOf(this.selVerts, selIdx), GL_DYNAMIC_DRAW);
            this.lineShader.setUniform("uColor", BONE_SEL_R, BONE_SEL_G, BONE_SEL_B);
            glDrawArrays(GL_LINES, 0, selIdx / FLOATS_PER_VERTEX);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glEnable(GL_DEPTH_TEST);
        this.lineShader.unbind();
    }

    private void buildLineVao()
    {
        this.vaoLines = glCreateVertexArrays();
        this.vboLines = glCreateBuffers();

        FloatBuffer placeholder = MemoryUtil.memAllocFloat(LINE_VBO_FLOAT_COUNT);
        glNamedBufferData(this.vboLines, placeholder, GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(placeholder);

        glEnableVertexArrayAttrib(this.vaoLines, 0);
        glVertexArrayAttribFormat(this.vaoLines, 0, 3, GL_FLOAT, false, 0);
        glVertexArrayAttribBinding(this.vaoLines, 0, 0);
        glVertexArrayVertexBuffer(this.vaoLines, 0, this.vboLines, 0L, FLOATS_PER_VERTEX * Float.BYTES);
    }
}
