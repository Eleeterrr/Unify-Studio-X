package eleeter.unifystudiox.launcher;

import java.util.List;

public interface IProjectRegistry
{
    /** Returns an unmodifiable snapshot of all known projects. */
    List<ProjectEntry> getAll();

    /** Registers a new project in the registry. */
    void add(ProjectEntry entry);

    void remove(String path);
}
