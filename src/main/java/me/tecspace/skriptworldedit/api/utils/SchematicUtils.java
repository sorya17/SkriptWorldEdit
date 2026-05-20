package me.tecspace.skriptworldedit.api.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.log.ErrorQuality;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import me.tecspace.skriptworldedit.api.clipboard.ClipboardManager;
import me.tecspace.skriptworldedit.api.clipboard.ClipboardWrapper;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
    public static void create(RegionWrapper region, Path savePath, ClipboardFormat format, boolean copyEntities, boolean copyBiomes, @Nullable Location origin, @Nullable Mask sourceMask, @Nullable Transform transform) {
        try (BlockArrayClipboard clipboard = new BlockArrayClipboard(region.region())) {
            ForwardExtentCopy copy = new ForwardExtentCopy(
                    region.world(),
                    region.region(),
                    (origin != null) ? Utils.toBlockVector3(origin) : region.region().getMinimumPoint(),
                    clipboard,
                    region.region().getMinimumPoint()
            );
            // configuration
            copy.setCopyingEntities(copyEntities);
            copy.setCopyingBiomes(copyBiomes);
            if (sourceMask != null) copy.setSourceMask(sourceMask);
            if (transform != null) copy.setTransform(transform);
            //
            try {
                Operations.complete(copy);
                try (ClipboardWriter writer = format.getWriter(
                        Files.newOutputStream(savePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                    writer.write(clipboard);
                }
            } catch (IOException | WorldEditException e) {
                Utils.log("Exception while trying to save clipboard: " + e.getMessage());
            }
        }
    }

    /**
     * Loads a schematic from a file and pastes it at a location
     */
    public static void paste(Path path, Location location, boolean ignoreAir, boolean pasteEntities, boolean pasteBiomes, @Nullable Mask mask, @Nullable Mask sourceMask, @Nullable Transform transform) {
        File file = path.toFile();
        if (!file.exists()) {
            Skript.error("Tried to load schematic from disk but file " + path + " does not exist.", ErrorQuality.SEMANTIC_ERROR);
            return;
        }
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Skript.error("Tried to load schematic from disk but could not determine schematic format of file " + path, ErrorQuality.SEMANTIC_ERROR);
            return;
        }

        World world = BukkitAdapter.adapt(location.getWorld());
        try (
                EditSession session = WorldEdit.getInstance().newEditSession(world);
                ClipboardWrapper clipboard = ClipboardManager.loadClipboard(path);
                ClipboardHolder holder = new ClipboardHolder(clipboard.clipboard())
        ) {
            if (mask != null) session.setMask(mask);
            if (transform != null) holder.setTransform(transform);
            Operation operation = holder
                    .createPaste(session)
                    .to(Utils.toBlockVector3(location))
                    .ignoreAirBlocks(ignoreAir)
                    .copyEntities(pasteEntities)
                    .copyBiomes(pasteBiomes)
                    .maskSource(sourceMask)
                    .build();
            Operations.complete(operation);
        }
    }
}