package eleeter.unifystudiox.vfx.renderer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL33.GL_TEXTURE0;
import static org.lwjgl.opengl.GL33.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL33.glActiveTexture;
import static org.lwjgl.opengl.GL33.glBindTexture;
import static org.lwjgl.opengl.GL33.glDeleteProgram;
import static org.lwjgl.opengl.GL33.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL33.glVertexAttribPointer;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import eleeter.unifystudiox.vfx.core.VFXEmitter;
import eleeter.unifystudiox.vfx.core.VFXParticle;
import eleeter.unifystudiox.vfx.shader.ShaderLoader;

/**
 * Renders ring/halo particles using GPU instancing. A pre-tessellated flat ring
 * mesh is stored in a static VBO; per-ring transform data (center, color, radius,
 * thickness, rotation) is uploaded into a dynamic instance VBO each frame.
 *
 * <p>Rings are rendered horizontally (Y-up plane) by default. Y-axis rotation
 * is applied in the vertex shader via the iRotation per-instance attribute.</p>
 */
public class RingRenderer
{

    private static final int MAX_INSTANCES = 500;
    private static final int RING_SEGMENTS = 48;

    /*
     * Per-instance layout (10 floats):
     *   iCenter    [3 floats] — world XYZ center
     *   iColor     [4 floats] — RGBA
     *   iRadius    [1 float]  — ring outer radius
     *   iThickness [1 float]  — ring thickness (inner = outer - thickness)
     *   iRotation  [1 float]  — Y-axis rotation in radians
     */
    private static final int INSTANCE_FLOATS = 10;
    private static final int INSTANCE_STRIDE = INSTANCE_FLOATS * Float.BYTES;

    private int vao;
    private int ringVBO;
    private int instanceVBO;
    private int ringVertCount;
    private int shader;
    private FloatBuffer instanceBuffer;

    /**
     * Allocates GPU resources.
     */
    public void init()
    {
        this.instanceBuffer = BufferUtils.createFloatBuffer(MAX_INSTANCES * INSTANCE_FLOATS);
        this.shader = ShaderLoader.load("/shaders/vfx/ring.vert", "/shaders/vfx/ring.frag");

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        /* --- Build static ring mesh as a strip of quads (inner + outer vertices) --- */
        int segs = RING_SEGMENTS;
        this.ringVertCount = (segs + 1) * 2;
        FloatBuffer ringData = BufferUtils.createFloatBuffer(this.ringVertCount * 5);

        for (int i = 0; i <= segs; i++)
        {
            float angle = (float) (i * Math.PI * 2.0D / segs);
            float cosA = (float) Math.cos(angle);
            float sinA = (float) Math.sin(angle);
            float u = (float) i / segs;

            /* Outer ring vertex — radius 1 (scaled in shader by iRadius) */
            ringData.put(cosA).put(0.0F).put(sinA)
                    .put(u).put(1.0F);

            /* Inner ring vertex — radius scales to (1 - thickness/radius) */
            float innerFrac = 0.6F;
            ringData.put(cosA * innerFrac).put(0.0F).put(sinA * innerFrac)
                    .put(u).put(0.0F);
        }
        ringData.flip();

        this.ringVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.ringVBO);
        glBufferData(GL_ARRAY_BUFFER, ringData, GL_STATIC_DRAW);

        int ringVertStride = 5 * Float.BYTES;

        /* aPos — location 0, 3 floats */
        glVertexAttribPointer(0, 3, GL_FLOAT, false, ringVertStride, 0L);
        glEnableVertexAttribArray(0);
        /* aUV — location 1, 2 floats */
        glVertexAttribPointer(1, 2, GL_FLOAT, false, ringVertStride, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);

        /* --- Dynamic instance VBO --- */
        this.instanceVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.instanceVBO);
        glBufferData(GL_ARRAY_BUFFER, (long) MAX_INSTANCES * INSTANCE_STRIDE, GL_DYNAMIC_DRAW);

        /* iCenter — location 2, 3 floats */
        glVertexAttribPointer(2, 3, GL_FLOAT, false, INSTANCE_STRIDE, 0L);
        glEnableVertexAttribArray(2);
        glVertexAttribDivisor(2, 1);

        /* iColor — location 3, 4 floats */
        glVertexAttribPointer(3, 4, GL_FLOAT, false, INSTANCE_STRIDE, 3L * Float.BYTES);
        glEnableVertexAttribArray(3);
        glVertexAttribDivisor(3, 1);

        /* iRadius — location 4, 1 float */
        glVertexAttribPointer(4, 1, GL_FLOAT, false, INSTANCE_STRIDE, 7L * Float.BYTES);
        glEnableVertexAttribArray(4);
        glVertexAttribDivisor(4, 1);

        /* iThickness — location 5, 1 float */
        glVertexAttribPointer(5, 1, GL_FLOAT, false, INSTANCE_STRIDE, 8L * Float.BYTES);
        glEnableVertexAttribArray(5);
        glVertexAttribDivisor(5, 1);

        /* iRotation — location 6, 1 float */
        glVertexAttribPointer(6, 1, GL_FLOAT, false, INSTANCE_STRIDE, 9L * Float.BYTES);
        glEnableVertexAttribArray(6);
        glVertexAttribDivisor(6, 1);

        glBindVertexArray(0);
    }

    /**
     * Uploads per-ring instance data and issues one instanced draw call.
     *
     * @param emitter source emitter (renderType must be RING)
     * @param view    column-major float[16] view matrix
     * @param proj    column-major float[16] projection matrix
     */
    public void render(VFXEmitter emitter, float[] view, float[] proj)
    {
        this.instanceBuffer.clear();
        int drawn = 0;

        for (int i = 0; i < emitter.maxParticles; i++)
        {
            VFXParticle p = emitter.pool[i];
            if (p.life <= 0.0F) continue;

            this.instanceBuffer
                    .put(p.x).put(p.y).put(p.z)
                    .put(p.r).put(p.g).put(p.b).put(p.a)
                    .put(emitter.ringRadius * p.scaleX)
                    .put(emitter.ringThickness)
                    .put(p.rotation);

            drawn++;
            if (drawn >= MAX_INSTANCES) break;
        }

        if (drawn == 0) return;

        this.instanceBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, this.instanceVBO);
        glBufferSubData(GL_ARRAY_BUFFER, 0L, this.instanceBuffer);

        glUseProgram(this.shader);
        glUniformMatrix4fv(glGetUniformLocation(this.shader, "uView"), false, view);
        glUniformMatrix4fv(glGetUniformLocation(this.shader, "uProjection"), false, proj);

        int hasTexture = (emitter.textureId > 0) ? 1 : 0;
        glUniform1i(glGetUniformLocation(this.shader, "uHasTexture"), hasTexture);

        if (emitter.textureId > 0)
        {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, emitter.textureId);
            glUniform1i(glGetUniformLocation(this.shader, "uTexture"), 0);
        }

        glBindVertexArray(this.vao);
        glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, this.ringVertCount, drawn);
        glBindVertexArray(0);
    }

    /**
     * Releases all GPU resources.
     */
    public void destroy()
    {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.ringVBO);
        glDeleteBuffers(this.instanceVBO);
        glDeleteProgram(this.shader);
    }
}
