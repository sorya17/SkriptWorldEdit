package me.tecspace.skworldedit.api.clipboard;

import com.fastasyncworldedit.core.extent.clipboard.DiskOptimizedClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import me.tecspace.skworldedit.api.utils.Utils;

import java.io.Closeable;

public record ClipboardWrapper(Clipboard clipboard) implements Closeable {

    public void close() {
        if (clipboard instanceof DiskOptimizedClipboard c && !c.getFile().delete()) {
            Utils.log("Could not remove disk backed clipboard: " + c.getFile());
        }
        clipboard.close();
    }
}
