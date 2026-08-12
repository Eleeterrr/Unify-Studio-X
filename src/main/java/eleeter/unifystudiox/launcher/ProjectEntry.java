package eleeter.unifystudiox.launcher;

public class ProjectEntry
{
    private final String name;
    private final String path;

    public ProjectEntry(String name, String path)
    {
        this.name = name;
        this.path = path;
    }

    /** Returns the display name of the project. */
    public String getName()
    {
        return this.name;
    }

    /** Returns the absolute file-system path to the project directory. */
    public String getPath()
    {
        return this.path;
    }
}
