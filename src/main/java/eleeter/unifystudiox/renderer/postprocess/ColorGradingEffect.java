package eleeter.unifystudiox.renderer.postprocess;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

import java.util.Map;

import org.lwjgl.opengl.GL11C;

public class ColorGradingEffect implements PostEffect
{
    private static final String NAME = "ColorGrading";
    private static final float DEF_CONTRAST = 1.0F;
    private static final float DEF_BRIGHTNESS = 0.0F;
    private static final float DEF_SATURATION = 1.0F;

    private static final String VERT = """
            #version 330 core
            out vec2 vTexCoord;
            void main() {
                vec2 pos[3] = vec2[3](vec2(-1.0,-1.0), vec2(3.0,-1.0), vec2(-1.0,3.0));
                vTexCoord = pos[gl_VertexID] * 0.5 + 0.5;
                gl_Position = vec4(pos[gl_VertexID], 0.0, 1.0);
            }
            """;

    private static final String FRAG = """
            #version 330 core
            in vec2 vTexCoord;
            out vec4 fragColor;
            uniform sampler2D uScene;
            uniform float uContrast;
            uniform float uBrightness;
            uniform float uSaturation;
            void main() {
                vec3 col = texture(uScene, vTexCoord).rgb;
                col = (col - 0.5) * uContrast + 0.5 + uBrightness;
                float lum = dot(col, vec3(0.2126, 0.7152, 0.0722));
                col = mix(vec3(lum), col, uSaturation);
                fragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
            }
            """;

    private float contrast = DEF_CONTRAST;
    private float brightness = DEF_BRIGHTNESS;
    private float saturation = DEF_SATURATION;
    private boolean enabled = true;

    private PostShader shader;
    private FullscreenQuad quad;

    @Override
    public void init()
    {
        this.shader = new PostShader(NAME, VERT, FRAG);
        this.quad = new FullscreenQuad();
    }

    @Override
    public void apply(int inputTexture, int outputFBO, int width, int height)
    {
        glBindFramebuffer(GL_FRAMEBUFFER, outputFBO);
        GL11C.glViewport(0, 0, width, height);

        this.shader.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, inputTexture);
        this.shader.setInt("uScene", 0);
        this.shader.setFloat("uContrast", this.contrast);
        this.shader.setFloat("uBrightness", this.brightness);
        this.shader.setFloat("uSaturation", this.saturation);
        this.quad.draw();
        this.shader.unbind();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void dispose()
    {
        if (this.shader != null) this.shader.dispose();
        if (this.quad != null) this.quad.dispose();
    }

    @Override
    public boolean isEnabled()
    {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean e)
    {
        this.enabled = e;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    public void setContrast(float v)
    {
        this.contrast = v;
    }

    public void setBrightness(float v)
    {
        this.brightness = v;
    }

    public void setSaturation(float v)
    {
        this.saturation = v;
    }

    @Override
    public void writeState(Map<String, Object> out)
    {
        out.put("enabled", this.enabled);
        out.put("contrast", this.contrast);
        out.put("brightness", this.brightness);
        out.put("saturation", this.saturation);
    }

    @Override
    public void readState(Map<String, Object> in)
    {
        if (in.containsKey("enabled")) this.enabled = (boolean) in.get("enabled");
        if (in.containsKey("contrast")) this.contrast = ((Number) in.get("contrast")).floatValue();
        if (in.containsKey("brightness")) this.brightness = ((Number) in.get("brightness")).floatValue();
        if (in.containsKey("saturation")) this.saturation = ((Number) in.get("saturation")).floatValue();
    }
}
