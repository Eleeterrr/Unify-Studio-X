package eleeter.unifystudiox.particle.pool;

import eleeter.unifystudiox.particle.ParticleData;

public class ParticlePool
{
    private final ParticleData[] slots;
    private int nextHint;


    public ParticlePool(int capacity)
    {
        this.slots = new ParticleData[capacity];
        this.nextHint = 0;

        for (int i = 0; i < capacity; i++)
        {
            this.slots[i] = new ParticleData();
        }
    }

    public ParticleData acquire()
    {
        int capacity = this.slots.length;

        for (int i = 0; i < capacity; i++)
        {
            int index = (this.nextHint + i) % capacity;
            ParticleData slot = this.slots[index];

            if (!slot.isAlive)
            {
                slot.reset();
                slot.isAlive = true;
                this.nextHint = (index + 1) % capacity;
                return slot;
            }
        }

        return null;
    }


    public ParticleData[] all()
    {
        return this.slots;
    }

    public int getCapacity()
    {
        return this.slots.length;
    }
}
