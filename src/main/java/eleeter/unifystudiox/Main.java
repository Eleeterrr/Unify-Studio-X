package eleeter.unifystudiox;

import eleeter.unifystudiox.animation.api.AnimationSystem;
import eleeter.unifystudiox.animation.runtime.AnimationSystemImpl;
import eleeter.unifystudiox.assets.AssetManager;
import eleeter.unifystudiox.assets.browser.SceneAssetBrowserDataSource;
import eleeter.unifystudiox.editor.animation.ModelEditorLayout;
import eleeter.unifystudiox.graphics.Window;
import eleeter.unifystudiox.graphics.gl.GLGraphicsBackend;
import eleeter.unifystudiox.graphics.TextureGL;
import eleeter.unifystudiox.i18n.I18nEngine;
import eleeter.unifystudiox.renderer.Renderer;
import eleeter.unifystudiox.renderer.postprocess.ACESTonemapEffect;
import eleeter.unifystudiox.renderer.postprocess.BloomEffect;
import eleeter.unifystudiox.renderer.postprocess.ChromaticAberrationEffect;
import eleeter.unifystudiox.renderer.postprocess.ColorGradingEffect;
import eleeter.unifystudiox.renderer.postprocess.DepthOfFieldEffect;
import eleeter.unifystudiox.renderer.postprocess.PostProcessingStack;
import eleeter.unifystudiox.renderer.postprocess.VignetteEffect;
import eleeter.unifystudiox.resource.AssetBinder;
import eleeter.unifystudiox.resource.AssetRegistry;
import eleeter.unifystudiox.resource.IAssetBinder;
import eleeter.unifystudiox.resource.SceneResourceOrchestrator;
import eleeter.unifystudiox.scene.CameraZoomSpeedController;
import eleeter.unifystudiox.scene.Scene;
import eleeter.unifystudiox.scene.entity.CloudEntity;
import eleeter.unifystudiox.scene.entity.SkyEntity;
import eleeter.unifystudiox.scene.entity.SunEntity;
import eleeter.unifystudiox.settings.SettingsIO;
import eleeter.unifystudiox.settings.SettingsRegister;
import eleeter.unifystudiox.settings.menu.MenuBarRegistry;
import eleeter.unifystudiox.ui.SettingsPanel;
import eleeter.unifystudiox.renderer.environment.BaseplateRenderer;
import eleeter.unifystudiox.scene.entity.BaseplateEntity;
import eleeter.unifystudiox.ui.assets.UIAssetsPanel;
import eleeter.unifystudiox.ui.assets.placement.AssetPlacementController;
import eleeter.unifystudiox.ui.assets.placement.WorldRaycaster;
import eleeter.unifystudiox.ui.menu.UIMenuBar;
import eleeter.unifystudiox.ui.menu.UITopBarMenu;
import eleeter.unifystudiox.ui.overlay.UICameraZoom;
import eleeter.unifystudiox.ui.overlay.UIDebugOverlay;
import eleeter.unifystudiox.ui.startup.SplashScreen;
import eleeter.unifystudiox.ui.theme.UIShell;
import eleeter.unifystudiox.util.DiscordIPC;
import eleeter.unifystudiox.util.PerformanceMonitor;
import eleeter.unifystudiox.util.log.AniLogger;
import eleeter.unifystudiox.util.log.CrashReporter;
import eleeter.unifystudiox.vfx.renderer.VFXRenderer;
import org.joml.Matrix4f;

public class Main
{
    public static void main(String[] args)
    {
        SplashScreen.show();
        DiscordIPC.init();
        DiscordIPC.setId(System.getenv("CLIENT_ID"));
        AssetRegistry.init();
        IAssetBinder assetBinder = new AssetBinder();

        AniLogger.info("Main", "Initializing Unify Engine...");
        I18nEngine.load("en_us");

        Window window = new Window("Unify Studio X", 1280, 720);
        Scene scene = new Scene();
        UIShell.boot(scene.getUi().getRoot());

        GLGraphicsBackend backend = new GLGraphicsBackend();
        Renderer renderer = new Renderer(backend);
        AssetManager assetManager = new AssetManager();
        AnimationSystem animationSystem = new AnimationSystemImpl();
        scene.setAnimationSystem(animationSystem);
        scene.setAssetBinder(assetBinder);

        AssetRegistry.addListener(new SceneResourceOrchestrator(scene, assetBinder));

        Thread.setDefaultUncaughtExceptionHandler((t, e) ->
        {
            CrashReporter.report(e);
        });

        SunEntity sun = new SunEntity();
        sun.setDirection(100.5f, 100.0f, -100.5f);
        scene.addEntity(sun);


        SkyEntity sky = new SkyEntity();
        sky.setSun(sun);
        scene.addEntity(sky);

        CloudEntity clouds = new CloudEntity("sys_clouds");
        scene.addEntity(clouds);
        BaseplateEntity baseplate = new BaseplateEntity("world_floor", 100f, 100f, 1.0f);
        baseplate.setTexture(TextureGL.loadCached("/textures/baseplate.png"));
        scene.addEntity(baseplate);


        SettingsPanel settingsPanel = new SettingsPanel(scene.getUi().getContext());
        scene.getUi().getRoot().addChild(settingsPanel);

        SettingsRegister.init(settingsPanel);

        UIAssetsPanel assetsPanel = new UIAssetsPanel(new SceneAssetBrowserDataSource(assetManager, scene::getEntities));
        scene.getUi().getRoot().addChild(assetsPanel);

        WorldRaycaster worldRaycaster = new WorldRaycaster()
        {
            @Override
            public Matrix4f getProjectionMatrix()
            {
                return scene.getCamera().getProjectionMatrix(window.getLogicalWidth(), window.getLogicalHeight());
            }

            @Override
            public Matrix4f getViewMatrix()
            {
                return scene.getCamera().getViewMatrix();
            }

            @Override
            public int getLogicalWidth()
            {
                return window.getLogicalWidth();
            }

            @Override
            public int getLogicalHeight()
            {
                return window.getLogicalHeight();
            }
        };
        AssetPlacementController placementController = new AssetPlacementController(scene, worldRaycaster);
        assetsPanel.setPlacementController(placementController);
        
        MenuBarRegistry menuRegistry = new MenuBarRegistry();
        UITopBarMenu bar = new UITopBarMenu();
        bar.setupExampleMenus(menuRegistry);

        UIMenuBar menuBar = new UIMenuBar(menuRegistry);
        scene.getUi().getRoot().addChild(menuBar);

        PerformanceMonitor monitor = new PerformanceMonitor();
        UIDebugOverlay debugOverlay = new UIDebugOverlay(monitor);
        scene.getUi().getRoot().addChild(debugOverlay);

        UICameraZoom zoomHud = new UICameraZoom();
        scene.getUi().getRoot().addChild(zoomHud);

        CameraZoomSpeedController zoomController = new CameraZoomSpeedController();
        scene.setZoomController(zoomController, zoomHud);
        ModelEditorLayout modelEditor = ModelEditorLayout.builder().build();
        modelEditor.addTo(scene.getUi().getRoot());
        VFXRenderer vfxRenderer = new VFXRenderer();
        PostProcessingStack postStack = new PostProcessingStack();
        ACESTonemapEffect aces = new ACESTonemapEffect();
        postStack.addEffect(aces);
        BloomEffect bloom = new BloomEffect();
        postStack.addEffect(bloom);
        ColorGradingEffect colorGrading = new ColorGradingEffect();
        postStack.addEffect(colorGrading);
        VignetteEffect vignette = new VignetteEffect();
        postStack.addEffect(vignette);
        ChromaticAberrationEffect chroma = new ChromaticAberrationEffect();
        postStack.addEffect(chroma);
        DepthOfFieldEffect dof = new DepthOfFieldEffect();
        postStack.addEffect(dof);
        SplashScreen.close();
        window.run(scene, renderer, backend);
        vfxRenderer.destroy();
        postStack.dispose();
        SettingsIO.save();
        DiscordIPC.shutdown();
        modelEditor.cleanup();
        assetsPanel.cleanupResources();

    }
}
