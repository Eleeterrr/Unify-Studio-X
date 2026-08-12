package eleeter.unifystudiox.vfx.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class VFXManager
{

    private final List<VFXEffect> activeEffects = new ArrayList<>();
    private final List<ActiveSlot> slots = new ArrayList<>();

    /**
     * Internal record that pairs an effect with its world spawn position.
     */
    private static final class ActiveSlot
    {
        VFXEffect effect;
        float worldX;
        float worldY;
        float worldZ;

        ActiveSlot(VFXEffect effect, float x, float y, float z)
        {
            this.effect = effect;
            this.worldX = x;
            this.worldY = y;
            this.worldZ = z;
        }
    }


    public void spawn(VFXEffect effect, float x, float y, float z)
    {
        this.slots.add(new ActiveSlot(effect, x, y, z));
        this.activeEffects.add(effect);
    }


    public void update(float dt)
    {
        Iterator<ActiveSlot> it = this.slots.iterator();
        while (it.hasNext())
        {
            ActiveSlot slot = it.next();

            if (slot.effect.isFinished())
            {
                this.activeEffects.remove(slot.effect);
                it.remove();
                continue;
            }

            slot.effect.update(dt, slot.worldX, slot.worldY, slot.worldZ);
        }
    }


    public void setPosition(VFXEffect effect, float x, float y, float z)
    {
        for (ActiveSlot slot : this.slots)
        {
            if (slot.effect == effect)
            {
                slot.worldX = x;
                slot.worldY = y;
                slot.worldZ = z;
                return;
            }
        }
    }


    public List<VFXEffect> getActiveEffects()
    {
        return Collections.unmodifiableList(this.activeEffects);
    }

    /**
     * Removes all active effects immediately .
     */
    public void clear()
    {
        this.slots.clear();
        this.activeEffects.clear();
    }
}
