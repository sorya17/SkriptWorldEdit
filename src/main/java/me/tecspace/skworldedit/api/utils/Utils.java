package me.tecspace.skworldedit.api.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.log.ErrorQuality;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import me.tecspace.skworldedit.SkWorldEdit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import io.papermc.paper.registry.tag.Tag;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class Utils {

    private static final MiniMessage MINIMESSAGE = MiniMessage.miniMessage();
    public static final Component PLUGIN_PREFIX = Component.text("[SkriptWorldEdit] ");

    public static void SkriptError(String s) {
        Skript.error(s, ErrorQuality.SEMANTIC_ERROR);
    }

    public static void log(String... lines) {
        for (String line : lines) {
            Bukkit.getConsoleSender().sendMessage(
                    PLUGIN_PREFIX.append(MINIMESSAGE.deserialize(line))
            );
        }
    }

    /**
     * Converts a {@link Vector} to a {@link Vector3}
     */
    public static Vector3 toVector3(Vector vec) {
        return Vector3.at(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Converts a {@link Location} to a {@link BlockVector3}
     */
    public static BlockVector3 toBlockVector3(Location loc) {
        return BlockVector3.at(loc.x(), loc.y(), loc.z());
    }

    /**
     * Converts an array of {@link Location} into an array of {@link BlockVector3}
     */
    public static BlockVector3[] toBlockVector3(Location[] locs) {
        return Arrays.stream(locs)
                .map(Utils::toBlockVector3)
                .toArray(BlockVector3[]::new);
    }

    /**
     * Converts a {@link Vector} to a {@link BlockVector3}
     */
    public static BlockVector3 toBlockVector3(Vector vec) {
        return BlockVector3.at(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Check if a {@link Tag} is a {@link Material} Tag.
     * @author <a href="https://github.com/ShaneBeee/SkBee/">ShaneBeee - SkBee</a>
     */
    public static boolean isMaterialTag(Tag<?> tag) {
        ParameterizedType superC = (ParameterizedType) tag.getClass().getGenericSuperclass();
        for (Type arg : superC.getActualTypeArguments()) {
            if (arg.equals(Material.class)) return true;
        }
        return false;
    }

    public static @Nullable String getFileExtension(Path path) {
        return getFileExtension(path.getFileName().toString());
    }

    public static @Nullable String getFileExtension(String string) {
        int dot = string.lastIndexOf('.');
        return dot > 0 ? string.substring(dot + 1) : null;
    }

    /**
     * Runs the operation async only if async is true and FAWE is used
     * @param async whether to run it asynchronously
     * @param runnable the code to run
     */
    public static void run(boolean async, Runnable runnable) {
        run(async, runnable, null);
    }

    /**
     * Runs the operation async only if async is true and FAWE is used, and code to be run afterward on the main thread
     * @param async whether to run it asynchronously
     * @param runnable the code to run
     * @param callback code to run afterward on the main thread
     */
    public static void run(boolean async, Runnable runnable, @Nullable Runnable callback) {
        if (SkWorldEdit.UsesFastAsyncWorldEdit && async) {
            Bukkit.getScheduler().runTaskAsynchronously(SkWorldEdit.getInstance(), () -> {
                runnable.run();
                if (callback != null)
                    Bukkit.getScheduler().runTask(SkWorldEdit.getInstance(), callback);
            });
        } else {
            runnable.run();
            if (callback != null) callback.run();
        }
    }

    public static <T> T getAsyncBlocking(T defaultValue, Supplier<T> supplier) {
        if (SkWorldEdit.UsesFastAsyncWorldEdit) {
            try {
                T result = CompletableFuture.supplyAsync(supplier).get();
                return result != null ? result : defaultValue;
            } catch (Exception e) {
                Utils.log("Exception in async eval: " + e.getMessage());
                return defaultValue;
            }
        } else {
            return supplier.get();
        }
    }
}
