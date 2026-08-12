package eleeter.unifystudiox.ui;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.TransformStack;

public class ShapeDraw
{
    private static final float PI = 3.14159265F;
    private static final float TWO_PI = 6.28318530F;
    private static final float HALF_PI = 1.57079632F;
    private static final float MIN_LEN = 0.00001F;
    private static final int MIN_SEG = 12;
    private static final int MAX_SEG = 48;

    private final List<Float> vertices = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();
    private final TransformStack matrixStack;
    private int vertexCount = 0;

    public ShapeDraw(TransformStack matrixStack)
    {
        this.matrixStack = matrixStack;
    }

    public List<Float> getVertices()
    {
        return vertices;
    }

    public List<Integer> getIndices()
    {
        return indices;
    }

    public void clear()
    {
        vertices.clear();
        indices.clear();
        vertexCount = 0;
    }

    public void push()
    {
        matrixStack.push();
    }

    public void pop()
    {
        matrixStack.pop();
    }

    public void loadIdentity()
    {
        matrixStack.identity();
    }

    public void translate(float x, float y)
    {
        matrixStack.translate(x, y, 0);
    }

    public void rotate(float rad)
    {
        matrixStack.rotate(rad, 0, 0, 1);
    }

    public void scale(float sx, float sy)
    {
        matrixStack.scale(sx, sy, 1);
    }

    public void scale(float f)
    {
        matrixStack.scale(f, f, 1);
    }

    public void vertex(float x, float y)
    {
        Matrix4f m = matrixStack.peek();
        Vector3f v = new Vector3f(x, y, 0).mulPosition(m);
        vertices.add(snap(v.x));
        vertices.add(snap(v.y));
        vertexCount++;
    }

    public void index(int i)
    {
        indices.add(i);
    }

    public void indexOffset(int o)
    {
        indices.add(vertexCount + o);
    }

    private static float snap(float v)
    {
        return (float) Math.floor(v) + 0.5F;
    }

    private static float normThick(float t)
    {
        return Math.max(1F, Math.round(t * 2F) * 0.5F);
    }

    private static float snapStroke(float v, float t)
    {
        int px = Math.max(1, Math.round(normThick(t)));
        return (px & 1) == 0 ? Math.round(v) : snap(v);
    }

    private static int adaptSeg(float r, float span, int min)
    {
        return Math.max(min, Math.min(MAX_SEG,
                (int) Math.ceil(Math.max(r, 1F) * Math.max(Math.abs(span), HALF_PI) * 1.25F)));
    }

    private void rect(float x, float y, float w, float h)
    {
        int b = vertexCount;
        vertex(x, y);
        vertex(x + w, y);
        vertex(x + w, y + h);
        vertex(x, y + h);
        index(b);
        index(b + 1);
        index(b + 2);
        index(b);
        index(b + 2);
        index(b + 3);
    }

    private void line(float x1, float y1, float x2, float y2, float t)
    {
        t = normThick(t);
        x1 = snapStroke(x1, t);
        y1 = snapStroke(y1, t);
        x2 = snapStroke(x2, t);
        y2 = snapStroke(y2, t);
        float dx = x2 - x1, dy = y2 - y1, len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < MIN_LEN) return;
        float nx = -dy / len * (t * 0.5F), ny = dx / len * (t * 0.5F);
        int b = vertexCount;
        vertex(x1 - nx, y1 - ny);
        vertex(x2 - nx, y2 - ny);
        vertex(x2 + nx, y2 + ny);
        vertex(x1 + nx, y1 + ny);
        index(b);
        index(b + 1);
        index(b + 2);
        index(b);
        index(b + 2);
        index(b + 3);
    }

    private void arc(float cx, float cy, float r, float a0, float a1, float t, int seg)
    {
        t = normThick(t);
        cx = snapStroke(cx, t);
        cy = snapStroke(cy, t);
        r = Math.max(t * 0.5F + 0.5F, Math.round(r * 2F) * 0.5F);
        seg = adaptSeg(r, a1 - a0, Math.max(MIN_SEG, seg));
        float ri = r - t * 0.5F, ro = r + t * 0.5F;
        int b = vertexCount;
        for (int i = 0; i <= seg; i++)
        {
            float a = (float) (a0 + (a1 - a0) * (double) i / seg);
            float c = (float) Math.cos(a), s = (float) Math.sin(a);
            vertex(cx + c * ri, cy + s * ri);
            vertex(cx + c * ro, cy + s * ro);
        }
        for (int i = 0; i < seg; i++)
        {
            int f = b + i * 2;
            index(f);
            index(f + 2);
            index(f + 1);
            index(f + 2);
            index(f + 3);
            index(f + 1);
        }
    }

    private void ring(float cx, float cy, float ri, float ro, int seg)
    {
        cx = snap(cx);
        cy = snap(cy);
        ri = Math.max(0.5F, Math.round(ri * 2F) * 0.5F);
        ro = Math.max(ri + 0.5F, Math.round(ro * 2F) * 0.5F);
        seg = adaptSeg(ro, TWO_PI, Math.max(MIN_SEG, seg));
        int b = vertexCount;
        for (int i = 0; i <= seg; i++)
        {
            float a = TWO_PI * i / seg, c = (float) Math.cos(a), s = (float) Math.sin(a);
            vertex(cx + c * ri, cy + s * ri);
            vertex(cx + c * ro, cy + s * ro);
        }
        for (int i = 0; i < seg; i++)
        {
            int f = b + i * 2;
            index(f);
            index(f + 2);
            index(f + 1);
            index(f + 2);
            index(f + 3);
            index(f + 1);
        }
    }

    private void polygon(float cx, float cy, float r, int sides)
    {
        int ctr = vertexCount;
        vertex(cx, cy);
        int st = vertexCount;
        for (int i = 0; i <= sides; i++)
        {
            float a = TWO_PI * i / sides;
            vertex(cx + (float) Math.cos(a) * r, cy + (float) Math.sin(a) * r);
        }
        for (int i = 0; i < sides; i++)
        {
            index(ctr);
            index(st + i);
            index(st + i + 1);
        }
    }

    public void drawPolygon(float cx, float cy, float r, int s)
    {
        polygon(cx, cy, r, s);
    }

    public void drawRing(float cx, float cy, float ri, float ro, int s)
    {
        ring(cx, cy, ri, ro, s);
    }

    public void drawArc(float cx, float cy, float r, float a0, float a1, float t, int s)
    {
        arc(cx, cy, r, a0, a1, t, s);
    }

    public void drawStar(float cx, float cy, float ir, float or, int pts)
    {
        int ctr = vertexCount;
        vertex(cx, cy);
        int st = vertexCount;
        int tot = pts * 2;
        for (int i = 0; i <= tot; i++)
        {
            float a = TWO_PI * i / tot - HALF_PI;
            float r = i % 2 == 0 ? or : ir;
            vertex(cx + (float) Math.cos(a) * r, cy + (float) Math.sin(a) * r);
        }
        for (int i = 0; i < tot; i++)
        {
            index(ctr);
            index(st + i);
            index(st + i + 1);
        }
    }

    public void drawHeart(float cx, float cy, float size, int seg)
    {
        int ctr = vertexCount;
        vertex(cx, cy);
        int st = vertexCount;
        for (int i = 0; i <= seg; i++)
        {
            float t = TWO_PI * i / seg;
            float x = 16 * (float) Math.pow(Math.sin(t), 3);
            float y = -(13 * (float) Math.cos(t) - 5 * (float) Math.cos(2 * t) - 2 * (float) Math.cos(3 * t) - (float) Math.cos(4 * t));
            vertex(cx + x * size * 0.05F, cy + y * size * 0.05F);
        }
        for (int i = 0; i < seg; i++)
        {
            index(ctr);
            index(st + i);
            index(st + i + 1);
        }
    }

    public void drawBezier(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2, float t, int seg)
    {
        float px = x1, py = y1;
        for (int i = 1; i <= seg; i++)
        {
            float f = (float) i / seg, u = 1 - f, nx = (u * u * u) * x1 + 3 * (u * u) * f * cx1 + 3 * u * (f * f) * cx2 + (f * f * f) * x2, ny = (u * u * u) * y1 + 3 * (u * u) * f * cy1 + 3 * u * (f * f) * cy2 + (f * f * f) * y2;
            line(px, py, nx, ny, t);
            px = nx;
            py = ny;
        }
    }

    public void drawWave(float cx, float cy, float w, float amp, float freq, float t, int seg)
    {
        float px = cx - w * 0.5F, py = cy;
        for (int i = 1; i <= seg; i++)
        {
            float x = cx - w * 0.5F + w * i / seg, y = cy + (float) Math.sin(freq * i / seg * TWO_PI) * amp;
            line(px, py, x, y, t);
            px = x;
            py = y;
        }
    }

    public void drawSpiral(float cx, float cy, float maxR, float coils, float t, int seg)
    {
        float px = cx, py = cy;
        for (int i = 1; i <= seg; i++)
        {
            float f = (float) i / seg, a = coils * TWO_PI * f, r = maxR * f, x = cx + (float) Math.cos(a) * r, y = cy + (float) Math.sin(a) * r;
            line(px, py, x, y, t);
            px = x;
            py = y;
        }
    }

    public void drawGrid(float cx, float cy, float size, int cells, float t)
    {
        float step = size / cells, h = size * 0.5F;
        for (int i = 0; i <= cells; i++)
        {
            float p = -h + i * step;
            line(cx + p, cy - h, cx + p, cy + h, t);
            line(cx - h, cy + p, cx + h, cy + p, t);
        }
    }

    public void drawCube(float cx, float cy, float size, float t)
    {
        float h = size * 0.5F, d = size * 0.25F;
        line(cx - h, cy - h, cx + h, cy - h, t);
        line(cx + h, cy - h, cx + h, cy + h, t);
        line(cx + h, cy + h, cx - h, cy + h, t);
        line(cx - h, cy + h, cx - h, cy - h, t);
        line(cx - h + d, cy - h - d, cx + h + d, cy - h - d, t);
        line(cx + h + d, cy - h - d, cx + h + d, cy + h - d, t);
        line(cx + h + d, cy + h - d, cx - h + d, cy + h - d, t);
        line(cx - h + d, cy + h - d, cx - h + d, cy - h - d, t);
        line(cx - h, cy - h, cx - h + d, cy - h - d, t);
        line(cx + h, cy - h, cx + h + d, cy - h - d, t);
        line(cx + h, cy + h, cx + h + d, cy + h - d, t);
        line(cx - h, cy + h, cx - h + d, cy + h - d, t);
    }

    public void drawGizmoArrows(float cx, float cy, float len, float t)
    {
        line(cx, cy, cx, cy - len, t);
        polygon(cx, cy - len, t * 3, 3);
        line(cx, cy, cx + len, cy, t);
        polygon(cx + len, cy, t * 3, 3);
        line(cx, cy, cx - len * 0.5F, cy + len * 0.5F, t);
        polygon(cx - len * 0.5F, cy + len * 0.5F, t * 3, 3);
    }

    /**
     * Two overlapping rectangles offset top-right
     */
    public void drawCopyIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.42F;
        float o = size * 0.18F;
        line(cx - h + o, cy - h - o, cx + h + o, cy - h - o, t);
        line(cx + h + o, cy - h - o, cx + h + o, cy + h - o, t);
        line(cx + h + o, cy + h - o, cx - h + o, cy + h - o, t);
        line(cx - h + o, cy + h - o, cx - h + o, cy - h - o, t);
        line(cx - h, cy - h, cx + h, cy - h, t);
        line(cx + h, cy - h, cx + h, cy + h, t);
        line(cx + h, cy + h, cx - h, cy + h, t);
        line(cx - h, cy + h, cx - h, cy - h, t);
    }

    /**
     * Clipboard with a small tab on top
     */
    public void drawPasteIcon(float cx, float cy, float size, float t)
    {
        float bw = size * 0.72F, bh = size * 0.82F;
        float bx = cx - bw * 0.5F, by = cy - bh * 0.5F + size * 0.06F;
        line(bx, by, bx + bw, by, t);
        line(bx + bw, by, bx + bw, by + bh, t);
        line(bx + bw, by + bh, bx, by + bh, t);
        line(bx, by + bh, bx, by, t);
        // tab
        float tw = bw * 0.45F, th = size * 0.18F;
        float tx = cx - tw * 0.5F, ty = by - th;
        line(tx, ty, tx + tw, ty, t);
        line(tx + tw, ty, tx + tw, by, t);
        line(tx, by, tx, ty, t);
    }

    /**
     * Scissors — two diagonal lines with circles at bottom
     */
    public void drawCutIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.38F;
        line(cx, cy, cx - h, cy + h, t);
        line(cx, cy, cx + h, cy + h, t);
        line(cx, cy, cx - h * 0.5F, cy - h, t);
        ring(cx - h, cy + h, h * 0.25F, h * 0.35F, 12);
        ring(cx + h, cy + h, h * 0.25F, h * 0.35F, 12);
    }

    /**
     * Counter-clockwise arc with arrow tip
     */
    public void drawUndoIcon(float cx, float cy, float size, float t)
    {
        float r = size * 0.36F;
        arc(cx, cy, r, HALF_PI * 0.6F, PI + HALF_PI * 0.6F, t, 16);
        // arrow head at start of arc
        float ax = cx + (float) Math.cos(HALF_PI * 0.6F) * r;
        float ay = cy + (float) Math.sin(HALF_PI * 0.6F) * r;
        float as = size * 0.14F;
        line(ax, ay, ax - as, ay - as * 0.4F, t);
        line(ax, ay, ax + as * 0.4F, ay - as, t);
    }

    /**
     * Clockwise arc with arrow tip
     */
    public void drawRedoIcon(float cx, float cy, float size, float t)
    {
        float r = size * 0.36F;
        arc(cx, cy, r, PI - HALF_PI * 0.6F, TWO_PI + HALF_PI * 0.4F, t, 16);
        float ax = cx + (float) Math.cos(PI - HALF_PI * 0.6F) * r;
        float ay = cy + (float) Math.sin(PI - HALF_PI * 0.6F) * r;
        float as = size * 0.14F;
        line(ax, ay, ax + as, ay - as * 0.4F, t);
        line(ax, ay, ax - as * 0.4F, ay - as, t);
    }

    /**
     * Floppy disk
     */
    public void drawSaveIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.44F;
        line(cx - h, cy - h, cx + h, cy - h, t);
        line(cx + h, cy - h, cx + h, cy + h, t);
        line(cx + h, cy + h, cx - h, cy + h, t);
        line(cx - h, cy + h, cx - h, cy - h, t);

        line(cx - h * 0.45F, cy - h, cx + h * 0.65F, cy - h, t);
        line(cx + h * 0.65F, cy - h, cx + h * 0.65F, cy - h * 0.25F, t);
        line(cx + h * 0.65F, cy - h * 0.25F, cx - h * 0.45F, cy - h * 0.25F, t);
        line(cx - h * 0.45F, cy - h * 0.25F, cx - h * 0.45F, cy - h, t);

        line(cx - h * 0.5F, cy + h * 0.15F, cx + h * 0.5F, cy + h * 0.15F, t);
        line(cx + h * 0.5F, cy + h * 0.15F, cx + h * 0.5F, cy + h, t);
        line(cx + h * 0.5F, cy + h, cx - h * 0.5F, cy + h, t);
        line(cx - h * 0.5F, cy + h, cx - h * 0.5F, cy + h * 0.15F, t);
    }

    /**
     * Open folder
     */
    public void drawOpenIcon(float cx, float cy, float size, float t)
    {
        float w = size * 0.82F, h = size * 0.62F;
        float x = cx - w * 0.5F, y = cy - h * 0.5F;
        // tab
        line(x, y + h * 0.32F, x + w * 0.38F, y + h * 0.32F, t);
        line(x + w * 0.38F, y + h * 0.32F, x + w * 0.5F, y, t);
        line(x + w * 0.5F, y, x + w, y, t);
        // body
        line(x + w, y, x + w, y + h, t);
        line(x + w, y + h, x, y + h, t);
        line(x, y + h, x, y + h * 0.32F, t);
    }

    /**
     * Dog-eared page
     */
    public void drawNewFileIcon(float cx, float cy, float size, float t)
    {
        float w = size * 0.64F, h = size * 0.80F, ear = size * 0.20F;
        float x = cx - w * 0.5F, y = cy - h * 0.5F;
        line(x, y, x + w - ear, y, t);
        line(x + w - ear, y, x + w, y + ear, t);
        line(x + w, y + ear, x + w, y + h, t);
        line(x + w, y + h, x, y + h, t);
        line(x, y + h, x, y, t);
        line(x + w - ear, y, x + w - ear, y + ear, t);
        line(x + w - ear, y + ear, x + w, y + ear, t);
    }

    /**
     * Classic trash can
     */
    public void drawTrashIcon(float cx, float cy, float size, float t)
    {
        float w = size * 0.60F, h = size * 0.62F;
        float x = cx - w * 0.5F, y = cy - h * 0.5F + size * 0.06F;
        line(cx - w * 0.62F, y - size * 0.12F, cx + w * 0.62F, y - size * 0.12F, t);
        line(cx - w * 0.24F, y - size * 0.12F, cx - w * 0.24F, y - size * 0.26F, t);
        line(cx - w * 0.24F, y - size * 0.26F, cx + w * 0.24F, y - size * 0.26F, t);
        line(cx + w * 0.24F, y - size * 0.26F, cx + w * 0.24F, y - size * 0.12F, t);
        line(x, y, x + w, y, t);
        line(x + w, y, x + w * 0.88F, y + h, t);
        line(x + w * 0.88F, y + h, x + w * 0.12F, y + h, t);
        line(x + w * 0.12F, y + h, x, y, t);
        line(cx - w * 0.2F, y + h * 0.14F, cx - w * 0.2F, y + h * 0.88F, t);
        line(cx, y + h * 0.14F, cx, y + h * 0.88F, t);
        line(cx + w * 0.2F, y + h * 0.14F, cx + w * 0.2F, y + h * 0.88F, t);
    }

    /**
     * Magnifier
     */
    public void drawSearchIcon(float cx, float cy, float size, float t)
    {
        float r = size * 0.28F;
        float ox = size * 0.08F, oy = size * 0.08F;
        ring(cx - ox, cy - oy, r - t, r, 16);
        float dx = (float) Math.cos(PI * 0.25F) * r, dy = (float) Math.sin(PI * 0.25F) * r;
        line(cx - ox + dx, cy - oy + dy, cx + size * 0.38F, cy + size * 0.38F, t * 1.5F);
    }

    /**
     * Gear
     */
    public void drawSettingsIcon(float cx, float cy, float size, float t)
    {
        ring(cx, cy, size * 0.18F, size * 0.28F, 12);
        for (int i = 0; i < 8; i++)
        {
            float a = TWO_PI * i / 8;
            float c = (float) Math.cos(a), s = (float) Math.sin(a);
            line(cx + c * size * 0.28F, cy + s * size * 0.28F, cx + c * size * 0.44F, cy + s * size * 0.44F, t * 1.8F);
        }
    }

    /**
     * Play triangle
     */
    public void drawPlayIcon(float cx, float cy, float size, float t)
    {
        int b = vertexCount;
        vertex(cx - size * 0.30F, cy - size * 0.38F);
        vertex(cx + size * 0.40F, cy);
        vertex(cx - size * 0.30F, cy + size * 0.38F);
        index(b);
        index(b + 1);
        index(b + 2);
    }

    /**
     * Two vertical bars
     */
    public void drawPauseIcon(float cx, float cy, float size, float t)
    {
        rect(cx - size * 0.28F, cy - size * 0.38F, size * 0.18F, size * 0.76F);
        rect(cx + size * 0.10F, cy - size * 0.38F, size * 0.18F, size * 0.76F);
    }

    /**
     * Filled square
     */
    public void drawStopIcon(float cx, float cy, float size, float t)
    {
        rect(cx - size * 0.30F, cy - size * 0.30F, size * 0.60F, size * 0.60F);
    }

    /**
     * X cross
     */
    public void drawCloseIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.32F;
        line(cx - h, cy - h, cx + h, cy + h, t);
        line(cx - h, cy + h, cx + h, cy - h, t);
    }

    /**
     * Horizontal line
     */
    public void drawMinimizeIcon(float cx, float cy, float size, float t)
    {
        line(cx - size * 0.30F, cy + size * 0.18F, cx + size * 0.30F, cy + size * 0.18F, t);
    }

    /**
     * Empty square
     */
    public void drawMaximizeIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.28F;
        line(cx - h, cy - h, cx + h, cy - h, t);
        line(cx + h, cy - h, cx + h, cy + h, t);
        line(cx + h, cy + h, cx - h, cy + h, t);
        line(cx - h, cy + h, cx - h, cy - h, t);
    }

    /**
     * Two overlapping squares
     */
    public void drawRestoreIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.22F, o = size * 0.12F;
        line(cx - h + o, cy - h - o, cx + h + o, cy - h - o, t);
        line(cx + h + o, cy - h - o, cx + h + o, cy + h - o, t);
        line(cx + h + o, cy + h - o, cx - h + o, cy + h - o, t);
        line(cx - h + o, cy + h - o, cx - h + o, cy - h - o, t);
        line(cx - h - o, cy - h + o, cx + h - o, cy - h + o, t);
        line(cx + h - o, cy - h + o, cx + h - o, cy + h + o, t);
        line(cx + h - o, cy + h + o, cx - h - o, cy + h + o, t);
        line(cx - h - o, cy + h + o, cx - h - o, cy - h + o, t);
    }

    /**
     * House silhouette
     */
    public void drawHomeIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.36F;
        line(cx - h, cy + size * 0.06F, cx, cy - h, t);
        line(cx, cy - h, cx + h, cy + size * 0.06F, t);
        line(cx - h * 0.72F, cy + size * 0.02F, cx - h * 0.72F, cy + h, t);
        line(cx + h * 0.72F, cy + size * 0.02F, cx + h * 0.72F, cy + h, t);
        line(cx - h * 0.72F, cy + h, cx + h * 0.72F, cy + h, t);
        line(cx - h * 0.26F, cy + size * 0.08F, cx - h * 0.26F, cy + h, t);
        line(cx - h * 0.26F, cy + h, cx + h * 0.26F, cy + h, t);
        line(cx + h * 0.26F, cy + h, cx + h * 0.26F, cy + size * 0.08F, t);
        line(cx + h * 0.26F, cy + size * 0.08F, cx - h * 0.26F, cy + size * 0.08F, t);
    }

    /**
     * Left arrow
     */
    public void drawBackIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.28F;
        line(cx + h, cy, cx - h, cy, t);
        line(cx - h, cy, cx, cy - h, t);
        line(cx - h, cy, cx, cy + h, t);
    }

    /**
     * Right arrow
     */
    public void drawForwardIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.28F;
        line(cx - h, cy, cx + h, cy, t);
        line(cx + h, cy, cx, cy - h, t);
        line(cx + h, cy, cx, cy + h, t);
    }

    public void drawArrowUpIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.28F;
        line(cx, cy + h, cx, cy - h, t);
        line(cx, cy - h, cx - h, cy, t);
        line(cx, cy - h, cx + h, cy, t);
    }

    public void drawArrowDownIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.28F;
        line(cx, cy - h, cx, cy + h, t);
        line(cx, cy + h, cx - h, cy, t);
        line(cx, cy + h, cx + h, cy, t);
    }

    public void drawArrowLeftIcon(float cx, float cy, float size, float t)
    {
        drawBackIcon(cx, cy, size, t);
    }

    public void drawArrowRightIcon(float cx, float cy, float size, float t)
    {
        drawForwardIcon(cx, cy, size, t);
    }

    /**
     * Eye
     */
    public void drawEyeIcon(float cx, float cy, float size, float t)
    {
        arc(cx, cy, size * 0.34F, PI * 0.22F, PI * 0.78F, t, 14);
        arc(cx, cy, size * 0.34F, PI + PI * 0.22F, PI * 2 - PI * 0.22F, t, 14);
        ring(cx, cy, size * 0.08F, size * 0.13F, 10);
    }

    /**
     * Eye with slash
     */
    public void drawHideIcon(float cx, float cy, float size, float t)
    {
        drawEyeIcon(cx, cy, size, t);
        line(cx - size * 0.42F, cy - size * 0.32F, cx + size * 0.42F, cy + size * 0.32F, t);
    }

    /**
     * Lock
     */
    public void drawLockIcon(float cx, float cy, float size, float t)
    {
        float bw = size * 0.56F, bh = size * 0.44F, bx = cx - bw * 0.5F, by = cy - size * 0.04F;
        line(bx, by, bx + bw, by, t);
        line(bx + bw, by, bx + bw, by + bh, t);
        line(bx + bw, by + bh, bx, by + bh, t);
        line(bx, by + bh, bx, by, t);
        arc(cx, by, size * 0.22F, PI, TWO_PI, t, 12);
    }

    /**
     * Unlocked lock
     */
    public void drawUnlockIcon(float cx, float cy, float size, float t)
    {
        float bw = size * 0.56F, bh = size * 0.44F, bx = cx - bw * 0.5F, by = cy - size * 0.04F;
        line(bx, by, bx + bw, by, t);
        line(bx + bw, by, bx + bw, by + bh, t);
        line(bx + bw, by + bh, bx, by + bh, t);
        line(bx, by + bh, bx, by, t);
        arc(cx + size * 0.26F, by, size * 0.22F, PI, TWO_PI, t, 12);
    }

    /**
     * Funnel
     */
    public void drawFilterIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.36F;
        line(cx - h, cy - h, cx + h, cy - h, t);
        line(cx + h, cy - h, cx + size * 0.12F, cy, t);
        line(cx - h, cy - h, cx - size * 0.12F, cy, t);
        line(cx - size * 0.12F, cy, cx - size * 0.12F, cy + h, t);
        line(cx + size * 0.12F, cy, cx + size * 0.12F, cy + h, t);
        line(cx - size * 0.12F, cy + h * 0.55F, cx + size * 0.12F, cy + h * 0.55F, t);
    }

    /**
     * Three shrinking lines
     */
    public void drawSortIcon(float cx, float cy, float size, float t)
    {
        line(cx - size * 0.34F, cy - size * 0.22F, cx + size * 0.34F, cy - size * 0.22F, t);
        line(cx - size * 0.22F, cy, cx + size * 0.22F, cy, t);
        line(cx - size * 0.10F, cy + size * 0.22F, cx + size * 0.10F, cy + size * 0.22F, t);
    }

    /**
     * Three horizontal dots
     */
    public void drawDotsIcon(float cx, float cy, float size, float t)
    {
        float r = t * 1.6F;
        polygon(cx - size * 0.30F, cy, r, 8);
        polygon(cx, cy, r, 8);
        polygon(cx + size * 0.30F, cy, r, 8);
    }

    /**
     * Plus
     */
    public void drawPlusIcon(float cx, float cy, float size, float t)
    {
        line(cx - size * 0.32F, cy, cx + size * 0.32F, cy, t);
        line(cx, cy - size * 0.32F, cx, cy + size * 0.32F, t);
    }

    /**
     * Minus
     */
    public void drawMinusIcon(float cx, float cy, float size, float t)
    {
        line(cx - size * 0.32F, cy, cx + size * 0.32F, cy, t);
    }

    /**
     * Checkbox with tick
     */
    public void drawCheckboxIcon(float cx, float cy, float size, float t)
    {
        drawMaximizeIcon(cx, cy, size, t);
        line(cx - size * 0.18F, cy + size * 0.02F, cx - size * 0.04F, cy + size * 0.16F, t);
        line(cx - size * 0.04F, cy + size * 0.16F, cx + size * 0.20F, cy - size * 0.14F, t);
    }

    /**
     * Radio button
     */
    public void drawRadioIcon(float cx, float cy, float size, float t)
    {
        ring(cx, cy, size * 0.28F, size * 0.28F + t, 16);
        polygon(cx, cy, size * 0.13F, 12);
    }

    /**
     * Toggle switch
     */
    public void drawToggleIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.22F;
        arc(cx - h, cy, h, HALF_PI, HALF_PI * 3, t, 8);
        arc(cx + h, cy, h, -HALF_PI, HALF_PI, t, 8);
        line(cx - h, cy - h, cx + h, cy - h, t);
        line(cx - h, cy + h, cx + h, cy + h, t);
        polygon(cx + h, cy, h * 0.55F, 12);
    }

    /**
     * Skip-forward
     */
    public void drawSkipIcon(float cx, float cy, float size, float t)
    {
        drawPlayIcon(cx - size * 0.08F, cy, size, t);
        rect(cx + size * 0.30F, cy - size * 0.38F, size * 0.14F, size * 0.76F);
    }

    /**
     * Resize handle (two diagonal lines)
     */
    public void drawResizeHandleIcon(float cx, float cy, float size, float t)
    {
        line(cx - size * 0.18F, cy + size * 0.38F, cx + size * 0.38F, cy - size * 0.18F, t);
        line(cx + size * 0.10F, cy + size * 0.38F, cx + size * 0.38F, cy + size * 0.10F, t);
    }

    /**
     * Three horizontal grab lines
     */
    public void drawDragHandleIcon(float cx, float cy, float size, float t)
    {
        line(cx - size * 0.30F, cy - size * 0.18F, cx + size * 0.30F, cy - size * 0.18F, t);
        line(cx - size * 0.30F, cy, cx + size * 0.30F, cy, t);
        line(cx - size * 0.30F, cy + size * 0.18F, cx + size * 0.30F, cy + size * 0.18F, t);
    }

    /**
     * Four corner brackets
     */
    public void drawFitScreenIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.38F, d = size * 0.16F;
        line(cx - h, cy - h, cx - h + d, cy - h, t);
        line(cx - h, cy - h, cx - h, cy - h + d, t);
        line(cx + h, cy - h, cx + h - d, cy - h, t);
        line(cx + h, cy - h, cx + h, cy - h + d, t);
        line(cx - h, cy + h, cx - h + d, cy + h, t);
        line(cx - h, cy + h, cx - h, cy + h - d, t);
        line(cx + h, cy + h, cx + h - d, cy + h, t);
        line(cx + h, cy + h, cx + h, cy + h - d, t);
    }

    /**
     * Bold B
     */
    public void drawBoldIcon(float cx, float cy, float size, float t)
    {
        float x = cx - size * 0.14F, y = cy - size * 0.38F;
        line(x, y, x, y + size * 0.76F, t);
        arc(x, y + size * 0.20F, size * 0.20F, -HALF_PI, HALF_PI, t, 8);
        arc(x, y + size * 0.56F, size * 0.24F, -HALF_PI, HALF_PI, t, 8);
    }

    /**
     * Italic I
     */
    public void drawItalicIcon(float cx, float cy, float size, float t)
    {
        line(cx + size * 0.10F, cy - size * 0.38F, cx - size * 0.10F, cy + size * 0.38F, t);
        line(cx - size * 0.18F, cy - size * 0.38F, cx + size * 0.26F, cy - size * 0.38F, t);
        line(cx - size * 0.26F, cy + size * 0.38F, cx + size * 0.18F, cy + size * 0.38F, t);
    }

    /**
     * Underline U
     */
    public void drawUnderlineIcon(float cx, float cy, float size, float t)
    {
        line(cx - size * 0.30F, cy + size * 0.40F, cx + size * 0.30F, cy + size * 0.40F, t);
        arc(cx, cy - size * 0.04F, size * 0.28F, 0, PI, t, 12);
        line(cx - size * 0.28F, cy - size * 0.32F, cx - size * 0.28F, cy - size * 0.04F, t);
        line(cx + size * 0.28F, cy - size * 0.32F, cx + size * 0.28F, cy - size * 0.04F, t);
    }

    /**
     * Magnifier with +
     */
    public void drawZoomInIcon(float cx, float cy, float size, float t)
    {
        float ox = -size * 0.08F, oy = -size * 0.08F, r = size * 0.26F;
        ring(cx + ox, cy + oy, r - t, r, 16);
        float d = (float) Math.cos(PI * 0.25F) * r;
        line(cx + ox + d, cy + oy + d, cx + size * 0.36F, cy + size * 0.36F, t * 1.5F);
        line(cx + ox - size * 0.12F, cy + oy, cx + ox + size * 0.12F, cy + oy, t);
        line(cx + ox, cy + oy - size * 0.12F, cx + ox, cy + oy + size * 0.12F, t);
    }

    /**
     * Magnifier with –
     */
    public void drawZoomOutIcon(float cx, float cy, float size, float t)
    {
        float ox = -size * 0.08F, oy = -size * 0.08F, r = size * 0.26F;
        ring(cx + ox, cy + oy, r - t, r, 16);
        float d = (float) Math.cos(PI * 0.25F) * r;
        line(cx + ox + d, cy + oy + d, cx + size * 0.36F, cy + size * 0.36F, t * 1.5F);
        line(cx + ox - size * 0.12F, cy + oy, cx + ox + size * 0.12F, cy + oy, t);
    }

    /**
     * Folder
     */
    public void drawFolderIcon(float cx, float cy, float size, float t)
    {
        float w = size * 0.80F, h = size * 0.58F;
        float x = cx - w * 0.5F, y = cy - h * 0.5F;
        line(x, y + h * 0.36F, x + w * 0.36F, y + h * 0.36F, t);
        line(x + w * 0.36F, y + h * 0.36F, x + w * 0.46F, y, t);
        line(x + w * 0.46F, y, x + w, y, t);
        line(x + w, y, x + w, y + h, t);
        line(x + w, y + h, x, y + h, t);
        line(x, y + h, x, y + h * 0.36F, t);
    }

    /**
     * Sphere (3D)
     */
    public void drawSphere(float cx, float cy, float r, float t, int seg)
    {
        ring(cx, cy, r - t * 0.5F, r + t * 0.5F, seg);
        arc(cx, cy, r, 0, PI, t, seg / 2);
        int b = vertexCount;
        float ry = r * 0.3F;
        for (int i = 0; i <= seg; i++)
        {
            float a = TWO_PI * i / seg, c = (float) Math.cos(a), s = (float) Math.sin(a);
            vertex(cx + c * (r - t * 0.5F), cy + s * (ry - t * 0.5F));
            vertex(cx + c * (r + t * 0.5F), cy + s * (ry + t * 0.5F));
        }
        for (int i = 0; i < seg; i++)
        {
            int f = b + i * 2;
            index(f);
            index(f + 2);
            index(f + 1);
            index(f + 2);
            index(f + 3);
            index(f + 1);
        }
    }

    public void drawCylinder(float cx, float cy, float r, float height, float t, int seg)
    {
        float h = height * 0.5F;
        arc(cx, cy - h, r, 0, TWO_PI, t, seg);
        arc(cx, cy + h, r, 0, TWO_PI, t, seg);
        line(cx - r, cy - h, cx - r, cy + h, t);
        line(cx + r, cy - h, cx + r, cy + h, t);
    }

    public void drawCone(float cx, float cy, float r, float height, float t, int seg)
    {
        float h = height * 0.5F;
        arc(cx, cy + h, r, 0, TWO_PI, t, seg);
        line(cx, cy - h, cx - r, cy + h, t);
        line(cx, cy - h, cx + r, cy + h, t);
    }

    public void drawTorus(float cx, float cy, float r, float tubeR, float t, int seg)
    {
        ring(cx, cy, r - tubeR, r + tubeR, seg);
        ring(cx, cy, r - t * 0.5F, r + t * 0.5F, seg);
    }

    public void drawCapsule(float cx, float cy, float r, float height, float t, int seg)
    {
        float h = height * 0.5F - r;
        arc(cx, cy - h, r, PI, TWO_PI, t, seg / 2);
        arc(cx, cy + h, r, 0, PI, t, seg / 2);
        line(cx - r, cy - h, cx - r, cy + h, t);
        line(cx + r, cy - h, cx + r, cy + h, t);
    }

    public void drawScrollbarShape(float x, float y, float w, float h, float r)
    {
        arc(x + r, y + r, r, HALF_PI, PI, r, 8);
        arc(x + w - r, y + r, r, 0, HALF_PI, r, 8);
        arc(x + r, y + h - r, r, PI, HALF_PI * 3, r, 8);
        arc(x + w - r, y + h - r, r, -HALF_PI, 0, r, 8);
        rect(x + r, y, w - r * 2, h);
        rect(x, y + r, w, h - r * 2);
    }

    public void drawSpline(float[] pts, float t, int seg)
    {
        for (int i = 0; i < pts.length - 2; i += 2) line(pts[i], pts[i + 1], pts[i + 2], pts[i + 3], t);
    }

    public void drawHelix(float cx, float cy, float w, float h, float coils, float t, int seg)
    {
        float px = cx + w * 0.5F, py = cy - h * 0.5F;
        for (int i = 1; i <= seg; i++)
        {
            float f = (float) i / seg, a = coils * TWO_PI * f, x = cx + (float) Math.cos(a) * w * 0.5F, y = cy - h * 0.5F + h * f;
            line(px, py, x, y, t);
            px = x;
            py = y;
        }
    }

    public void drawPyramid(float cx, float cy, float r, float height, float t)
    {
        float h = height * 0.5F, ry = r * 0.3F;
        line(cx, cy - h, cx - r, cy + h, t);
        line(cx, cy - h, cx + r, cy + h, t);
        line(cx, cy - h, cx, cy + h + ry, t);
        line(cx - r, cy + h, cx, cy + h + ry, t);
        line(cx, cy + h + ry, cx + r, cy + h, t);
        line(cx - r, cy + h, cx + r, cy + h, t);
    }

    public void drawBoundingBox(float cx, float cy, float w, float h, float d, float t)
    {
        drawCube(cx, cy, Math.max(w, h), t);
    }

    public void drawDoubleArrowIcon(float cx, float cy, float size, float t)
    {
        float h = size * 0.26F;
        line(cx - size * 0.08F, cy, cx + size * 0.36F, cy - h, t);
        line(cx - size * 0.08F, cy, cx + size * 0.36F, cy + h, t);
        line(cx - size * 0.36F, cy, cx + size * 0.08F, cy - h, t);
        line(cx - size * 0.36F, cy, cx + size * 0.08F, cy + h, t);
    }
}
