package eleeter.unifystudiox.graphics.layout;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;

public enum AttributeType
{

    FLOAT(GL_FLOAT, 4, false),

    INT(GL_INT, 4, true),

    UNSIGNED_INT(GL_UNSIGNED_INT, 4, true),

    SHORT(GL_SHORT, 2, true),

    UNSIGNED_SHORT(GL_UNSIGNED_SHORT, 2, true),


    UNSIGNED_BYTE(GL_UNSIGNED_BYTE, 1, true);


    public final int glType;

    public final int bytes;


    public final boolean isInteger;


    AttributeType(int glType, int bytes, boolean isInteger)
    {
        this.glType = glType;
        this.bytes = bytes;
        this.isInteger = isInteger;
    }
}
