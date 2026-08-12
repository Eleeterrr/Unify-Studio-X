package eleeter.unifystudiox.assets.browser;

import eleeter.unifystudiox.assets.AssetCategory;
import eleeter.unifystudiox.assets.AssetManager;
import eleeter.unifystudiox.assets.IModelAsset;
import eleeter.unifystudiox.assets.ModelAssetDescriptor;
import eleeter.unifystudiox.assets.ModelPreviewSpec;
import eleeter.unifystudiox.renderer.FontManager;
import eleeter.unifystudiox.scene.entity.LabelEntity;
import eleeter.unifystudiox.scene.entity.PointLightEntity;
import eleeter.unifystudiox.scene.entity.SceneEntity;
import eleeter.unifystudiox.scene.entity.SpotlightEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.joml.Vector3f;

public class SceneAssetBrowserDataSource implements AssetBrowserDataSource
{
    private final AssetManager assetManager;
    private final Supplier<Collection<SceneEntity>> sceneEntitySupplier;

    public SceneAssetBrowserDataSource(AssetManager assetManager,
                                       Supplier<Collection<SceneEntity>> sceneEntitySupplier)
    {
        this.assetManager = assetManager;
        this.sceneEntitySupplier = sceneEntitySupplier;
    }

    @Override
    public void refresh()
    {
        this.assetManager.syncFromScene(this.sceneEntitySupplier.get());
    }

    @Override
    public int getRevision()
    {
        return this.assetManager.getRevision();
    }

    @Override
    public List<IModelAsset> getPreviewModels()
    {
        return this.assetManager.getLoadedModels();
    }

    @Override
    public List<AssetBrowserSection> getSections()
    {
        Map<AssetCategory, List<AssetBrowserItem>> grouped = new EnumMap<>(AssetCategory.class);
        for (AssetCategory cat : AssetCategory.values())
        {
            grouped.put(cat, new ArrayList<>());
        }

        for (IModelAsset asset : this.assetManager.getLoadedModels())
        {
            grouped.get(asset.getCategory()).add(AssetBrowserItem.forModel(asset));
        }

        grouped.get(AssetCategory.TOOL).addAll(this.buildToolItems());

        List<AssetBrowserSection> sections = new ArrayList<>();
        this.addSection(sections, grouped, AssetCategory.MODEL, "Models");
        this.addSection(sections, grouped, AssetCategory.LIGHT, "Lights");
        this.addSection(sections, grouped, AssetCategory.ANNOTATION, "Annotations");
        this.addSection(sections, grouped, AssetCategory.TOOL, "Tool Templates");

        sections.add(new AssetBrowserSection("recent_items", "Recent", List.of(AssetBrowserItem.placeholder("recent_placeholder", "Recent Items", "Recent assets will appear here"))));

        return sections;
    }

    private void addSection(List<AssetBrowserSection> sections, Map<AssetCategory, List<AssetBrowserItem>> grouped, AssetCategory category, String title)
    {
        List<AssetBrowserItem> items = grouped.get(category);
        if (items != null && !items.isEmpty())
        {
            sections.add(new AssetBrowserSection(category.name().toLowerCase() + "_section", title, items));
        }
    }

    private List<AssetBrowserItem> buildToolItems()
    {
        List<AssetBrowserItem> items = new ArrayList<>();

        ModelPreviewSpec spotSpec = new ModelPreviewSpec(() ->
        {
            SpotlightEntity s = new SpotlightEntity("tool_preview_spot");
            s.setColor(0.4f, 0.7f, 1.0f).setRange(15f).setCutoff(25f, 40f);
            s.setPosition(new Vector3f(0f, 0f, 0f));
            return s;
        }, new ModelPreviewSpec.Bounds(0, -7.5f, 0, 15, 15, 15), 1.0f);
        items.add(AssetBrowserItem.forModel(new ModelAssetDescriptor("tool_spotlight", "Spotlight Tool", false, spotSpec, AssetCategory.TOOL)));

        ModelPreviewSpec pointSpec = new ModelPreviewSpec(() ->
        {
            PointLightEntity p = new PointLightEntity("tool_preview_point");
            p.setColor(1.0f, 0.85f, 0.6f).setRange(15f);
            p.setPosition(new Vector3f(0f, 0f, 0f));
            return p;
        }, new ModelPreviewSpec.Bounds(0, -7.5f, 0, 15, 15, 15), 1.0f);
        items.add(AssetBrowserItem.forModel(new ModelAssetDescriptor("tool_point_light", "Point Light Tool", false, pointSpec, AssetCategory.TOOL)));

        ModelPreviewSpec labelSpec = new ModelPreviewSpec(() ->
        {
            LabelEntity l = new LabelEntity("tool_preview_label");
            l.setFont(FontManager.getFont("inter"));
            l.setText("Label").setFontSize(24f).setColor(1f, 1f, 1f).setBillboard(false);
            l.setPosition(new Vector3f(0f, 0f, 0f));
            l.getMeshData(); // force bounds calculation
            return l;
        }, new ModelPreviewSpec.Bounds(0, 0, 0, 3f, 1f, 0.1f), 1.0f);
        items.add(AssetBrowserItem.forModel(new ModelAssetDescriptor("tool_label", "Label Tool", false, labelSpec, AssetCategory.TOOL)));

        return items;
    }
}
