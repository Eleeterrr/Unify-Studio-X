package eleeter.unifystudiox.ui.framework.math;

import java.util.ArrayList;
import java.util.List;


public class UIBezierCurve
{

    private float x0 = 0.0F;
    private float y0 = 0.0F;
    private float cx0 = 0.0F;
    private float cy0 = 0.0F;
    private float cx1 = 0.0F;
    private float cy1 = 0.0F;
    private float x1 = 0.0F;
    private float y1 = 0.0F;

    public UIBezierCurve()
    {
    }

    public UIBezierCurve(float x0, float y0, float cx0, float cy0, float cx1, float cy1, float x1, float y1)
    {
        this.set(x0, y0, cx0, cy0, cx1, cy1, x1, y1);
    }

    public void set(float x0, float y0, float cx0, float cy0, float cx1, float cy1, float x1, float y1)
    {
        this.x0 = x0;
        this.y0 = y0;
        this.cx0 = cx0;
        this.cy0 = cy0;
        this.cx1 = cx1;
        this.cy1 = cy1;
        this.x1 = x1;
        this.y1 = y1;
    }

    public float evaluate(float t, float p0, float cp0, float cp1, float p1)
    {
        float u = 1.0F - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        return uuu * p0 + 3.0F * uu * t * cp0 + 3.0F * u * tt * cp1 + ttt * p1;
    }


    public float[] evaluate(float t)
    {
        float rx = this.evaluate(t, this.x0, this.cx0, this.cx1, this.x1);
        float ry = this.evaluate(t, this.y0, this.cy0, this.cy1, this.y1);
        return new float[]{rx, ry};
    }

    public float[] evaluateDerivative(float t)
    {
        float u = 1.0F - t;
        float dx = 3.0F * u * u * (this.cx0 - this.x0) + 6.0F * u * t * (this.cx1 - this.cx0) + 3.0F * t * t * (this.x1 - this.cx1);
        float dy = 3.0F * u * u * (this.cy0 - this.y0) + 6.0F * u * t * (this.cy1 - this.cy0) + 3.0F * t * t * (this.y1 - this.cy1);
        return new float[]{dx, dy};
    }


    public float solveYForX(float targetX)
    {
        if (targetX <= this.x0)
        {
            return this.y0;
        }
        if (targetX >= this.x1)
        {
            return this.y1;
        }

        float rangeX = this.x1 - this.x0;
        if (rangeX <= 0.0001F)
        {
            return this.y0;
        }

        float targetXNorm = (targetX - this.x0) / rangeX;
        float ncx0 = (this.cx0 - this.x0) / rangeX;
        float ncx1 = (this.cx1 - this.x0) / rangeX;

        float t = targetXNorm;
        for (int i = 0; i < 8; i++)
        {
            float currentX = 3.0F * (1.0F - t) * (1.0F - t) * t * ncx0 + 3.0F * (1.0F - t) * t * t * ncx1 + t * t * t * 1.0F - targetXNorm;

            if (Math.abs(currentX) < 0.0001F)
            {
                break;
            }

            float dx = 3.0F * (1.0F - t) * (1.0F - t) * ncx0 + 6.0F * (1.0F - t) * t * (ncx1 - ncx0) + 3.0F * t * t * (1.0F - ncx1);

            if (Math.abs(dx) < 0.0001F)
            {
                break;
            }
            t -= currentX / dx;
        }

        if (t < 0.0F || t > 1.0F)
        {
            float low = 0.0F;
            float high = 1.0F;
            t = 0.5F;
            for (int i = 0; i < 16; i++)
            {
                float currentX = 3.0F * (1.0F - t) * (1.0F - t) * t * ncx0 + 3.0F * (1.0F - t) * t * t * ncx1 + t * t * t * 1.0F;

                if (Math.abs(currentX - targetXNorm) < 0.0001F)
                {
                    break;
                }
                if (currentX < targetXNorm)
                {
                    low = t;
                } else
                {
                    high = t;
                }
                t = (low + high) * 0.5F;
            }
        }

        return this.evaluate(t, this.y0, this.cy0, this.cy1, this.y1);
    }


    public List<float[]> getSubdividedPoints(float tolerance)
    {
        List<float[]> points = new ArrayList<>();
        points.add(new float[]{this.x0, this.y0});
        this.subdivideRecursive(this.x0, this.y0, this.cx0, this.cy0, this.cx1, this.cy1, this.x1, this.y1, tolerance, points);
        points.add(new float[]{this.x1, this.y1});
        return points;
    }

    private void subdivideRecursive(float x0, float y0, float cx0, float cy0, float cx1, float cy1, float x1, float y1, float tolerance, List<float[]> outPoints)
    {
        float dx = x1 - x0;
        float dy = y1 - y0;

        float d1 = Math.abs((cx0 - x1) * dy - (cy0 - y1) * dx);
        float d2 = Math.abs((cx1 - x1) * dy - (cy1 - y1) * dx);

        if ((d1 + d2) * (d1 + d2) < tolerance * tolerance * (dx * dx + dy * dy))
        {
            return;
        }

        float x01 = (x0 + cx0) * 0.5F;
        float y01 = (y0 + cy0) * 0.5F;
        float x12 = (cx0 + cx1) * 0.5F;
        float y12 = (cy0 + cy1) * 0.5F;
        float x23 = (cx1 + x1) * 0.5F;
        float y23 = (cy1 + y1) * 0.5F;

        float x012 = (x01 + x12) * 0.5F;
        float y012 = (y01 + y12) * 0.5F;
        float x123 = (x12 + x23) * 0.5F;
        float y123 = (y12 + y23) * 0.5F;

        float xm = (x012 + x123) * 0.5F;
        float ym = (y012 + y123) * 0.5F;

        this.subdivideRecursive(x0, y0, x01, y01, x012, y012, xm, ym, tolerance, outPoints);
        outPoints.add(new float[]{xm, ym});
        this.subdivideRecursive(xm, ym, x123, y123, x23, y23, x1, y1, tolerance, outPoints);
    }
}
