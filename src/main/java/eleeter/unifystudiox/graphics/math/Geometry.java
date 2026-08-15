package eleeter.unifystudiox.graphics.math;

import eleeter.unifystudiox.graphics.math.shape.CubeGeometry;

public class Geometry
{
    private Geometry()
    {
    }

    public static GeometryData cube()
    {
        return cube(1.0F, 1.0F, 1.0F);
    }

    public static GeometryData cube(float width, float height, float depth)
    {
        GeometryBuilder builder = new GeometryBuilder(8);

        new CubeGeometry(width, height, depth, CubeGeometry.Format.POSITION_NORMAL_UV).generate(builder);

        return builder.build();
    }

    public static GeometryData cubePosition()
    {
        return cubePosition(1.0F, 1.0F, 1.0F);
    }

    public static GeometryData cubePosition(float width, float height, float depth)
    {
        GeometryBuilder builder = new GeometryBuilder(3);

        new CubeGeometry(width, height, depth, CubeGeometry.Format.POSITION).generate(builder);

        return builder.build();
    }

    public static GeometryData cubePositionUnindexed()
    {
        return cubePositionUnindexed(1.0F, 1.0F, 1.0F);
    }

    public static GeometryData cubePositionUnindexed(float width, float height, float depth)
    {
        GeometryBuilder builder = new GeometryBuilder(3);

        new CubeGeometry(width, height, depth, CubeGeometry.Format.POSITION_UNINDEXED).generate(builder);

        return builder.build();
    }
}