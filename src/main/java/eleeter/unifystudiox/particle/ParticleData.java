package eleeter.unifystudiox.particle;

public class ParticleData
{
    /* Position */
    public float positionX;
    public float positionY;
    public float positionZ;

    /* Velocity */
    public float velocityX;
    public float velocityY;
    public float velocityZ;

    /* Color */
    public float colorR;
    public float colorG;
    public float colorB;
    public float colorEndR;
    public float colorEndG;
    public float colorEndB;

    /* Alpha */
    public float alpha;

    /* Size */
    public float size;

    /* Rotation around the billboard normal in radians */
    public float rotation;
    public float rotationVelocity;

    /* Lifetime */
    public float life;
    public float maxLife;

    /* Sprite sheet animation */
    public int frame;
    public float frameTimer;

    /* Pool membership flag */
    public boolean isAlive;

    /** Resets all fields soo this slot can be reused by the pool */
    public void reset()
    {
        this.positionX = 0.0F;
        this.positionY = 0.0F;
        this.positionZ = 0.0F;
        this.velocityX = 0.0F;
        this.velocityY = 0.0F;
        this.velocityZ = 0.0F;
        this.colorR = 1.0F;
        this.colorG = 1.0F;
        this.colorB = 1.0F;
        this.colorEndR = 1.0F;
        this.colorEndG = 1.0F;
        this.colorEndB = 1.0F;
        this.alpha = 1.0F;
        this.size = 1.0F;
        this.rotation = 0.0F;
        this.rotationVelocity = 0.0F;
        this.life = 0.0F;
        this.maxLife = 1.0F;
        this.frame = 0;
        this.frameTimer = 0.0F;
        this.isAlive = false;
    }
}
