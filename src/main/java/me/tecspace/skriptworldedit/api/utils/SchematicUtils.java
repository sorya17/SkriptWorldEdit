package me.tecspace.skriptworldedit.api.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;

public class SchematicUtils {

    /**
     * Gets a schematic file from its full path
     */
    public static @Nullable File getSchematicFile(Path path) {
        File file = path.toFile();
        if (file.exists()) return file;
        return null;
    }

    /**
     * Gets a schematic file by its name using the worldedit schematics directory
     */
    public static @Nullable File getSchematicFile(String name) {
        Path directory = getSchematicsFolderPath();
        for (ClipboardFormat format : ClipboardFormats.getAll()) {
            File file = directory.resolve(name + "." + format.getPrimaryFileExtension()).toFile();
            if (file.exists()) return file;
        }
        return null;
    }

    public static Path getSchematicsFolderPath() {
        return WorldEdit.getInstance().getSchematicsFolderPath();
    }

    /**
     * Creates a schematic from a region
     */
    public static void create(RegionWrapper region, Path savePath, ClipboardFormat format, boolean copyEntities, boolean copyBiomes, @Nullable Mask sourceMask, @Nullable Transform transform) {
        Utils.run(true, () -> {
            try (BlockArrayClipboard clipboard = new BlockArrayClipboard(region.region())) {
                ForwardExtentCopy copy = new ForwardExtentCopy(BukkitAdapter.adapt(region.world()), region.region(), clipboard, region.region().getMinimumPoint());
                // configuration
                copy.setCopyingEntities(copyEntities);
                copy.setCopyingBiomes(copyBiomes);
                if (sourceMask != null) copy.setSourceMask(sourceMask);
                if (transform != null) copy.setTransform(transform);
                //
                try {
                    Operations.complete(copy);
                    try (ClipboardWriter writer = format.getWriter(new FileOutputStream(savePath.toFile()))) {
                        writer.write(clipboard);
                    }
                } catch (IOException | WorldEditException e) {
                    Utils.log("Exception while trying to save clipboard: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Loads a schematic from a file and pastes it at a location
     */
    public static void paste(Path path, Location location, boolean ignoreAir, boolean pasteEntities, boolean pasteBiomes) {
        File file = path.toFile();
        if (!file.exists()) {
            Utils.log("File does not exist: " + path);
            return;
        }
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Utils.log("File not a supported schematic or failed to determine schematic format at: " + path);
            return;
        }

        Clipboard clipboard;
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        World world = BukkitAdapter.adapt(location.getWorld());
        Utils.run(true, () -> {
            try (
                    EditSession editSession = WorldEdit.getInstance().newEditSession(world);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    Clipboard c = clipboard
            ) {
                Operation operation = holder
                        .createPaste(editSession)
                        .to(Utils.toBlockVector3(location))
                        .ignoreAirBlocks(ignoreAir)
                        .copyEntities(pasteEntities)
                        .copyBiomes(pasteBiomes)
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException e) {
                Utils.log("Exception while trying to paste clipboard: " + e.getMessage());
            }
        });
    }
}