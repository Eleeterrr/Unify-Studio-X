package eleeter.unifystudiox.util.log;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class AniLogger
{

    private static final int MAX_HISTORY = 1000;

    private static final LinkedList<String> history = new LinkedList<>();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RED_BOLD = "\u001B[1;31m";

    private final String moduleName;

    public AniLogger(String moduleName)
    {
        this.moduleName = moduleName;
    }


    public static void info(String module, String message)
    {
        log(LogLevel.INFO, module, message);
    }

    public static void warn(String module, String message)
    {
        log(LogLevel.WARN, module, message);
    }

    public static void error(String module, String message)
    {
        log(LogLevel.ERROR, module, message);
    }

    public static void fatal(String module, String message)
    {
        log(LogLevel.FATAL, module, message);
    }


    public void info(String message)
    {
        log(LogLevel.INFO, this.moduleName, message);
    }

    public void warn(String message)
    {
        log(LogLevel.WARN, this.moduleName, message);
    }

    public void error(String message)
    {
        log(LogLevel.ERROR, this.moduleName, message);
    }

    public void fatal(String message)
    {
        log(LogLevel.FATAL, this.moduleName, message);
    }


    private static synchronized void log(LogLevel level, String module, String message)
    {
        String timestamp = LocalTime.now().format(timeFormatter);
        String threadName = Thread.currentThread().getName();
        
        String color = ANSI_RESET;
        switch (level)
        {
            case INFO -> color = ANSI_WHITE;
            case WARN -> color = ANSI_YELLOW;
            case ERROR -> color = ANSI_RED;
            case FATAL -> color = ANSI_RED_BOLD;
        }

        String rawFormatted = String.format("[%s] [%s/%s] (%s) %s", timestamp, threadName, level.name(), module, message);

        String coloredFormatted = String.format("%s[%s] [%s/%s] (%s) %s%s", color, timestamp, threadName, level.name(), module, message, ANSI_RESET);

        if (level == LogLevel.ERROR || level == LogLevel.FATAL)
        {
            System.err.println(coloredFormatted);
        }
        else
        {
            System.out.println(coloredFormatted);
        }

        history.add(rawFormatted);

        if (history.size() > MAX_HISTORY)
        {
            history.removeFirst();
        }
    }


    public static List<String> getHistory()
    {
        return new LinkedList<>(history);
    }
}
