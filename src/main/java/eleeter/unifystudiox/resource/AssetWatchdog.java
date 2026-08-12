package eleeter.unifystudiox.resource;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import eleeter.unifystudiox.util.log.AniLogger;


public class AssetWatchdog
{
    private static final String ASSETS_ROOT = "assets";
    private static final Map<WatchKey, Path> WATCH_KEYS = new HashMap<>();
    private static WatchService watchService;

    /**
     * Starts the watchdog daemon thread
     */
    public static void start()
    {
        Thread daemon = new Thread(AssetWatchdog::run, "AssetWatchdog");
        daemon.setDaemon(true);
        daemon.start();
    }

    private static void run()
    {
        try
        {
            watchService = FileSystems.getDefault().newWatchService();
            Path rootPath = Paths.get(ASSETS_ROOT);

            if (!Files.exists(rootPath))
            {
                AniLogger.warn("AssetWatchdog", "Assets root folder not found: " + ASSETS_ROOT);
                return;
            }

            AniLogger.info("AssetWatchdog", "Starting initial recursive indexing...");
            registerRecursive(rootPath);
            AniLogger.info("AssetWatchdog", "Initial indexing complete.");

            while (true)
            {
                WatchKey key = watchService.take();
                Path dir = WATCH_KEYS.get(key);

                if (dir == null)
                {
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents())
                {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE)
                    {
                        if (Files.isDirectory(child))
                        {
                            registerRecursive(child);
                        }
                        else
                        {
                            registerFile(child);
                        }
                    }
                    else if (kind == StandardWatchEventKinds.ENTRY_DELETE)
                    {
                        // Handle recursive removal if a directory was deleted
                        unregisterRecursive(child);
                    }
                    else if (kind == StandardWatchEventKinds.ENTRY_MODIFY)
                    {
                        if (!Files.isDirectory(child))
                        {
                            modifyFile(child);
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid)
                {
                    WATCH_KEYS.remove(key);
                    if (WATCH_KEYS.isEmpty())
                    {
                        break;
                    }
                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            AniLogger.error("AssetWatchdog", "Watchdog thread interrupted or failed: " + e.getMessage());
        }
    }


    private static void registerRecursive(final Path start) throws IOException
    {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
                WatchKey key = dir.register(watchService, 
                        StandardWatchEventKinds.ENTRY_CREATE, 
                        StandardWatchEventKinds.ENTRY_DELETE, 
                        StandardWatchEventKinds.ENTRY_MODIFY);
                
                WATCH_KEYS.put(key, dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                registerFile(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void registerFile(Path file)
    {
        String pathString = file.toString().replace("\\", "/");
        AssetRegistry.register(pathString);
        AniLogger.info("AssetWatchdog", "Registered asset: " + pathString);
    }

    private static void modifyFile(Path file)
    {
        String pathString = file.toString().replace("\\", "/");
        AssetRegistry.modify(pathString);
        AniLogger.info("AssetWatchdog", "Modified asset: " + pathString);
    }

    private static void unregisterFile(Path file)
    {
        String pathString = file.toString().replace("\\", "/");
        AssetRegistry.remove(pathString);
        AniLogger.info("AssetWatchdog", "Unregistered asset: " + pathString);
    }

    private static void unregisterRecursive(Path path)
    {
        String pathString = path.toString().replace("\\", "/");
        AssetRegistry.removeFolder(pathString);
        AniLogger.info("AssetWatchdog", "Unregistered folder/asset: " + pathString);
    }
}
