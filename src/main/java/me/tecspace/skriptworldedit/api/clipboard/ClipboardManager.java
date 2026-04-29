package me.tecspace.skriptworldedit.api.clipboard;

import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ClipboardManager {

    private static final Map<Path, ClipboardWrapper> CLIPBOARD_MAP = new HashMap<>();

    public static boolean has(Path path) {
        return CLIPBOARD_MAP.containsKey(path);
    }

    public static String[] getCachedClipboards() {
        return CLIPBOARD_MAP.keySet().stream()
                .map(Path::toString)
                .toArray(String[]::new);
    }

    /**
     * Returns the {@link ClipboardWrapper} for the given path,
     * loading it from disk first if not cached.
     */
    public static @NotNull ClipboardWrapper getClipboard(Path path, boolean cache) throws IOException {
        ClipboardWrapper cached = CLIPBOARD_MAP.get(path);
        if (cached != null) return cached;

        ClipboardWrapper clipboard = loadClipboard(path);
        if (cache) CLIPBOARD_MAP.put(path, clipboard);
        return clipboard;
    }

    public static void removeClipboard(Path path) {
        if (!CLIPBOARD_MAP.containsKey(path)) return;
        CLIPBOARD_MAP.remove(path).close();
    }

    public static void removeAll() {
        CLIPBOARD_MAP.values().forEach(ClipboardWrapper::close);
        CLIPBOARD_MAP.clear();
    }

    private static ClipboardWrapper loadClipboard(Path path) throws IOException {
        File file = path.toFile();
        if (!file.exists())
            throw new IOException("File does not exist: " + file);

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null)
            throw new IOException("Unsupported or unrecognized schematic format: " + file);

        try (FileInputStream fis = new FileInputStream(file);
             ClipboardReader reader = format.getReader(fis)) {
            return new ClipboardWrapper(reader.read());
        }
    }
}