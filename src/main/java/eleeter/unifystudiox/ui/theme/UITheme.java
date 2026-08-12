package eleeter.unifystudiox.ui.theme;


public class UITheme
{
    private static Theme current = new DarkTheme();

    private UITheme()
    {
    }

    public static Theme get()
    {
        return current;
    }

    public static void apply(Theme theme)
    {
        current = theme;
    }

    public interface Theme
    {
        float[] surface();

        float[] accent();

        float[] textPrimary();

        float[] textMuted();

        float[] border();

        float radiusSm();

        float radiusMd();

        float spacing();
    }

    /**
     * Default Dark Theme matching AniMatrix palette.
     */
    public static class DarkTheme implements Theme
    {
        @Override
        public float[] surface()
        {
            return new float[]{0.12F, 0.12F, 0.14F, 1.0F};
        }

        @Override
        public float[] accent()
        {
            return new float[]{0.25F, 0.45F, 0.95F, 1.0F};
        }

        @Override
        public float[] textPrimary()
        {
            return new float[]{1.0F, 1.0F, 1.0F, 1.0F};
        }

        @Override
        public float[] textMuted()
        {
            return new float[]{0.85F, 0.85F, 0.88F, 1.0F};
        }

        @Override
        public float[] border()
        {
            return new float[]{0.20F, 0.22F, 0.25F, 1.0F};
        }

        @Override
        public float radiusSm()
        {
            return 4.0F;
        }

        @Override
        public float radiusMd()
        {
            return 8.0F;
        }

        @Override
        public float spacing()
        {
            return 10.0F;
        }
    }
}
