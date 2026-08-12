package eleeter.unifystudiox.vfx.renderer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
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
import static org.lwjgl.opengl.GL33.glDrawArrays;
import static org.lwjgl.opengl.GL33.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL33.glVertexAttribPointer;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import eleeter.unifystudiox.vfx.core.VFXEmitter;
import eleeter.unifystudiox.vfx.core.VFXParticle;
import eleeter.unifystudiox.vfx.shader.ShaderLoader;

public class RibbonRenderer
{

    private static final int MAX_PARTICLES = 500;
    private static final int MAX_HIST_POINTS = 64;

    /*
     * Per-vertex layout (10 floats):
     *   aPos   [3 floats] — world position of the expanded strip vertex
     *   aColor [4 floats] — RGBA
     *   aUV    [2 floats] — UV
     *   aWidth [1 float]  — half-width of the strip at this vertex (signed: -/+)
     */
    private static final int VERTEX_FLOATS = 10;
    private static final int VERTEX_STRIDE = VERTEX_FLOATS * Float.BYTES;

    private int vao;
    private int vbo;
    private int shader;
    private FloatBuffer vertexBuffer;

    /**
     * Allocates GPU resources.
     */
    public void init()
    {
        int maxVerts = MAX_PARTICLES * MAX_HIST_POINTS * 2;
        this.vertexBuffer = BufferUtils.createFloatBuffer(maxVerts * VERTEX_FLOATS);

        this.shader = ShaderLoader.load("/shaders/vfx/ribbon.vert", "/shaders/vfx/ribbon.frag");

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        this.vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) maxVerts * VERTEX_STRIDE, GL_DYNAMIC_DRAW);

        /* aPos - location 0, 3 floats */
        glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_STRIDE, 0L);
        glEnableVertexAttribArray(0);
        /* aColor - location 1, 4 floats */
        glVertexAttribPointer(1, 4, GL_FLOAT, false, VERTEX_STRIDE, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);
        /* aUV - location 2, 2 floats */
        glVertexAttribPointer(2, 2, GL_FLOAT, false, VERTEX_STRIDE, 7L * Float.BYTES);
        glEnableVertexAttribArray(2);
        /* aWidth - location 3, 1 float */
        glVertexAttribPointer(3, 1, GL_FLOAT, false, VERTEX_STRIDE, 9L * Float.BYTES);
        glEnableVertexAttribArray(3);

        glBindVertexArray(0);
    }


    public void render(VFXEmitter emitter, float[] view, float[] proj)
    {
        this.vertexBuffer.clear();
        int totalVerts = 0;
        int drawn = 0;

        float halfWidth = emitter.ribbonWidth * 0.5F;

        int[] vertsPerRibbon = new int[emitter.maxParticles];

        for (int i = 0; i < emitter.maxParticles; i++)
        {
            VFXParticle p = emitter.pool[i];
            if (p.life <= 0.0F)
            {
                continue;
            }

            if (p.histX == null || p.histLen < 2)
            {
                continue;
            }

            int histSize = p.histX.length;
            float ageFrac = p.normalizedLife;
            int ribbonVerts = 0;

            for (int j = 0; j < p.histLen; j++)
            {
                int idx = ((p.histHead - p.histLen + j) % histSize + histSize) % histSize;
                float hx = p.histX[idx];
                float hy = p.histY[idx];
                float hz = p.histZ[idx];

                float segFrac = (float) j / (p.histLen - 1);
                float w = halfWidth * emitter.ribbonWidthCurve.evaluate(segFrac);

                float uvX = segFrac;
                float alpha = p.a * (1.0F - segFrac);

                /* Top vertex (positive width offset — expanded by the shader) */
                this.vertexBuffer
                        .put(hx).put(hy).put(hz)
                        .put(p.r).put(p.g).put(p.b).put(alpha)
                        .put(uvX).put(1.0F)
                        .put(w);

                /* Bottom vertex (negative width offset) */
                this.vertexBuffer.put(hx).put(hy).put(hz).put(p.r).put(p.g).put(p.b).put(alpha).put(uvX).put(0.0F).put(-w);

                ribbonVerts += 2;
            }
            vertsPerRibbon[drawn] = ribbonVerts;
            totalVerts += ribbonVerts;
            drawn++;

            if (drawn >= MAX_PARTICLES)
            {
                break;
            }
        }

        if (totalVerts == 0 || drawn == 0)
        {
            return;
        }

        this.vertexBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0L, this.vertexBuffer);

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

        int offset = 0;
        for (int i = 0; i < drawn; i++)
        {
            int count = vertsPerRibbon[i];
            if (count > 0)
            {
                glDrawArrays(GL_TRIANGLE_STRIP, offset, count);
                offset += count;
            }
        }

        glBindVertexArray(0);
    }

    /**
     * Releases all GPU resources.
     */
    public void destroy()
    {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteProgram(this.shader);
    }
}
