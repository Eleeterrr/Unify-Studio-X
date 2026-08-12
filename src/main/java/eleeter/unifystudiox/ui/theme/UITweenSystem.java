package eleeter.unifystudiox.ui.theme;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import eleeter.unifystudiox.ui.framework.UIElement;


public class UITweenSystem
{
    private static final List<Tween> ACTIVE_TWEENS = new ArrayList<>();

    private UITweenSystem()
    {
    }

    public static void register(UIElement owner, String key, Supplier<Float> getter, Consumer<Float> setter, float target, float speed)
    {
        UITweenSystem.setTarget(owner, key, target); 
        
        boolean found = false;
        for (Tween t : UITweenSystem.ACTIVE_TWEENS)
        {
            if (t.owner == owner && t.key.equals(key))
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            UITweenSystem.ACTIVE_TWEENS.add(new Tween(owner, key, getter, setter, target, speed));
        }
    }

    public static void setTarget(UIElement owner, String key, float newTarget)
    {
        for (Tween t : UITweenSystem.ACTIVE_TWEENS)
        {
            if (t.owner == owner && t.key.equals(key))
            {
                t.target = newTarget;
                return;
            }
        }
    }

    public static void update(double deltaTime)
    {
        Iterator<Tween> it = UITweenSystem.ACTIVE_TWEENS.iterator();
        while (it.hasNext())
        {
            Tween t = it.next();
            
            float current = t.getter.get();
            if (Math.abs(current - t.target) < 0.001F)
            {
                t.setter.accept(t.target);
                continue;
            }

            float next = current + (t.target - current) * (float) (1.0D - Math.pow(0.001D, deltaTime * (double) t.speed));
            t.setter.accept(next);
        }
    }

    private static class Tween
    {
        final UIElement owner;
        final String key;
        final Supplier<Float> getter;
        final Consumer<Float> setter;
        float target;
        float speed;

        Tween(UIElement owner, String key, Supplier<Float> getter, Consumer<Float> setter, float target, float speed)
        {
            this.owner = owner;
            this.key = key;
            this.getter = getter;
            this.setter = setter;
            this.target = target;
            this.speed = speed;
        }
    }
}
