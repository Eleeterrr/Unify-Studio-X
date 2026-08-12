package eleeter.unifystudiox.amb;

import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.gizmo.Ray;
import eleeter.unifystudiox.scene.SelectionResult;


public class AmbBonePicker
{

    public static SelectionResult pick(Ray ray, AmbModelInstance instance)
    {
        if (instance == null)
        {
            return SelectionResult.empty();
        }

        Matrix4f invModel = new Matrix4f(instance.getModelMatrix()).invert();
        
        Vector3f localOrigin = new Vector3f();
        invModel.transformPosition(ray.origin(), localOrigin);
        
        Vector3f localDir = new Vector3f();
        invModel.transformDirection(ray.direction(), localDir).normalize();
        
        float closestT = Float.MAX_VALUE;
        int bestBoneIndex = -1;
        boolean hit = false;
        
        /* Cache matrices */
        Matrix4f[] boneMats = new Matrix4f[instance.sourceModel.skeleton.bones.size()];
        for (int i = 0; i < boneMats.length; i++)
        {
            boneMats[i] = new Matrix4f();
            boneMats[i].set(instance.boneMatrices, i * 16);
        }

        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f tempPos = new Vector3f();
        
        for (AmbMesh mesh : instance.sourceModel.meshes)
        {
            float[] v = mesh.vertexData;
            int[] idx = mesh.indices;
            int stride = 16;
            
            for (int i = 0; i < idx.length; i += 3)
            {
                int i0 = idx[i] * stride;
                int i1 = idx[i+1] * stride;
                int i2 = idx[i+2] * stride;
                
                getAnimatedPosition(v, i0, boneMats, v0, tempPos);
                getAnimatedPosition(v, i1, boneMats, v1, tempPos);
                getAnimatedPosition(v, i2, boneMats, v2, tempPos);
                
                float t = Intersectionf.intersectRayTriangle(localOrigin, localDir, v0, v1, v2, 1e-5f);
                if (t >= 0.0f && t < closestT)
                {
                    closestT = t;
                    hit = true;
                    bestBoneIndex = getDominantBone(v, i0, i1, i2);
                }
            }
        }

        if (hit)
        {
            Vector3f localHitPoint = new Vector3f(localDir).mul(closestT).add(localOrigin);
            Vector3f worldHitPoint = new Vector3f();
            instance.getModelMatrix().transformPosition(localHitPoint, worldHitPoint);
            float worldT = worldHitPoint.distance(ray.origin());
            return new SelectionResult(instance, worldT, bestBoneIndex, null);
        }

        return SelectionResult.empty();
    }

    private static void getAnimatedPosition(float[] v, int offset, Matrix4f[] boneMats, Vector3f dest, Vector3f tempPos)
    {
        float px = v[offset];
        float py = v[offset + 1];
        float pz = v[offset + 2];
        
        dest.set(0, 0, 0);
        float totalWeight = 0;
        
        for (int j = 0; j < 4; j++)
        {
            float weight = v[offset + 12 + j];
            if (weight > 0)
            {
                int boneIdx = (int) v[offset + 8 + j];
                if (boneIdx >= 0 && boneIdx < boneMats.length)
                {
                    tempPos.set(px, py, pz);
                    boneMats[boneIdx].transformPosition(tempPos);
                    tempPos.mul(weight);
                    dest.add(tempPos);
                    totalWeight += weight;
                }
            }
        }
        
        if (totalWeight == 0)
        {
            dest.set(px, py, pz);
        }
    }

    private static int getDominantBone(float[] v, int i0, int i1, int i2)
    {
        int bestBone = -1;
        float maxWeight = -1;
        
        for (int b = 0; b < 12; b++)
        {
            int vertexOffset = (b < 4) ? i0 : (b < 8 ? i1 : i2);
            int weightOffset = vertexOffset + 12 + (b % 4);
            int indexOffset = vertexOffset + 8 + (b % 4);
            
            float weight = v[weightOffset];
            if (weight > 0)
            {
                int boneIdx = (int) v[indexOffset];
                float totalWeight = 0;
                
                for (int k = 0; k < 12; k++)
                {
                    int vo2 = (k < 4) ? i0 : (k < 8 ? i1 : i2);
                    if ((int) v[vo2 + 8 + (k % 4)] == boneIdx)
                    {
                        totalWeight += v[vo2 + 12 + (k % 4)];
                    }
                }
                
                if (totalWeight > maxWeight)
                {
                    maxWeight = totalWeight;
                    bestBone = boneIdx;
                }
            }
        }
        
        return bestBone;
    }
}
