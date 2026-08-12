package eleeter.unifystudiox.cubic;

import eleeter.unifystudiox.cubic.render.CubeAnimationPlayer;
import eleeter.unifystudiox.cubic.render.CubeRuntimeModel;

public class CubicAnimationController
{
    private final CubeAnimationPlayer player = new CubeAnimationPlayer();
    private String currentAnimation;
    private float animationTime;
    private boolean paused = false;



    public void update(CubeRuntimeModel model, double deltaTime)
    {
        if (this.currentAnimation == null || this.paused)
        {
            return;
        }

        this.animationTime += (float) deltaTime;

        CubicAnimation activeAnim = null;
        for (CubicAnimation anim : model.animations)
        {
            if (anim.name.equals(this.currentAnimation))
            {
                activeAnim = anim;
                break;
            }
        }

        /* Pretty Bad Hardcoded loop mode */
        if (activeAnim != null && activeAnim.length > 0f)
        {
            if ("loop".equals(activeAnim.loopMode))
            {
                while (this.animationTime >= activeAnim.length)
                {
                    this.animationTime -= activeAnim.length;
                }
            } else
            {
                if (this.animationTime > activeAnim.length)
                {
                    this.animationTime = activeAnim.length;
                }
            }
        }

        this.player.apply(model, this.currentAnimation, this.animationTime);
    }

    public void play(String animationName)
    {
        this.currentAnimation = animationName;
        this.animationTime = 0;
    }

    public void setPaused(boolean paused)
    {
        this.paused = paused;
    }

    public boolean isPaused()
    {
        return paused;
    }

    public String getCurrentAnimation()
    {
        return this.currentAnimation;
    }

    public float getAnimationTime()
    {
        return this.animationTime;
    }
}
