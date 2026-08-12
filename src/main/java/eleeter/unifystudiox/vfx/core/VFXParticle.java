package eleeter.unifystudiox.vfx.core;

public class VFXParticle
{

    public float x, y, z;

    public float vx, vy, vz;

    public float ax, ay, az;


    public float life;
    public float maxLife;
    public float normalizedLife;

    public float r, g, b, a;
    public float scaleX = 1.0F;
    public float scaleY = 1.0F;
    public float baseScaleX = 1.0F;
    public float baseScaleY = 1.0F;
    public float rotation;
    public float rotSpeed;

    public float u0, v0, u1, v1;
    public int currentFrame;

    public float[] histX;
    public float[] histY;
    public float[] histZ;
    public int histHead;
    public int histLen;

    public float speed;

    public int emitterIndex;

    public boolean alive;
}
