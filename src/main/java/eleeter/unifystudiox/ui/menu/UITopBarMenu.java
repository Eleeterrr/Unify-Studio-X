package eleeter.unifystudiox.ui.menu;

import eleeter.unifystudiox.i18n.list.Keys;
import eleeter.unifystudiox.settings.menu.MenuAction;
import eleeter.unifystudiox.settings.menu.MenuBarRegistry;
import eleeter.unifystudiox.settings.menu.MenuCategory;

public class UITopBarMenu
{
    private MenuCategory files;
    private MenuBarRegistry memu;
    private MenuAction action;


    public void setupExampleMenus(MenuBarRegistry memu, MenuCategory files, MenuAction action)
    {
        this.memu = memu;
        this.files = files;
        this.action = action;


    }

    public void setupExampleMenus(MenuBarRegistry registry)
    {


        this.files = registry.registerCategory("file", "File");
        this.files.setIcon((sd, cx, cy, s, t) -> sd.drawNewFileIcon(cx, cy, s, t));

        this.files.addAction(new MenuAction("New Scene", "Ctrl+N",
                Keys.MEMU_CREATE_NEW_SCENE,
                () -> System.out.println("New Scene"))
                .setIcon((sd, cx, cy, s, t) -> sd.drawNewFileIcon(cx, cy, s, t)));

        this.files.addAction(new MenuAction("Open...", "Ctrl+O",
                Keys.MEMU_OPEN_EXPLORER,
                () -> System.out.println("Open"))
                .setIcon((sd, cx, cy, s, t) -> sd.drawOpenIcon(cx, cy, s, t)));

        this.files.addSeparator();

        this.files.addAction(new MenuAction("Save", "Ctrl+S",
                Keys.MEMU_OPEN_SAVE, () -> System.out.println("Save"))
                .setIcon((sd, cx, cy, s, t) -> sd.drawSaveIcon(cx, cy, s, t)));

        this.files.addAction(new MenuAction("Save As...", "Ctrl+Shift+S",
                Keys.MEMU_OPEN_SAVE_AS, () -> System.out.println("Save As"))
                .setIcon((sd, cx, cy, s, t) -> sd.drawSaveIcon(cx, cy, s, t)));

        this.files.addSeparator();

        this.files.addAction(new MenuAction("Exit", "Alt+F4",
                Keys.MEMU_EXIT, () -> System.exit(0))
                .setIcon((sd, cx, cy, s, t) -> sd.drawCloseIcon(cx, cy, s, t)));

        // EDIT
        MenuCategory edit = registry.registerCategory("edit", "Edit");
        edit.setIcon((sd, cx, cy, s, t) -> sd.drawCopyIcon(cx, cy, s, t));

        edit.addAction(new MenuAction("Undo", "Ctrl+Z",
                Keys.MEMU_UNDO,
                () -> System.out.println("Undo"))
                .setIcon((sd, cx, cy, s, t) -> sd.drawUndoIcon(cx, cy, s, t)));

        edit.addAction(new MenuAction("Redo", "Ctrl+Y",
                Keys.MEMU_REDO,
                () -> System.out.println("Redo"))
                .setIcon((sd, cx, cy, s, t) -> sd.drawRedoIcon(cx, cy, s, t)));


    }
}
