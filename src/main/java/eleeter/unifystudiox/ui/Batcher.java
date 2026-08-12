package eleeter.unifystudiox.ui;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import eleeter.unifystudiox.graphics.TransformStack;

public class Batcher
{

    private static final float MATHEMATICAL_PI = 3.14159265F;
    private static final float MATHEMATICAL_TWO_PI = 6.28318530F;
    private static final float MATHEMATICAL_HALF_PI = 1.57079632F;
    private static final float MINIMUM_LINE_LENGTH = 0.00001F;


    private final List<Float> vertices = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();
    private final TransformStack matrixStack;

    private float currentRed = 1.0F;
    private float currentGreen = 1.0F;
    private float currentBlue = 1.0F;
    private float currentAlpha = 1.0F;

    private float currentNormalX = 0.0F;
    private float currentNormalY = 0.0F;
    private float currentNormalZ = 1.0F;

    private float currentTextureU = 0.0F;
    private float currentTextureV = 0.0F;

    private int vertexCount = 0;

    /**
     * Initializes the UI geometry batcher with a base identity matrix.
     */
    public Batcher(TransformStack matrixStack)
    {
        this.matrixStack = matrixStack;
    }


    /**
     * Pushes the current matrix onto the transform stack.
     */
    public void push()
    {
        this.matrixStack.push();
    }

    /**
     * Pops the current matrix from the transform stack.
     */
    public void pop()
    {
        this.matrixStack.pop();
    }

    /**
     * Reset the top matrix to identity.
     */
    public void loadIdentity()
    {
        this.matrixStack.identity();
    }

    /**
     * Applies a 2D translation to the active matrix stack.
     */
    public void translate(float x, float y)
    {
        this.matrixStack.translate(x, y, 0.0F);
    }

    /**
     * Applies a rotation around the Z-axis to the active matrix stack.
     */
    public void rotate(float angleRadians)
    {
        this.matrixStack.rotate(angleRadians, 0.0F, 0.0F, 1.0F);
    }

    /**
     * Applies a 2D scale transformation to the active matrix stack.
     */
    public void scale(float scaleX, float scaleY)
    {
        this.matrixStack.scale(scaleX, scaleY, 1.0F);
    }

    /**
     * Applies a uniform scale factor to the active matrix stack.
     */
    public void scale(float factor)
    {
        this.matrixStack.scale(factor, factor, 1.0F);
    }

    /**
     * Sets the active drawing color.
     */
    public void color(float red, float green, float blue, float alpha)
    {
        this.currentRed = red;
        this.currentGreen = green;
        this.currentBlue = blue;
        this.currentAlpha = alpha;
    }

    /**
     * Sets the active drawing normal.
     */
    public void normal(float x, float y, float z)
    {
        this.currentNormalX = x;
        this.currentNormalY = y;
        this.currentNormalZ = z;
    }

    /**
     * Sets the active drawing texture coordinates.
     */
    public void texCoords(float u, float v)
    {
        this.currentTextureU = u;
        this.currentTextureV = v;
    }

    /**
     * Submits a single transformed vertex combining active states.
     */
    public void vertex(float positionX, float positionY)
    {
        Matrix4f activeMatrix = this.matrixStack.peek();

        Vector3f transformedPosition = new Vector3f(positionX, positionY, 0.0F);
        transformedPosition.mulPosition(activeMatrix);

        Vector3f transformedNormal = new Vector3f(this.currentNormalX, this.currentNormalY, this.currentNormalZ);
        transformedNormal.mulDirection(activeMatrix);
        transformedNormal.normalize();

        this.vertices.add(transformedPosition.x);
        this.vertices.add(transformedPosition.y);
        this.vertices.add(transformedPosition.z);

        this.vertices.add(transformedNormal.x);
        this.vertices.add(transformedNormal.y);
        this.vertices.add(transformedNormal.z);

        this.vertices.add(this.currentRed);
        this.vertices.add(this.currentGreen);
        this.vertices.add(this.currentBlue);
        this.vertices.add(this.currentAlpha);

        this.vertices.add(this.currentTextureU);
        this.vertices.add(this.currentTextureV);

        this.vertexCount++;
    }

    /**
     * Submits an absolute index value.
     */
    public void index(int indexValue)
    {
        this.indices.add(indexValue);
    }

    /**
     * Submits an offset index relative to the active mesh structure.
     */
    public void indexOffset(int offset)
    {
        this.indices.add(this.vertexCount + offset);
    }

    /**
     * Draws a flat solid 2D rectangle.
     */
    public void drawRectangle(float x, float y, float width, float height)
    {
        int initialVertex = this.vertexCount;

        this.normal(0.0F, 0.0F, 1.0F);

        this.texCoords(0.0F, 0.0F);
        this.vertex(x, y);

        this.texCoords(1.0F, 0.0F);
        this.vertex(x + width, y);

        this.texCoords(1.0F, 1.0F);
        this.vertex(x + width, y + height);

        this.texCoords(0.0F, 1.0F);
        this.vertex(x, y + height);

        this.index(initialVertex);
        this.index(initialVertex + 1);
        this.index(initialVertex + 2);

        this.index(initialVertex);
        this.index(initialVertex + 2);
        this.index(initialVertex + 3);
    }

    /**
     * Draws a flat 2D rectangle filled with a linear gradient.
     */
    public void drawGradientRectangle(float x, float y, float width, float height, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2, boolean isVertical)
    {
        int initialVertex = this.vertexCount;
        this.normal(0.0F, 0.0F, 1.0F);

        if (isVertical)
        {
            this.color(r1, g1, b1, a1);
            this.texCoords(0.0F, 0.0F);
            this.vertex(x, y);

            this.texCoords(1.0F, 0.0F);
            this.vertex(x + width, y);

            this.color(r2, g2, b2, a2);
            this.texCoords(1.0F, 1.0F);
            this.vertex(x + width, y + height);

            this.texCoords(0.0F, 1.0F);
            this.vertex(x, y + height);
        } else
        {
            this.color(r1, g1, b1, a1);
            this.texCoords(0.0F, 0.0F);
            this.vertex(x, y);

            this.color(r2, g2, b2, a2);
            this.texCoords(1.0F, 0.0F);
            this.vertex(x + width, y);

            this.texCoords(1.0F, 1.0F);
            this.vertex(x + width, y + height);

            this.color(r1, g1, b1, a1);
            this.texCoords(0.0F, 1.0F);
            this.vertex(x, y + height);
        }

        this.index(initialVertex);
        this.index(initialVertex + 1);
        this.index(initialVertex + 2);

        this.index(initialVertex);
        this.index(initialVertex + 2);
        this.index(initialVertex + 3);
    }

    /**
     * Draws a flat solid 2D line.
     */
    public void drawLine(float startX, float startY, float endX, float endY, float thickness)
    {
        float dx = endX - startX;
        float dy = endY - startY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length < MINIMUM_LINE_LENGTH)
        {
            return;
        }

        float nx = -dy / length * (thickness * 0.5F);
        float ny = dx / length * (thickness * 0.5F);

        int initialVertex = this.vertexCount;
        this.normal(0.0F, 0.0F, 1.0F);

        this.texCoords(0.0F, 0.0F);
        this.vertex(startX - nx, startY - ny);

        this.texCoords(1.0F, 0.0F);
        this.vertex(endX - nx, endY - ny);

        this.texCoords(1.0F, 1.0F);
        this.vertex(endX + nx, endY + ny);

        this.texCoords(0.0F, 1.0F);
        this.vertex(startX + nx, startY + ny);

        this.index(initialVertex);
        this.index(initialVertex + 1);
        this.index(initialVertex + 2);

        this.index(initialVertex);
        this.index(initialVertex + 2);
        this.index(initialVertex + 3);
    }

    /**
     * Draws a flat solid 2D border outline.
     */
    public void drawBorder(float x, float y, float width, float height, float thickness)
    {
        float halfThickness = thickness * 0.5F;
        this.drawLine(x - halfThickness, y, x + width + halfThickness, y, thickness);
        this.drawLine(x + width, y + halfThickness, x + width, y + height - halfThickness, thickness);
        this.drawLine(x + width + halfThickness, y + height, x - halfThickness, y + height, thickness);
        this.drawLine(x, y + height - halfThickness, x, y + halfThickness, thickness);
    }

    /**
     * Draws a 2D flat grid overlay.
     */
    public void drawGrid(float x, float y, float width, float height, int columns, int rows, float thickness)
    {
        float colStep = width / columns;
        for (int i = 0; i <= columns; i++)
        {
            this.drawLine(x + i * colStep, y, x + i * colStep, y + height, thickness);
        }

        float rowStep = height / rows;
        for (int j = 0; j <= rows; j++)
        {
            this.drawLine(x, y + j * rowStep, x + width, y + j * rowStep, thickness);
        }
    }

    /**
     * Draws a flat solid 2D circle.
     */
    public void drawCircle(float centerX, float centerY, float radius, int segments)
    {
        int centerVertex = this.vertexCount;
        this.normal(0.0F, 0.0F, 1.0F);
        this.texCoords(0.5F, 0.5F);
        this.vertex(centerX, centerY);

        int startVertex = this.vertexCount;

        for (int i = 0; i <= segments; i++)
        {
            float angle = MATHEMATICAL_TWO_PI * i / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            this.texCoords(cos * 0.5F + 0.5F, sin * 0.5F + 0.5F);
            this.vertex(centerX + cos * radius, centerY + sin * radius);
        }

        for (int i = 0; i < segments; i++)
        {
            this.index(centerVertex);
            this.index(startVertex + i);
            this.index(startVertex + i + 1);
        }
    }

    /**
     * Draws a flat 2D circle border outline.
     */
    public void drawCircleBorder(float centerX, float centerY, float radius, float thickness, int segments)
    {
        int startVertex = this.vertexCount;
        float innerRadius = radius - thickness * 0.5F;
        float outerRadius = radius + thickness * 0.5F;

        this.normal(0.0F, 0.0F, 1.0F);

        for (int i = 0; i <= segments; i++)
        {
            float angle = MATHEMATICAL_TWO_PI * i / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            this.texCoords(0.0F, (float) i / segments);
            this.vertex(centerX + cos * innerRadius, centerY + sin * innerRadius);

            this.texCoords(1.0F, (float) i / segments);
            this.vertex(centerX + cos * outerRadius, centerY + sin * outerRadius);
        }

        for (int i = 0; i < segments; i++)
        {
            int first = startVertex + i * 2;
            int second = first + 2;

            this.index(first);
            this.index(second);
            this.index(first + 1);

            this.index(second);
            this.index(second + 1);
            this.index(first + 1);
        }
    }

    /**
     * Draws a flat solid 2D ellipse.
     */
    public void drawEllipse(float centerX, float centerY, float radiusX, float radiusY, int segments)
    {
        int centerVertex = this.vertexCount;
        this.normal(0.0F, 0.0F, 1.0F);
        this.texCoords(0.5F, 0.5F);
        this.vertex(centerX, centerY);

        int startVertex = this.vertexCount;

        for (int i = 0; i <= segments; i++)
        {
            float angle = MATHEMATICAL_TWO_PI * i / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            this.texCoords(cos * 0.5F + 0.5F, sin * 0.5F + 0.5F);
            this.vertex(centerX + cos * radiusX, centerY + sin * radiusY);
        }

        for (int i = 0; i < segments; i++)
        {
            this.index(centerVertex);
            this.index(startVertex + i);
            this.index(startVertex + i + 1);
        }
    }

    /**
     * Draws a flat 2D ellipse border outline.
     */
    public void drawEllipseBorder(float centerX, float centerY, float radiusX, float radiusY, float thickness, int segments)
    {
        int startVertex = this.vertexCount;
        float halfThickness = thickness * 0.5F;

        this.normal(0.0F, 0.0F, 1.0F);

        for (int i = 0; i <= segments; i++)
        {
            float angle = MATHEMATICAL_TWO_PI * i / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float innerX = cos * (radiusX - halfThickness);
            float innerY = sin * (radiusY - halfThickness);
            float outerX = cos * (radiusX + halfThickness);
            float outerY = sin * (radiusY + halfThickness);

            this.texCoords(0.0F, (float) i / segments);
            this.vertex(centerX + innerX, centerY + innerY);

            this.texCoords(1.0F, (float) i / segments);
            this.vertex(centerX + outerX, centerY + outerY);
        }

        for (int i = 0; i < segments; i++)
        {
            int first = startVertex + i * 2;
            int second = first + 2;

            this.index(first);
            this.index(second);
            this.index(first + 1);

            this.index(second);
            this.index(second + 1);
            this.index(first + 1);
        }
    }

    /**
     * Draws a solid flat 2D pie wedge or arc segment.
     */
    public void drawPie(float centerX, float centerY, float radius, float startAngle, float endAngle, int segments)
    {
        int centerVertex = this.vertexCount;
        this.normal(0.0F, 0.0F, 1.0F);
        this.texCoords(0.5F, 0.5F);
        this.vertex(centerX, centerY);

        int startVertex = this.vertexCount;

        for (int i = 0; i <= segments; i++)
        {
            float angle = startAngle + (endAngle - startAngle) * i / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            this.texCoords(cos * 0.5F + 0.5F, sin * 0.5F + 0.5F);
            this.vertex(centerX + cos * radius, centerY + sin * radius);
        }

        for (int i = 0; i < segments; i++)
        {
            this.index(centerVertex);
            this.index(startVertex + i);
            this.index(startVertex + i + 1);
        }
    }

    /**
     * Draws a solid 2D flat rounded rectangle.
     */
    public void drawRoundedRectangle(float x, float y, float width, float height, float radius, int segments)
    {
        int centerVertex = this.vertexCount;

        this.normal(0.0F, 0.0F, 1.0F);
        this.texCoords(0.5F, 0.5F);
        this.vertex(x + width * 0.5F, y + height * 0.5F);

        float cornerRadius = Math.min(radius, Math.min(width * 0.5F, height * 0.5F));

        int firstVertex = this.vertexCount;

        this.emitCornerArc(x + width - cornerRadius, y + cornerRadius, cornerRadius, 0.0F, MATHEMATICAL_HALF_PI, segments);

        this.emitCornerArc(x + cornerRadius, y + cornerRadius, cornerRadius, MATHEMATICAL_HALF_PI, MATHEMATICAL_PI, segments);

        this.emitCornerArc(x + cornerRadius, y + height - cornerRadius, cornerRadius, MATHEMATICAL_PI, MATHEMATICAL_PI + MATHEMATICAL_HALF_PI, segments);

        this.emitCornerArc(x + width - cornerRadius, y + height - cornerRadius, cornerRadius, MATHEMATICAL_PI + MATHEMATICAL_HALF_PI, MATHEMATICAL_TWO_PI, segments);

        int endVertex = this.vertexCount;
        int count = endVertex - firstVertex;

        for (int i = 0; i < count; i++)
        {
            int current = firstVertex + i;
            int next = firstVertex + ((i + 1) % count);

            this.index(centerVertex);
            this.index(current);
            this.index(next);
        }
    }

    private void emitCornerArc(float centerX, float centerY, float radius, float startAngle, float endAngle, int segments)
    {
        for (int i = 0; i <= segments; i++)
        {
            float angle = startAngle + (endAngle - startAngle) * i / segments;
            float px = centerX + (float) Math.cos(angle) * radius;
            float py = centerY - (float) Math.sin(angle) * radius;

            this.texCoords(0.0F, 0.0F);
            this.vertex(px, py);
        }
    }

    /**
     * Draws a flat rounded border outline.
     */
    public void drawRoundedBorder(float x, float y, float width, float height, float radius, float thickness, int segments)
    {
        float halfThickness = thickness * 0.5F;
        float cornerRadius = Math.min(radius, Math.min(width * 0.5F, height * 0.5F));

        int startVertex = this.vertexCount;

        this.emitRoundedBorderArc(x + width - cornerRadius, y + cornerRadius, cornerRadius, 0.0F, MATHEMATICAL_HALF_PI, halfThickness, segments);

        this.emitRoundedBorderArc(x + cornerRadius, y + cornerRadius, cornerRadius, MATHEMATICAL_HALF_PI, MATHEMATICAL_PI, halfThickness, segments);

        this.emitRoundedBorderArc(x + cornerRadius, y + height - cornerRadius, cornerRadius, MATHEMATICAL_PI, MATHEMATICAL_PI + MATHEMATICAL_HALF_PI, halfThickness, segments);

        this.emitRoundedBorderArc(x + width - cornerRadius, y + height - cornerRadius, cornerRadius, MATHEMATICAL_PI + MATHEMATICAL_HALF_PI, MATHEMATICAL_TWO_PI, halfThickness, segments);

        int endVertex = this.vertexCount;
        int count = (endVertex - startVertex) / 2;

        for (int i = 0; i < count; i++)
        {
            int currentInner = startVertex + i * 2;
            int currentOuter = currentInner + 1;
            int nextInner = startVertex + ((i + 1) % count) * 2;
            int nextOuter = nextInner + 1;

            this.index(currentInner);
            this.index(nextInner);
            this.index(currentOuter);

            this.index(nextInner);
            this.index(nextOuter);
            this.index(currentOuter);
        }
    }

    private void emitRoundedBorderArc(float centerX, float centerY, float radius, float startAngle, float endAngle, float halfThickness, int segments)
    {
        float innerRadius = radius - halfThickness;
        float outerRadius = radius + halfThickness;

        for (int i = 0; i <= segments; i++)
        {
            float angle = startAngle + (endAngle - startAngle) * i / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float ix = centerX + cos * innerRadius;
            float iy = centerY - sin * innerRadius;
            float ox = centerX + cos * outerRadius;
            float oy = centerY - sin * outerRadius;

            this.texCoords(0.0F, 0.0F);
            this.vertex(ix, iy);

            this.texCoords(1.0F, 0.0F);
            this.vertex(ox, oy);
        }
    }

    /**
     * Draws a flat 2D star shape.
     */
    public void drawStar(float centerX, float centerY, float innerRadius, float outerRadius, int points)
    {
        int centerVertex = this.vertexCount;
        this.normal(0.0F, 0.0F, 1.0F);
        this.texCoords(0.5F, 0.5F);
        this.vertex(centerX, centerY);

        int startVertex = this.vertexCount;
        int totalVertices = points * 2;

        for (int i = 0; i <= totalVertices; i++)
        {
            float angle = MATHEMATICAL_TWO_PI * i / totalVertices - MATHEMATICAL_HALF_PI;
            float currentRadius = (i % 2 == 0) ? outerRadius : innerRadius;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            this.texCoords(cos * 0.5F + 0.5F, sin * 0.5F + 0.5F);
            this.vertex(centerX + cos * currentRadius, centerY + sin * currentRadius);
        }

        for (int i = 0; i < totalVertices; i++)
        {
            this.index(centerVertex);
            this.index(startVertex + i);
            this.index(startVertex + i + 1);
        }
    }

    /**
     * Draws a flat solid 2D vector arrow.
     */
    public void drawArrow2D(float startX, float startY, float endX, float endY, float headWidth, float headLength, float shaftThickness)
    {
        float dx = endX - startX;
        float dy = endY - startY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length < MINIMUM_LINE_LENGTH)
        {
            return;
        }

        float ux = dx / length;
        float uy = dy / length;

        float actualHeadLength = Math.min(headLength, length * 0.75F);
        float shaftLength = length - actualHeadLength;

        float shaftEndX = startX + ux * shaftLength;
        float shaftEndY = startY + uy * shaftLength;

        this.drawLine(startX, startY, shaftEndX, shaftEndY, shaftThickness);

        float rx = -uy * (headWidth * 0.5F);
        float ry = ux * (headWidth * 0.5F);

        int headBase = this.vertexCount;
        this.normal(0.0F, 0.0F, 1.0F);

        this.texCoords(0.5F, 0.0F);
        this.vertex(shaftEndX + rx, shaftEndY + ry);

        this.texCoords(0.5F, 1.0F);
        this.vertex(shaftEndX - rx, shaftEndY - ry);

        this.texCoords(1.0F, 0.5F);
        this.vertex(endX, endY);

        this.index(headBase);
        this.index(headBase + 1);
        this.index(headBase + 2);
    }

    /**
     * Draws a 2D UI dropping shadow box around a rectangle.
     */
    public void drawShadowRectangle(float x, float y, float width, float height, float shadowSize, float shadowIntensity)
    {
        int baseIndex = this.vertexCount;
        this.normal(0.0F, 0.0F, 1.0F);

        float innerLeft = x;
        float innerRight = x + width;
        float innerTop = y;
        float innerBottom = y + height;

        float outerLeft = x - shadowSize;
        float outerRight = x + width + shadowSize;
        float outerTop = y - shadowSize;
        float outerBottom = y + height + shadowSize;

        float r = this.currentRed;
        float g = this.currentGreen;
        float b = this.currentBlue;

        this.color(r, g, b, shadowIntensity);
        this.vertex(innerLeft, innerTop);
        this.vertex(innerRight, innerTop);
        this.vertex(innerRight, innerBottom);
        this.vertex(innerLeft, innerBottom);

        this.color(r, g, b, 0.0F);
        this.vertex(outerLeft, outerTop);
        this.vertex(outerRight, outerTop);
        this.vertex(outerRight, outerBottom);
        this.vertex(outerLeft, outerBottom);

        int innerTL = baseIndex;
        int innerTR = baseIndex + 1;
        int innerBR = baseIndex + 2;
        int innerBL = baseIndex + 3;

        int outerTL = baseIndex + 4;
        int outerTR = baseIndex + 5;
        int outerBR = baseIndex + 6;
        int outerBL = baseIndex + 7;

        this.emitQuad(outerTL, innerTL, innerTR, outerTR);

        this.emitQuad(innerTR, innerBR, outerBR, outerTR);

        this.emitQuad(innerBL, outerBL, outerBR, innerBR);

        this.emitQuad(outerTL, outerBL, innerBL, innerTL);
    }

    private void emitQuad(int v1, int v2, int v3, int v4)
    {
        this.index(v1);
        this.index(v2);
        this.index(v3);

        this.index(v1);
        this.index(v3);
        this.index(v4);
    }

    /**
     * Draws a 2D UI drop shadow glow around a circle.
     */
    public void drawShadowCircle(float centerX, float centerY, float radius, float shadowSize, float shadowIntensity, int segments)
    {
        int startVertex = this.vertexCount;
        float outerRadius = radius + shadowSize;

        float r = this.currentRed;
        float g = this.currentGreen;
        float b = this.currentBlue;

        this.normal(0.0F, 0.0F, 1.0F);

        for (int i = 0; i <= segments; i++)
        {
            float angle = MATHEMATICAL_TWO_PI * i / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            this.color(r, g, b, shadowIntensity);
            this.texCoords(0.5F, 0.5F);
            this.vertex(centerX + cos * radius, centerY + sin * radius);

            this.color(r, g, b, 0.0F);
            this.texCoords(0.5F, 0.5F);
            this.vertex(centerX + cos * outerRadius, centerY + sin * outerRadius);
        }

        for (int i = 0; i < segments; i++)
        {
            int first = startVertex + i * 2;
            int second = first + 2;

            this.index(first);
            this.index(second);
            this.index(first + 1);

            this.index(second);
            this.index(second + 1);
            this.index(first + 1);
        }
    }

    /**
     * Draws a 2D UI drop shadow glow around a rounded rectangle.
     */
    public void drawShadowRoundedRectangle(float x, float y, float width, float height, float radius, float shadowSize, float shadowIntensity, int segments)
    {
        int startVertex = this.vertexCount;
        float cornerRadius = Math.min(radius, Math.min(width * 0.5F, height * 0.5F));

        float r = this.currentRed;
        float g = this.currentGreen;
        float b = this.currentBlue;

        this.normal(0.0F, 0.0F, 1.0F);

        this.emitRoundedShadowArc(x + width - cornerRadius, y + cornerRadius, cornerRadius, shadowSize, 0.0F, MATHEMATICAL_HALF_PI, r, g, b, shadowIntensity, segments);

        this.emitRoundedShadowArc(x + cornerRadius, y + cornerRadius, cornerRadius, shadowSize, MATHEMATICAL_HALF_PI, MATHEMATICAL_PI, r, g, b, shadowIntensity, segments);

        this.emitRoundedShadowArc(x + cornerRadius, y + height - cornerRadius, cornerRadius, shadowSize, MATHEMATICAL_PI, MATHEMATICAL_PI + MATHEMATICAL_HALF_PI, r, g, b, shadowIntensity, segments);

        this.emitRoundedShadowArc(x + width - cornerRadius, y + height - cornerRadius, cornerRadius, shadowSize, MATHEMATICAL_PI + MATHEMATICAL_HALF_PI, MATHEMATICAL_TWO_PI, r, g, b, shadowIntensity, segments);

        int endVertex = this.vertexCount;
        int count = (endVertex - startVertex) / 2;

        for (int i = 0; i < count; i++)
        {
            int currentInner = startVertex + i * 2;
            int currentOuter = currentInner + 1;
            int nextInner = startVertex + ((i + 1) % count) * 2;
            int nextOuter = nextInner + 1;

            this.index(currentInner);
            this.index(nextInner);
            this.index(currentOuter);

            this.index(nextInner);
            this.index(nextOuter);
            this.index(currentOuter);
        }
    }

    private void emitRoundedShadowArc(float centerX, float centerY, float radius, float shadowSize, float startAngle, float endAngle, float r, float g, float b, float shadowIntensity, int segments)
    {
        float outerRadius = radius + shadowSize;

        for (int i = 0; i <= segments; i++)
        {
            float angle = startAngle + (endAngle - startAngle) * i / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float ix = centerX + cos * radius;
            float iy = centerY - sin * radius;
            float ox = centerX + cos * outerRadius;
            float oy = centerY - sin * outerRadius;

            this.color(r, g, b, shadowIntensity);
            this.texCoords(0.0F, 0.0F);
            this.vertex(ix, iy);

            this.color(r, g, b, 0.0F);
            this.texCoords(1.0F, 0.0F);
            this.vertex(ox, oy);
        }
    }

    /**
     * Resets the compiled geometric buffers and stack state.
     */
    public void reset()
    {
        this.vertices.clear();
        this.indices.clear();
        this.vertexCount = 0;
        while (this.matrixStack.getDepth() > 0)
        {
            this.matrixStack.pop();
        }
        this.matrixStack.identity();

        this.currentRed = 1.0F;
        this.currentGreen = 1.0F;
        this.currentBlue = 1.0F;
        this.currentAlpha = 1.0F;

        this.currentNormalX = 0.0F;
        this.currentNormalY = 0.0F;
        this.currentNormalZ = 1.0F;

        this.currentTextureU = 0.0F;
        this.currentTextureV = 0.0F;
    }

    /**
     * Returns a copy of compiled vertices as a float array.
     */
    public float[] getVertexData()
    {
        float[] array = new float[this.vertices.size()];
        for (int i = 0; i < this.vertices.size(); i++)
        {
            array[i] = this.vertices.get(i);
        }
        return array;
    }

    /**
     * Returns a copy of compiled indices as an int array.
     */
    public int[] getIndexData()
    {
        int[] array = new int[this.indices.size()];
        for (int i = 0; i < this.indices.size(); i++)
        {
            array[i] = this.indices.get(i);
        }
        return array;
    }

    /**
     * Gets the total compiled vertices count.
     */
    public int getVertexCount()
    {
        return this.vertexCount;
    }

    /**
     * Gets the total compiled indices count.
     */
    public int getIndexCount()
    {
        return this.indices.size();
    }
}
