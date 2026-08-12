package eleeter.unifystudiox.settings;

import eleeter.unifystudiox.i18n.I18nEngine;
import eleeter.unifystudiox.i18n.list.Keys;
import eleeter.unifystudiox.renderer.core.RenderSettings;
import eleeter.unifystudiox.ui.SettingsPanel;

public class SettingsRegister
{
    public static void init(SettingsPanel settingsPanel)
    {
        /* Language Settings */
        SettingsRegistry.registerHeader("General", Keys.HEADER_LANGUAGE);

        String[] selectedLang = {"en_us"};

        SettingsRegistry.registerDropdown(
            "language.selection",
            "General",
            "Language",
            "en_us:English",
            selectedLang[0],
            () -> selectedLang[0],
            key ->
            {
                selectedLang[0] = key;
                I18nEngine.load(key);
                settingsPanel.requestRebuild();
            }
        ).withLabel(Keys.LABEL_LANGUAGE_SELECTION);

        /* Graphics Settings */
        SettingsRegistry.registerHeader("Graphics", Keys.HEADER_QUALITY_PRESETS);
        SettingsRegistry.registerAction("graphics.quality.ultra_low", "Graphics", "Set Preset: Ultra Low",
                        () -> RenderSettings.applyQuality(RenderSettings.GraphicsQuality.ULTRA_LOW))
                .withLabel(Keys.LABEL_GRAPHICS_QUALITY_ULTRA_LOW)
                .withTooltip(Keys.GRAPHICS_QUALITY_ULTRA_LOW);


        SettingsRegistry.registerAction("graphics.quality.low", "Graphics", "Set Preset: Low",
                        () -> RenderSettings.applyQuality(RenderSettings.GraphicsQuality.LOW))
                .withLabel(Keys.LABEL_GRAPHICS_QUALITY_LOW)
                .withTooltip(Keys.GRAPHICS_QUALITY_LOW);


        SettingsRegistry.registerAction("graphics.quality.mid", "Graphics", "Set Preset: Mid",
                        () -> RenderSettings.applyQuality(RenderSettings.GraphicsQuality.MID))
                .withLabel(Keys.LABEL_GRAPHICS_QUALITY_MID)
                .withTooltip(Keys.GRAPHICS_QUALITY_MID);


        SettingsRegistry.registerAction("graphics.quality.high", "Graphics", "Set Preset: High",
                        () -> RenderSettings.applyQuality(RenderSettings.GraphicsQuality.HIGH))
                .withLabel(Keys.LABEL_GRAPHICS_QUALITY_HIGH)
                .withTooltip((Keys.GRAPHICS_QUALITY_HIGH));


        SettingsRegistry.registerHeader("Graphics", Keys.HEADER_QUALITY);

        SettingsRegistry.registerToggle("graphics.anisotropy", "Graphics", "Anisotropic Filtering",
                        RenderSettings.ANISOTROPY_ENABLED,
                        () -> RenderSettings.ANISOTROPY_ENABLED,
                        val -> RenderSettings.ANISOTROPY_ENABLED = val)
                .withLabel(Keys.LABEL_GRAPHICS_ANISOTROPY)
                .withTooltip(Keys.GRAPHICS_ANISOTROPY);


        SettingsRegistry.registerToggle("graphics.vsync", "Graphics", "VSync",
                        RenderSettings.VSYNC_ENABLED,
                        () -> RenderSettings.VSYNC_ENABLED,
                        val -> RenderSettings.VSYNC_ENABLED = val)
                .withLabel(Keys.LABEL_GRAPHICS_VSYNC)
                .withTooltip(Keys.GRAPHICS_VSYNC);


        SettingsRegistry.registerField("graphics.fps_limit", "Graphics", "Max FPS", 30f, 360f, 5f,
                        RenderSettings.TARGET_FPS,
                        () -> RenderSettings.TARGET_FPS,
                        val -> RenderSettings.TARGET_FPS = val)
                .withLabel(Keys.LABEL_GRAPHICS_FPS_LIMIT)
                .withTooltip(Keys.GRAPHICS_FPS_LIMIT);

        SettingsRegistry.registerSlider("graphics.lod_bias", "Graphics", "LOD Bias", 0f, 2.0f, 0.01f,
                        RenderSettings.GLOBAL_LOD_BIAS,
                        () -> RenderSettings.GLOBAL_LOD_BIAS,
                        val -> RenderSettings.GLOBAL_LOD_BIAS = val)
                .withLabel(Keys.LABEL_GRAPHICS_LOD_BIAS)
                .withTooltip(Keys.GRAPHICS_LOD_BIAS);

        /* Atmospheric Settings */
        SettingsRegistry.registerHeader("Graphics", Keys.HEADER_ATMOSPHERE);

        SettingsRegistry.registerToggle("graphics.night_mode", "Graphics", "Night Mode",
                        RenderSettings.NIGHT_MODE,
                        () -> RenderSettings.NIGHT_MODE,
                        val -> RenderSettings.NIGHT_MODE = val)
                .withLabel(Keys.LABEL_GRAPHICS_NIGHT_MODE)
                .withTooltip(Keys.GRAPHICS_NIGHT_MODE);

        SettingsRegistry.registerToggle("graphics.clouds", "Graphics", "Volumetric Clouds",
                        RenderSettings.CLOUDS_ENABLED,
                        () -> RenderSettings.CLOUDS_ENABLED,
                        val -> RenderSettings.CLOUDS_ENABLED = val)
                .withLabel(Keys.LABEL_GRAPHICS_CLOUDS)
                .withTooltip(Keys.GRAPHICS_CLOUDS);

        SettingsRegistry.registerSlider("graphics.cloud_coverage", "Graphics", "Cloud Coverage", 0f, 1.0f,
                        0.01f,
                        RenderSettings.CLOUD_COVERAGE,
                        () -> RenderSettings.CLOUD_COVERAGE,
                        val -> RenderSettings.CLOUD_COVERAGE = val)
                .withLabel(Keys.LABEL_GRAPHICS_CLOUD_COVERAGE)
                .withTooltip(Keys.GRAPHICS_CLOUD_COVERAGE);

        SettingsRegistry.registerSlider("graphics.cloud_density", "Graphics", "Cloud Density", 0.1f, 5.0f, 0.1f,
                        RenderSettings.CLOUD_DENSITY,
                        () -> RenderSettings.CLOUD_DENSITY,
                        val -> RenderSettings.CLOUD_DENSITY = val)
                .withLabel(Keys.LABEL_GRAPHICS_CLOUD_DENSITY)
                .withTooltip(Keys.GRAPHICS_CLOUD_DENSITY);

        /* Post-Processing Settings */
        SettingsRegistry.registerHeader("Graphics", Keys.HEADER_POST_PROCESSING);

        SettingsRegistry.registerToggle("graphics.bloom_enabled", "Graphics", "Bloom",
                        RenderSettings.BLOOM_ENABLED,
                        () -> RenderSettings.BLOOM_ENABLED,
                        val -> RenderSettings.BLOOM_ENABLED = val)
                .withLabel(Keys.LABEL_GRAPHICS_BLOOM_ENABLED)
                .withTooltip(Keys.GRAPHICS_BLOOM_ENABLED);

        SettingsRegistry.registerSlider("graphics.bloom_strength", "Graphics", "Bloom Intensity", 0.0f, 5.0f, 0.1f,
                        RenderSettings.BLOOM_STRENGTH,
                        () -> RenderSettings.BLOOM_STRENGTH,
                        val -> RenderSettings.BLOOM_STRENGTH = val)
                .withLabel(Keys.LABEL_GRAPHICS_BLOOM_STRENGTH)
                .withTooltip(Keys.GRAPHICS_BLOOM_STRENGTH);

        SettingsRegistry.registerSlider("graphics.bloom_threshold", "Graphics", "Bloom Threshold", 0.0f, 1.0f, 0.01f,
                        RenderSettings.BLOOM_THRESHOLD,
                        () -> RenderSettings.BLOOM_THRESHOLD,
                        val -> RenderSettings.BLOOM_THRESHOLD = val)
                .withLabel(Keys.LABEL_GRAPHICS_BLOOM_THRESHOLD)
                .withTooltip(Keys.GRAPHICS_BLOOM_THRESHOLD);

        SettingsRegistry.registerSlider("graphics.bloom_radius", "Graphics", "Bloom Radius", 1f, 12f, 1f,
                        (float) RenderSettings.BLOOM_RADIUS,
                        () -> (float) RenderSettings.BLOOM_RADIUS,
                        val -> RenderSettings.BLOOM_RADIUS = Math.round(val))
                .withLabel(Keys.LABEL_GRAPHICS_BLOOM_RADIUS)
                .withTooltip(Keys.GRAPHICS_BLOOM_RADIUS);

        SettingsRegistry.registerHeader("Graphics", Keys.HEADER_CINEMATIC_EFFECTS);

        SettingsRegistry.registerToggle("graphics.aces_enabled", "Graphics", "ACES Tonemapping",
                        RenderSettings.ACES_ENABLED,
                        () -> RenderSettings.ACES_ENABLED,
                        val -> RenderSettings.ACES_ENABLED = val)
                .withLabel(Keys.LABEL_GRAPHICS_ACES_ENABLED)
                .withTooltip(Keys.GRAPHICS_ACES_ENABLED);

        SettingsRegistry.registerSlider("graphics.aces_exposure", "Graphics", "Exposure", 0.1f, 5.0f, 0.1f,
                        RenderSettings.ACES_EXPOSURE,
                        () -> RenderSettings.ACES_EXPOSURE,
                        val -> RenderSettings.ACES_EXPOSURE = val)
                .withLabel(Keys.LABEL_GRAPHICS_ACES_EXPOSURE)
                .withTooltip(Keys.GRAPHICS_ACES_EXPOSURE);

        SettingsRegistry.registerToggle("graphics.cg_enabled", "Graphics", "Color Grading",
                        RenderSettings.COLOR_GRADING_ENABLED,
                        () -> RenderSettings.COLOR_GRADING_ENABLED,
                        val -> RenderSettings.COLOR_GRADING_ENABLED = val)
                .withLabel(Keys.LABEL_GRAPHICS_COLOR_GRADING_ENABLED)
                .withTooltip(Keys.GRAPHICS_COLOR_GRADING_ENABLED);

        SettingsRegistry.registerSlider("graphics.cg_contrast", "Graphics", "Contrast", 0.0f, 2.0f, 0.05f,
                        RenderSettings.CG_CONTRAST,
                        () -> RenderSettings.CG_CONTRAST,
                        val -> RenderSettings.CG_CONTRAST = val)
                .withLabel(Keys.LABEL_GRAPHICS_CG_CONTRAST)
                .withTooltip(Keys.GRAPHICS_CG_CONTRAST);

        SettingsRegistry.registerSlider("graphics.cg_brightness", "Graphics", "Brightness", -1.0f, 1.0f, 0.05f,
                        RenderSettings.CG_BRIGHTNESS,
                        () -> RenderSettings.CG_BRIGHTNESS,
                        val -> RenderSettings.CG_BRIGHTNESS = val)
                .withLabel(Keys.LABEL_GRAPHICS_CG_BRIGHTNESS)
                .withTooltip(Keys.GRAPHICS_CG_BRIGHTNESS);

        SettingsRegistry.registerSlider("graphics.cg_saturation", "Graphics", "Saturation", 0.0f, 3.0f, 0.05f,
                        RenderSettings.CG_SATURATION,
                        () -> RenderSettings.CG_SATURATION,
                        val -> RenderSettings.CG_SATURATION = val)
                .withLabel(Keys.LABEL_GRAPHICS_CG_SATURATION)
                .withTooltip(Keys.GRAPHICS_CG_SATURATION);

        SettingsRegistry.registerHeader("Graphics", Keys.HEADER_LENS_EFFECTS);

        SettingsRegistry.registerToggle("graphics.vignette_enabled", "Graphics", "Vignette",
                        RenderSettings.VIGNETTE_ENABLED,
                        () -> RenderSettings.VIGNETTE_ENABLED,
                        val -> RenderSettings.VIGNETTE_ENABLED = val)
                .withLabel(Keys.LABEL_GRAPHICS_VIGNETTE_ENABLED)
                .withTooltip(Keys.GRAPHICS_VIGNETTE_ENABLED);

        SettingsRegistry.registerSlider("graphics.vignette_intensity", "Graphics", "Vignette Intensity", 0.0f, 1.0f, 0.05f,
                        RenderSettings.VIGNETTE_INTENSITY,
                        () -> RenderSettings.VIGNETTE_INTENSITY,
                        val -> RenderSettings.VIGNETTE_INTENSITY = val)
                .withLabel(Keys.LABEL_GRAPHICS_VIGNETTE_INTENSITY)
                .withTooltip(Keys.GRAPHICS_VIGNETTE_INTENSITY);

        SettingsRegistry.registerToggle("graphics.chroma_enabled", "Graphics", "Chromatic Aberration",
                        RenderSettings.CHROMA_ENABLED,
                        () -> RenderSettings.CHROMA_ENABLED,
                        val -> RenderSettings.CHROMA_ENABLED = val)
                .withLabel(Keys.LABEL_GRAPHICS_CHROMA_ENABLED)
                .withTooltip(Keys.GRAPHICS_CHROMA_ENABLED);

        SettingsRegistry.registerSlider("graphics.chroma_strength", "Graphics", "Aberration Strength", 0.0f, 0.05f, 0.001f,
                        RenderSettings.CHROMA_STRENGTH,
                        () -> RenderSettings.CHROMA_STRENGTH,
                        val -> RenderSettings.CHROMA_STRENGTH = val)
                .withLabel(Keys.LABEL_GRAPHICS_CHROMA_STRENGTH)
                .withTooltip(Keys.GRAPHICS_CHROMA_STRENGTH);

        SettingsRegistry.registerToggle("graphics.dof_enabled", "Graphics", "Depth of Field",
                        RenderSettings.DOF_ENABLED,
                        () -> RenderSettings.DOF_ENABLED,
                        val -> RenderSettings.DOF_ENABLED = val)
                .withLabel(Keys.LABEL_GRAPHICS_DOF_ENABLED)
                .withTooltip(Keys.GRAPHICS_DOF_ENABLED);

        SettingsIO.save();
        // TODO: For now we just hardcoding the settings, In the future we will load
        // them from The main Project Scene File.
        SettingsIO.load();
    }
}
