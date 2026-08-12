package eleeter.unifystudiox.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;


public class AssetRegistry
{
    private static final Map<String, List<String>> INDEX = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> DIRECTORY_INDEX = new ConcurrentHashMap<>();
    private static final List<AssetListener> LISTENERS = new CopyOnWriteArrayList<>();
    private static final ConcurrentLinkedQueue<AssetEvent> PENDING_EVENTS = new ConcurrentLinkedQueue<>();
    private static boolean initialized = false;


    public static void init()
    {
        if (initialized)
        {
            return;
        }

        AssetWatchdog.start();
        initialized = true;
    }

    public static void addListener(AssetListener listener)
    {
        LISTENERS.add(listener);
    }

    public static void removeListener(AssetListener listener)
    {
        LISTENERS.remove(listener);
    }


    public static void update()
    {
        AssetEvent event;
        while ((event = PENDING_EVENTS.poll()) != null)
        {
            for (AssetListener listener : LISTENERS)
            {
                if (event.type == AssetEvent.Type.ADDED)
                {
                    listener.onAssetAdded(event.path);
                }
                else if (event.type == AssetEvent.Type.REMOVED)
                {
                    listener.onAssetRemoved(event.path);
                }
                else if (event.type == AssetEvent.Type.MODIFIED)
                {
                    listener.onAssetModified(event.path);
                }
            }
        }
    }

    public static boolean isInitialized()
    {
        return initialized;
    }


    protected static void register(String path)
    {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1)
        {
            return;
        }

        String extension = path.substring(dotIndex).toLowerCase();
        List<String> paths = INDEX.computeIfAbsent(extension, k -> Collections.synchronizedList(new ArrayList<>()));
        
        if (!paths.contains(path))
        {
            paths.add(path);
            
            // Update Directory Index
            String dir = getDirectory(path);
            List<String> dirAssets = DIRECTORY_INDEX.computeIfAbsent(dir, k -> Collections.synchronizedList(new ArrayList<>()));
            if (!dirAssets.contains(path))
            {
                dirAssets.add(path);
            }

            PENDING_EVENTS.add(new AssetEvent(path, AssetEvent.Type.ADDED));
        }
    }


    protected static void remove(String path)
    {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1)
        {
            return;
        }

        String extension = path.substring(dotIndex).toLowerCase();
        List<String> paths = INDEX.get(extension);
        
        if (paths != null)
        {
            if (paths.remove(path))
            {
                String dir = getDirectory(path);
                List<String> dirAssets = DIRECTORY_INDEX.get(dir);
                if (dirAssets != null)
                {
                    dirAssets.remove(path);
                }

                PENDING_EVENTS.add(new AssetEvent(path, AssetEvent.Type.REMOVED));
            }
        }
    }

    protected static void modify(String path)
    {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1)
        {
            return;
        }

        PENDING_EVENTS.add(new AssetEvent(path, AssetEvent.Type.MODIFIED));
    }

    private static class AssetEvent
    {
        enum Type
        {
            ADDED, REMOVED, MODIFIED
        }
        String path;
        Type type;

        AssetEvent(String path, Type type)
        {
            this.path = path;
            this.type = type;
        }
    }


    protected static void removeFolder(String folderPath)
    {
        String prefix = folderPath.endsWith("/") ? folderPath : folderPath + "/";
        
        for (List<String> paths : INDEX.values())
        {
            synchronized (paths)
            {
                Iterator<String> it = paths.iterator();
                while (it.hasNext())
                {
                    String path = it.next();
                    if (path.startsWith(prefix) || path.equals(folderPath))
                    {
                        it.remove();
                        PENDING_EVENTS.add(new AssetEvent(path, AssetEvent.Type.REMOVED));
                    }
                }
            }
        }

        DIRECTORY_INDEX.keySet().removeIf(dir -> dir.startsWith(prefix) || dir.equals(folderPath));
    }


    public static List<String> getPaths(String extension)
    {
        List<String> paths = INDEX.get(extension.toLowerCase());
        if (paths == null)
        {
            return Collections.emptyList();
        }
        
        synchronized (paths)
        {
            return new ArrayList<>(paths);
        }
    }


    public static List<String> getAssetsInDirectory(String directory)
    {
        List<String> assets = DIRECTORY_INDEX.get(directory);
        if (assets == null)
        {
            return Collections.emptyList();
        }

        synchronized (assets)
        {
            return new ArrayList<>(assets);
        }
    }

    private static String getDirectory(String path)
    {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1)
        {
            lastSlash = path.lastIndexOf('\\');
        }
        
        return (lastSlash == -1) ? "" : path.substring(0, lastSlash);
    }
}
