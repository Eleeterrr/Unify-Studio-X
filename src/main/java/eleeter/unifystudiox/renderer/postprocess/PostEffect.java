package eleeter.unifystudiox.renderer.postprocess;

import java.util.Map;

public interface PostEffect
{
    void init();
    void apply(int inputTexture, int outputFBO, int width, int height);
    void dispose();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    String getName();
    void writeState(Map<String, Object> out);
    void readState(Map<String, Object> in);
}
