package me.tecspace.skriptworldedit.api.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.log.ErrorQuality;
import com.sk89q.worldedit.math.BlockVector3;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import io.papermc.paper.registry.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Utils {

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
     * Converts a bukkit location to a BlockVector3
     * @param loc the location to convert
     */
    public static BlockVector3 toBlockVector3(Location loc) {
        return BlockVector3.at(loc.x(), loc.y(), loc.z());
    }

    /**
     * Converts an array of bukkit locations into an array of BlockVector3
     * @param locs an array of locations to convert
     */
    public static BlockVector3[] toBlockVector3(Location[] locs) {
        return Arrays.stream(locs)
                .map(Utils::toBlockVector3)
                .toArray(BlockVector3[]::new);
    }

    /**
     * Check whether a tag is a material tag
     * Author: ShaneBeee/SkBee
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
     * Runs operation async only if async is true and FAWE is used
     */
    public static void run(boolean async, Runnable runnable) {
        if (SkriptWorldEdit.UsesFastAsyncWorldEdit && async) {
            Bukkit.getScheduler().runTaskAsynchronously(SkriptWorldEdit.getInstance(), runnable);
        } else {
            runnable.run();
            //Bukkit.getScheduler().runTask(SkriptWorldEdit.getInstance(), runnable);
        }
    }

    /**
     * Force FAWE to evaluate the value async to prevent an exception, while waiting on main thread.
     * FAWE expects some things to be done async so this ensures that, even when we need it immediately.
     */
    public static <T> T evalAsyncBlocking(T defaultValue, Supplier<T> supplier) {
        if (SkriptWorldEdit.UsesFastAsyncWorldEdit) {
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
