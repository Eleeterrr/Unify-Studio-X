package eleeter.unifystudiox.resource;

import java.util.List;


public class AssetLink
{
    public static List<String> paths(String extension)
    {
        if (!AssetRegistry.isInitialized())
        {
            if (AssetRegistry.class != null)
            {
                AssetRegistry.init();
            }
        }
        
        return AssetRegistry.getPaths(extension);
    }

    public static List<String> siblings(String assetPath)
    {
        String directory = assetPath.contains("/") || assetPath.contains("\\") 
            ? assetPath.substring(0, Math.max(assetPath.lastIndexOf('/'), assetPath.lastIndexOf('\\')))
            : "";
            
        return AssetRegistry.getAssetsInDirectory(directory);
    }

    public static String match(String assetPath, String extension)
    {
        int lastDot = assetPath.lastIndexOf('.');
        if (lastDot == -1)
        {
            return null;
        }

        String targetName = assetPath.substring(0, lastDot);
        List<String> candidates = siblings(assetPath);
        
        for (String candidate : candidates)
        {
            if (candidate.startsWith(targetName) && candidate.toLowerCase().endsWith(extension.toLowerCase()))
            {
                return candidate;
            }
        }
        
        return null;
    }
}
