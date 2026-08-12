package eleeter.unifystudiox.vfx.renderer;

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
import static org.lwjgl.opengl.GL33.GL_FLOAT;
import static org.lwjgl.opengl.GL33.GL_TEXTURE0;
import static org.lwjgl.opengl.GL33.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL33.GL_TRIANGLES;
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
 * Renders billboard quad particles for a single {@link VFXEmitter} in one
 * GPU-instanced draw call. All live particles are packed into a dynamic VBO
 * per frame; the static quad geometry is shared by all instances.
 */
public class SpriteRenderer
{

    private static final int MAX_INSTANCES = 10_000;

    /*
     * Per-instance stride layout (14 floats):
     *   location 2  — iPosition   [3 floats] — offset  0
     *   location 3  — iColor      [4 floats] — offset 12
     *   location 4  — iSize       [2 floats] — offset 28
     *   location 5  — iRotation   [1 float]  — offset 36
     *   location 6  — iUVRegion   [4 floats] — offset 40
     */
    private static final int INSTANCE_FLOATS = 14;
    private static final int INSTANCE_STRIDE = INSTANCE_FLOATS * Float.BYTES;

    private static final float[] QUAD_DATA =
            {
                    /* aPos.x  aPos.y  aUV.x  aUV.y */
                    -0.5F, 0.5F, 0.0F, 1.0F,
                    -0.5F, -0.5F, 0.0F, 0.0F,
                    0.5F, -0.5F, 1.0F, 0.0F,
                    -0.5F, 0.5F, 0.0F, 1.0F,
                    0.5F, -0.5F, 1.0F, 0.0F,
                    0.5F, 0.5F, 1.0F, 1.0F,
            };

    private int vao;
    private int quadVBO;
    private int instanceVBO;
    private int shader;
    private FloatBuffer instanceBuffer;

    /**
     * Allocates GPU resources. Must be called from the render thread.
     */
    public void init()
    {
        this.instanceBuffer = BufferUtils.createFloatBuffer(MAX_INSTANCES * INSTANCE_FLOATS);

        this.shader = ShaderLoader.load("/shaders/vfx/sprite.vert", "/shaders/vfx/sprite.frag");

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        /* --- Static quad VBO --- */
        this.quadVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.quadVBO);
        FloatBuffer qb = BufferUtils.createFloatBuffer(QUAD_DATA.length);
        qb.put(QUAD_DATA).flip();
        glBufferData(GL_ARRAY_BUFFER, qb, GL_STATIC_DRAW);

        /* aPos  — location 0, 2 floats, stride 4 floats */
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        /* aUV   — location 1, 2 floats */
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);

        /* --- Dynamic instance VBO --- */
        this.instanceVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.instanceVBO);
        glBufferData(GL_ARRAY_BUFFER, (long) MAX_INSTANCES * INSTANCE_STRIDE, GL_DYNAMIC_DRAW);

        /* iPosition — location 2, 3 floats, offset 0 */
        glVertexAttribPointer(2, 3, GL_FLOAT, false, INSTANCE_STRIDE, 0L);
        glEnableVertexAttribArray(2);
        glVertexAttribDivisor(2, 1);

        /* iColor — location 3, 4 floats, offset 12 */
        glVertexAttribPointer(3, 4, GL_FLOAT, false, INSTANCE_STRIDE, 3L * Float.BYTES);
        glEnableVertexAttribArray(3);
        glVertexAttribDivisor(3, 1);

        /* iSize — location 4, 2 floats, offset 28 */
        glVertexAttribPointer(4, 2, GL_FLOAT, false, INSTANCE_STRIDE, 7L * Float.BYTES);
        glEnableVertexAttribArray(4);
        glVertexAttribDivisor(4, 1);

        /* iRotation — location 5, 1 float, offset 36 */
        glVertexAttribPointer(5, 1, GL_FLOAT, false, INSTANCE_STRIDE, 9L * Float.BYTES);
        glEnableVertexAttribArray(5);
        glVertexAttribDivisor(5, 1);

        /* iUVRegion — location 6, 4 floats, offset 40 */
        glVertexAttribPointer(6, 4, GL_FLOAT, false, INSTANCE_STRIDE, 10L * Float.BYTES);
        glEnableVertexAttribArray(6);
        glVertexAttribDivisor(6, 1);

        glBindVertexArray(0);
    }

    /**
     * Uploads per-particle data and issues one instanced draw call.
     *
     * @param emitter the emitter whose particles to render
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
                    .put(p.scaleX).put(p.scaleY)
                    .put(p.rotation)
                    .put(p.u0).put(p.v0).put(p.u1).put(p.v1);

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
        glUniform1i(glGetUniformLocation(this.shader, "uBillboard"), 0);
        glUniform1i(glGetUniformLocation(this.shader, "uSoftParticle"), 0);

        int hasTexture = (emitter.textureId > 0) ? 1 : 0;
        glUniform1i(glGetUniformLocation(this.shader, "uHasTexture"), hasTexture);

        if (emitter.textureId > 0)
        {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, emitter.textureId);
            glUniform1i(glGetUniformLocation(this.shader, "uTexture"), 0);
        }

        glBindVertexArray(this.vao);
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, drawn);
        glBindVertexArray(0);
    }

    /**
     * Releases all GPU resources. Must be called from the render thread.
     */
    public void destroy()
    {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.quadVBO);
        glDeleteBuffers(this.instanceVBO);
        glDeleteProgram(this.shader);
    }
}
