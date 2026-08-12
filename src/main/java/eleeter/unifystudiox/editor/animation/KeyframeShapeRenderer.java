package eleeter.unifystudiox.editor.animation;

import eleeter.unifystudiox.ui.framework.render.UIRenderer;


public class KeyframeShapeRenderer
{
    private KeyframeShapeRenderer()
    {
    }



    public static void drawKeyframeMarker(UIRenderer renderer,
                                          float cx, float cy,
                                          String property,
                                          boolean selected)
    {
        String group = resolveGroup(property);
        float[] color = selected
                ? AnimationEditorTheme.KEYFRAME_SELECTED_COLOR
                : resolveColor(group);

        switch (group)
        {
            case "rotation":
                drawCircle(renderer, cx, cy, 8f, color);
                break;
            case "position":
                drawDiamond(renderer, cx, cy, 9f, color);
                break;
            case "scale":
                drawSquare(renderer, cx, cy, 8f, color);
                break;
            case "pose":
                drawPoseDiamond(renderer, cx, cy, 11f, color);
                break;
            default:
                drawBar(renderer, cx, cy, color);
                break;
        }
    }

    private static void drawCircle(UIRenderer r, float cx, float cy,
                                   float radius, float[] c)
    {
        r.drawRect(cx - radius, cy - radius * 0.55f, radius * 2f, radius * 1.1f, c[0], c[1], c[2], c[3]);
        r.drawRect(cx - radius * 0.55f, cy - radius, radius * 1.1f, radius * 2f, c[0], c[1], c[2], c[3]);
        float d = radius * 0.72f;
        r.drawRect(cx - d, cy - d, d * 2f, d * 2f, c[0], c[1], c[2], c[3] * 0.7f);
    }

    private static void drawDiamond(UIRenderer r, float cx, float cy, float half, float[] c)
    {
        r.drawRect(cx - half, cy - 1.5f, half * 2f, 3f, c[0], c[1], c[2], c[3]);
        r.drawRect(cx - 1.5f, cy - half, 3f, half * 2f, c[0], c[1], c[2], c[3]);
        r.drawRect(cx - half * 0.7f, cy - half * 0.7f, half * 1.4f, half * 1.4f, c[0], c[1], c[2], c[3] * 0.6f);
    }


    private static void drawSquare(UIRenderer r, float cx, float cy, float half, float[] c)
    {
        r.drawRect(cx - half, cy - half, half * 2f, half * 2f, c[0], c[1], c[2], c[3]);
    }

    private static void drawPoseDiamond(UIRenderer r, float cx, float cy, float half, float[] c)
    {
        drawDiamond(r, cx, cy, half, c);
        float ring = half + 3f;
        r.drawRect(cx - ring, cy - 1f, ring * 2f, 2f, c[0], c[1], c[2], c[3] * 0.45f);
        r.drawRect(cx - 1f, cy - ring, 2f, ring * 2f, c[0], c[1], c[2], c[3] * 0.45f);
    }

    private static void drawBar(UIRenderer r, float cx, float cy, float[] c)
    {
        r.drawRect(cx - 1.5f, cy - 5f, 3f, 10f, c[0], c[1], c[2], c[3]);
    }



    public static String resolveGroup(String property)
    {
        if (property == null) return "other";
        String[] parts = property.split(":", -1);
        if (property.startsWith("bone:"))
        {
            int lastColon = property.lastIndexOf(':');
            if (lastColon > 5)
            {
                String comp = property.substring(lastColon + 1);
                if (comp.startsWith("rotation")) return "rotation";
                if (comp.startsWith("position")) return "position";
                if (comp.startsWith("scale")) return "scale";
                if ("pose".equals(comp)) return "pose";
            }
        }
        return "other";
    }

    private static float[] resolveColor(String group)
    {
        switch (group)
        {
            case "rotation":
                return AnimationEditorTheme.TRACK_ROTATION_ACCENT;
            case "position":
                return AnimationEditorTheme.TRACK_POSITION_ACCENT;
            case "scale":
                return AnimationEditorTheme.TRACK_SCALE_ACCENT;
            case "pose":
                return AnimationEditorTheme.TRACK_POSE_ACCENT;
            default:
                return AnimationEditorTheme.KEYFRAME_COLOR;
        }
    }
}
