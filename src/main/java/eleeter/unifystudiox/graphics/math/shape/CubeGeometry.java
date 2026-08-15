package eleeter.unifystudiox.graphics.math.shape;

import eleeter.unifystudiox.graphics.math.GeometryBuilder;

public class CubeGeometry implements GeometryShape
{
    public enum Format
    {
        POSITION,
        POSITION_NORMAL_UV,
        POSITION_UNINDEXED
    }

    private final float width;
    private final float height;
    private final float depth;
    private final Format format;

    public CubeGeometry(float width, float height, float depth)
    {
        this(width, height, depth, Format.POSITION_NORMAL_UV);
    }

    public CubeGeometry(float width, float height, float depth, Format format)
    {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.format = format;
    }

    /*
     *       4--------5
     *      /|       /|
     *     / |      / |
     *    7--------6  |
     *    |  0------|--1
     *    | /       | /
     *    |/        |/
     *    3---------2
     */
    @Override
    public void generate(GeometryBuilder builder)
    {
        float x = this.width * 0.5F;
        float y = this.height * 0.5F;
        float z = this.depth * 0.5F;

        if (this.format == Format.POSITION_UNINDEXED)
        {
            this.addPositionUnindexed(builder, x, y, z);
            return;
        }

        /*
         * Positions can be shared between faces here.
         */
        if (this.format == Format.POSITION)
        {
            this.addPositionFace(builder, -x, -y, z, x, -y, z, x, y, z, -x, y, z);
            this.addPositionFace(builder, x, -y, -z, -x, -y, -z, -x, y, -z, x, y, -z);
            this.addPositionFace(builder, -x, -y, -z, -x, -y, z, -x, y, z, -x, y, -z);
            this.addPositionFace(builder, x, -y, z, x, -y, -z, x, y, -z, x, y, z);
            this.addPositionFace(builder, -x, y, z, x, y, z, x, y, -z, -x, y, -z);
            this.addPositionFace(builder, -x, -y, -z, x, -y, -z, x, -y, z, -x, -y, z);

            return;
        }

        this.addFace(builder, -x, -y, z, x, -y, z, x, y, z, -x, y, z, 0, 0, 1);
        this.addFace(builder, x, -y, -z, -x, -y, -z, -x, y, -z, x, y, -z, 0, 0, -1);
        this.addFace(builder, -x, -y, -z, -x, -y, z, -x, y, z, -x, y, -z, -1, 0, 0);
        this.addFace(builder, x, -y, z, x, -y, -z, x, y, -z, x, y, z, 1, 0, 0);
        this.addFace(builder, -x, y, z, x, y, z, x, y, -z, -x, y, -z, 0, 1, 0);
        this.addFace(builder, -x, -y, -z, x, -y, -z, x, -y, z, -x, -y, z, 0, -1, 0);
    }

    private void addPositionUnindexed(GeometryBuilder builder, float x, float y, float z)
    {
        builder.vertex(-x,  y, -z);
        builder.vertex(-x, -y, -z);
        builder.vertex( x, -y, -z);

        builder.vertex( x, -y, -z);
        builder.vertex( x,  y, -z);
        builder.vertex(-x,  y, -z);

        builder.vertex(-x, -y,  z);
        builder.vertex(-x, -y, -z);
        builder.vertex(-x,  y, -z);

        builder.vertex(-x,  y, -z);
        builder.vertex(-x,  y,  z);
        builder.vertex(-x, -y,  z);

        builder.vertex( x, -y, -z);
        builder.vertex( x, -y,  z);
        builder.vertex( x,  y,  z);

        builder.vertex( x,  y,  z);
        builder.vertex( x,  y, -z);
        builder.vertex( x, -y, -z);

        builder.vertex(-x, -y,  z);
        builder.vertex( x, -y,  z);
        builder.vertex( x,  y,  z);

        builder.vertex( x,  y,  z);
        builder.vertex(-x,  y,  z);
        builder.vertex(-x, -y,  z);

        builder.vertex(-x,  y, -z);
        builder.vertex( x,  y, -z);
        builder.vertex( x,  y,  z);

        builder.vertex( x,  y,  z);
        builder.vertex(-x,  y,  z);
        builder.vertex(-x,  y, -z);

        builder.vertex(-x, -y, -z);
        builder.vertex(-x, -y,  z);
        builder.vertex( x, -y, -z);

        builder.vertex( x, -y, -z);
        builder.vertex(-x, -y,  z);
        builder.vertex( x, -y,  z);
    }

    private void addPositionFace(GeometryBuilder builder, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3)
    {
        int a = builder.vertex(x0, y0, z0);
        int b = builder.vertex(x1, y1, z1);
        int c = builder.vertex(x2, y2, z2);
        int d = builder.vertex(x3, y3, z3);

        builder.quad(a, b, c, d);
    }

    private void addFace(GeometryBuilder builder, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float nx, float ny, float nz)
    {
        int a = builder.vertex(x0, y0, z0, nx, ny, nz, 0.0F, 0.0F);
        int b = builder.vertex(x1, y1, z1, nx, ny, nz, 1.0F, 0.0F);
        int c = builder.vertex(x2, y2, z2, nx, ny, nz, 1.0F, 1.0F);
        int d = builder.vertex(x3, y3, z3, nx, ny, nz, 0.0F, 1.0F);

        builder.quad(a, b, c, d);
    }
}