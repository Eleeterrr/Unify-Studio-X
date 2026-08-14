package eleeter.unifystudiox.ui.framework.layout;


import java.util.ArrayList;
import java.util.List;

public class GridLayout
{
    public static int computeColumns(float availableWidth, float tileSize, float padding)
    {
        return Math.max(1, (int) Math.floor(availableWidth / (tileSize + padding)));
    }

    public static List<TileSlot> layout(int itemCount, int columns, float tileW, float tileH, float padding, float startY)
    {
        List<TileSlot> slots = new ArrayList<>(itemCount);
        for (int i = 0; i < itemCount; i++)
        {
            int col = i % columns, row = i / columns;
            float x = padding + col * (tileW + padding);
            float y = startY + row * (tileH + padding);
            slots.add(new TileSlot(x, y));
        }
        return slots;
    }

    public static float rowsHeight(int itemCount, int columns, float tileH, float padding)
    {
        int rows = itemCount == 0 ? 0 : (itemCount + columns - 1) / columns;
        return rows * (tileH + padding);
    }

    public record TileSlot(float x, float y)
    {
    }
}