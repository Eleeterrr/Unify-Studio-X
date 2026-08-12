package eleeter.unifystudiox.renderer.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BucketManager
{
    private final Map<RenderBucket, List<RenderCommand>> buckets = new EnumMap<>(RenderBucket.class);
    private final List<RenderCommand> commandPool = new ArrayList<>();
    private int poolIndex = 0;

    public BucketManager()
    {
        for (RenderBucket bucket : RenderBucket.values())
        {
            buckets.put(bucket, new ArrayList<>());
        }
    }

    public RenderCommand allocateCommand()
    {
        if (poolIndex >= commandPool.size())
        {
            commandPool.add(new RenderCommand());
        }
        RenderCommand cmd = commandPool.get(poolIndex++);
        cmd.reset();
        return cmd;
    }

    public void submit(RenderBucket bucket, RenderCommand cmd)
    {
        buckets.get(bucket).add(cmd);
    }

    public List<RenderCommand> getSortedBucket(RenderBucket bucket)
    {
        List<RenderCommand> list = buckets.get(bucket);
        Collections.sort(list);
        return list;
    }

    public void clear()
    {
        for (List<RenderCommand> list : buckets.values())
        {
            list.clear();
        }
        poolIndex = 0;
    }
}
