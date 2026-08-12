package eleeter.unifystudiox.vfx.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VFXEffect
{

    private final List<VFXEmitter> emitters;

    private boolean finished = false;

    public VFXEffect(VFXEmitter... emitters)
    {
        this.emitters = new ArrayList<>(Arrays.asList(emitters));
    }


    public void update(float dt, float worldX, float worldY, float worldZ)
    {
        for (VFXEmitter emitter : this.emitters)
        {
            emitter.update(dt, worldX, worldY, worldZ);
        }
    }

    public List<VFXEmitter> getEmitters()
    {
        return Collections.unmodifiableList(this.emitters);
    }

    public boolean isFinished()
    {
        return this.finished;
    }

    public void markFinished()
    {
        this.finished = true;
    }
}
