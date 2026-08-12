package eleeter.unifystudiox.anchor;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.scene.entity.Positionable;

public class AnchorAttachment
{
    private final String id;
    private final Positionable payload;
    private final AnchorDefinition definition;

    private final Vector3f tmpPos = new Vector3f();
    private final Quaternionf tmpRot = new Quaternionf();
    private final Vector3f tmpScale = new Vector3f();

    private final Vector3f lastAppliedPos = new Vector3f();
    private final Quaternionf lastAppliedRot = new Quaternionf();

    public AnchorAttachment(String id, Positionable payload, AnchorDefinition definition)
    {
        this.id = id;
        this.payload = payload;
        this.definition = definition;
    }

    public String getId()
    {
        return this.id;
    }

    public Positionable getPayload()
    {
        return this.payload;
    }

    public void update()
    {
        Matrix4f worldMatrix = this.definition.resolveWorldMatrix();
        worldMatrix.getTranslation(this.tmpPos);
        worldMatrix.getUnnormalizedRotation(this.tmpRot);
        worldMatrix.getScale(this.tmpScale);
        
        this.payload.setPosition(this.tmpPos);
        this.payload.setRotation(this.tmpRot);
        this.payload.setScale(this.tmpScale);

        this.lastAppliedPos.set(this.tmpPos);
        this.lastAppliedRot.set(this.tmpRot);
    }

    public void syncOffsetFromPayload()
    {
        Vector3f currentPos = this.payload.getPosition();
        Quaternionf currentRot = this.payload.getRotation();

        if (currentPos.distance(this.lastAppliedPos) > 0.001f || Math.abs(currentRot.difference(this.lastAppliedRot, new Quaternionf()).angle()) > 0.001f)
        {
            Matrix4f pureBoneMatrix = this.definition.resolvePureBoneWorldMatrix();
            Matrix4f pureBoneInverse = pureBoneMatrix.invert(new Matrix4f());

            Matrix4f newPayloadWorld = new Matrix4f().translate(currentPos).rotate(currentRot).scale(this.payload.getScale());

            Matrix4f newOffset = pureBoneInverse.mul(newPayloadWorld);

            newOffset.getTranslation(this.definition.offsetTranslation);
            newOffset.getUnnormalizedRotation(this.definition.offsetRotation);
            newOffset.getScale(this.definition.offsetScale);

            this.lastAppliedPos.set(currentPos);
            this.lastAppliedRot.set(currentRot);
        }
    }
}
