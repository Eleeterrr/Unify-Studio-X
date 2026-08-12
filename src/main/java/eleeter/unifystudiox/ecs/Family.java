package eleeter.unifystudiox.ecs;

import java.util.BitSet;


public class Family
{

    private static int nextIndex = 0;

    private final BitSet all;
    private final BitSet any;
    private final BitSet exclude;
    private final int index;

    /**
     * Creates a builder for a Family.
     */
    @SafeVarargs
    public static Builder all(Class<? extends Component>... componentTypes)
    {
        return new Builder().all(componentTypes);
    }

    private Family(BitSet all, BitSet any, BitSet exclude)
    {
        this.all = all;
        this.any = any;
        this.exclude = exclude;
        this.index = nextIndex;
        nextIndex++;
    }

    public int getIndex()
    {
        return this.index;
    }

    /**
     * Checks if the given entity's component bits match this Family's requirements.
     */
    public boolean matches(BitSet entityComponentBits)
    {
        if (!this.all.isEmpty() && !containsAll(entityComponentBits, this.all))
        {
            return false;
        }
        if (!this.any.isEmpty() && !entityComponentBits.intersects(this.any))
        {
            return false;
        }
        if (!this.exclude.isEmpty() && entityComponentBits.intersects(this.exclude))
        {
            return false;
        }
        return true;
    }

    private boolean containsAll(BitSet bitSet, BitSet required)
    {
        BitSet temp = (BitSet) required.clone();
        temp.andNot(bitSet);
        return temp.isEmpty();
    }

    @Override
    public int hashCode()
    {
        int result = this.all.hashCode();
        result = 31 * result + this.any.hashCode();
        result = 31 * result + this.exclude.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof Family other)) return false;
        return this.all.equals(other.all) && this.any.equals(other.any) && this.exclude.equals(other.exclude);
    }

    public static class Builder
    {
        private final BitSet all = new BitSet();
        private final BitSet any = new BitSet();
        private final BitSet exclude = new BitSet();

        @SafeVarargs
        public final Builder all(Class<? extends Component>... componentTypes)
        {
            for (Class<? extends Component> type : componentTypes)
            {
                this.all.set(ComponentType.getIndexFor(type));
            }
            return this;
        }

        @SafeVarargs
        public final Builder any(Class<? extends Component>... componentTypes)
        {
            for (Class<? extends Component> type : componentTypes)
            {
                this.any.set(ComponentType.getIndexFor(type));
            }
            return this;
        }

        @SafeVarargs
        public final Builder exclude(Class<? extends Component>... componentTypes)
        {
            for (Class<? extends Component> type : componentTypes)
            {
                this.exclude.set(ComponentType.getIndexFor(type));
            }
            return this;
        }

        public Family build()
        {
            return new Family(this.all, this.any, this.exclude);
        }
    }
}
