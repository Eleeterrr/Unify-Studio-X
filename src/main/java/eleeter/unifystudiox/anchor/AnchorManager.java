package eleeter.unifystudiox.anchor;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnchorManager
{
    private final Map<String, AnchorAttachment> registry = new LinkedHashMap<>();

    public void register(AnchorAttachment attachment)
    {
        this.registry.put(attachment.getId(), attachment);
    }

    public void unregister(String id)
    {
        this.registry.remove(id);
    }

    public AnchorAttachment get(String id)
    {
        return this.registry.get(id);
    }

    public void updateAll()
    {
        for (AnchorAttachment attachment : this.registry.values())
        {
            attachment.update();
        }
    }

    public void syncOffsetsFromPayloads()
    {
        for (AnchorAttachment attachment : this.registry.values())
        {
            attachment.syncOffsetFromPayload();
        }
    }

    public void clear()
    {
        this.registry.clear();
    }
}
