package eleeter.unifystudiox.cubic;

import java.lang.Math;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import eleeter.unifystudiox.cubic.render.CubeRuntimeNode;
import eleeter.unifystudiox.gizmo.Ray;
import eleeter.unifystudiox.scene.SelectionResult;

public class CubicRayPicker
{

    public static SelectionResult pick(CubicModelInstance instance, Ray ray)
    {
        if (instance == null) return SelectionResult.empty();

        Matrix4f baseTransform = instance.getWorldMatrix();

        SelectionResult jointHit = pickJoints(instance, ray, baseTransform);

        SelectionResult elementHit = pickRecursive(instance.getModel().root, ray, instance, baseTransform);

        if (jointHit.hasHit())
        {
            if (!elementHit.hasHit() || jointHit.distance() < elementHit.distance() + 0.5f)
            {
                return jointHit;
            }
        }

        return elementHit;
    }

    private static SelectionResult pickJoints(CubicModelInstance instance, Ray ray, Matrix4f baseTransform)
    {
        float bestT = Float.MAX_VALUE;
        int bestIndex = -1;

        for (int i = 0; i < instance.getModel().flattenedNodes.size(); i++)
        {
            CubeRuntimeNode node = instance.getModel().flattenedNodes.get(i);

            Vector3f pixelPivot = new Vector3f();
            node.worldMatrix.transformPosition(node.pivot, pixelPivot);

            Vector3f worldPivot = new Vector3f();
            baseTransform.transformPosition(pixelPivot, worldPivot);

            float distanceToCamera = new Vector3f(worldPivot).distance(ray.origin());
            float selectionRadius = Math.max(0.06f, 0.008f * distanceToCamera);

            float t = intersectSphere(ray, worldPivot, selectionRadius);
            if (t > 0 && t < bestT)
            {
                bestT = t;
                bestIndex = node.boneIndex;
            }
        }

        if (bestIndex != -1)
        {
            return new SelectionResult(instance, bestT, bestIndex, null);
        }
        return SelectionResult.empty();
    }

    private static float intersectSphere(Ray ray, Vector3f center, float radius)
    {
        Vector3f L = new Vector3f(center).sub(ray.origin());
        float tca = L.dot(ray.direction());

        if (tca < 0)
        {
            return -1;
        }
        float d2 = L.dot(L) - tca * tca;
        if (d2 > radius * radius)
        {
            return -1;
        }
        float thc = (float) Math.sqrt(radius * radius - d2);
        float t0 = tca - thc;
        float t1 = tca + thc;

        if (t0 < 0) t0 = t1;

        if (t0 < 0)
        {
            return -1;
        }
        return t0;
    }

    private static SelectionResult pickRecursive(CubeRuntimeNode node, Ray ray, CubicModelInstance instance, Matrix4f baseTransform)
    {
        SelectionResult closest = SelectionResult.empty();

        Matrix4f fullNodeWorld = new Matrix4f(baseTransform).mul(node.worldMatrix);

        for (CubeElement element : node.elements)
        {
            if (element instanceof CubicElement cubicElement)
            {
                float dist = intersectCube(cubicElement, ray, fullNodeWorld);
                if (dist > 0 && dist < closest.distance())
                {
                    closest = new SelectionResult(instance, dist, node.boneIndex, cubicElement);
                }
            }
        }

        for (CubeRuntimeNode child : node.children)
        {
            SelectionResult hit = pickRecursive(child, ray, instance, baseTransform);
            if (hit.hasHit() && hit.distance() < closest.distance())
            {
                closest = hit;
            }
        }

        return closest;
    }


    public static float intersectCube(CubicElement cube, Ray ray, Matrix4f worldTransform)
    {
        Matrix4f cubeLocal = new Matrix4f().translate(cube.origin).rotateXYZ((float) Math.toRadians(cube.rotation.x), (float) Math.toRadians(cube.rotation.y), (float) Math.toRadians(cube.rotation.z)).translate(-cube.origin.x, -cube.origin.y, -cube.origin.z);

        Matrix4f fullTransform = new Matrix4f(worldTransform).mul(cubeLocal);

        Matrix4f invWorld = fullTransform.invert(new Matrix4f());
        Vector4f localOrigin4 = invWorld.transform(new Vector4f(ray.origin(), 1.0f));
        Vector4f localDir4 = invWorld.transform(new Vector4f(ray.direction(), 0.0f));

        Vector3f localOrigin = new Vector3f(localOrigin4.x, localOrigin4.y, localOrigin4.z);
        Vector3f localDir = new Vector3f(localDir4.x, localDir4.y, localDir4.z);

        return rayAABBIntersect(localOrigin, localDir, cube.from, cube.to);
    }

    private static float rayAABBIntersect(Vector3f rayOrigin, Vector3f rayDir, Vector3f min, Vector3f max)
    {
        float tmin = (min.x - rayOrigin.x) / rayDir.x;
        float tmax = (max.x - rayOrigin.x) / rayDir.x;

        if (tmin > tmax)
        {
            float temp = tmin;
            tmin = tmax;
            tmax = temp;
        }

        float tymin = (min.y - rayOrigin.y) / rayDir.y;
        float tymax = (max.y - rayOrigin.y) / rayDir.y;

        if (tymin > tymax)
        {
            float temp = tymin;
            tymin = tymax;
            tymax = temp;
        }

        if ((tmin > tymax) || (tymin > tmax))
        {
            return -1.0f;
        }

        if (tymin > tmin) tmin = tymin;
        if (tymax < tmax) tmax = tymax;

        float tzmin = (min.z - rayOrigin.z) / rayDir.z;
        float tzmax = (max.z - rayOrigin.z) / rayDir.z;

        if (tzmin > tzmax)
        {
            float temp = tzmin;
            tzmin = tzmax;
            tzmax = temp;
        }

        if ((tmin > tzmax) || (tzmin > tmax))
        {
            return -1.0f;
        }

        if (tzmin > tmin) tmin = tzmin;
        if (tzmax < tmax) tmax = tzmax;

        if (tmax < 0)
        {
            return -1.0f;
        }
        return Math.max(tmin, 0.0f);
    }
}
