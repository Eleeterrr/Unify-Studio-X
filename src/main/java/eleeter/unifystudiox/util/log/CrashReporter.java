package eleeter.unifystudiox.util.log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CrashReporter
{

    private static final String LOG_DIR = "logs";


    public static void report(Throwable throwable)
    {
        try
        {
            File dir = new File(LOG_DIR);
            if (!dir.exists())
            {
                dir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            File reportFile = new File(dir, "crash-report-" + timestamp + ".txt");

            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile)))
            {
                writer.println("--- Unify Studio X Report ---");
                writer.println();
                writer.println("--- YOU DID SOMETHING WRONG! ---");
                writer.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println("Exception: " + throwable.toString());
                writer.println();

                writer.println("--- STACK TRACE ---");
                throwable.printStackTrace(writer);
                writer.println();

                writer.println("--- SYSTEM INFO ---");
                writer.println("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")");
                writer.println("Architecture: " + System.getProperty("os.arch"));
                writer.println("Java Version: " + System.getProperty("java.version"));
                writer.println("User: " + System.getProperty("user.name"));
                writer.println();


                writer.println();


                writer.println("--- LOG HISTORY ---");
                List<String> history = AniLogger.getHistory();
                for (String entry : history)
                {
                    writer.println(entry);
                }
                writer.println();
                
                writer.println("End of Report, Take a look!");
            }

            AniLogger.fatal("CrashReporter", "A fatal error occurred! Crash report saved to: " + reportFile.getAbsolutePath());
        }
        catch (Exception e)
        {
            System.err.println("[CrashReporter] Failed to generate crash report!");
            e.printStackTrace();
        }
    }
}
