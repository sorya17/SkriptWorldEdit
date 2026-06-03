package me.tecspace.skworldedit;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.util.Version;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import me.tecspace.skworldedit.api.plugin.UpdateChecker;
import me.tecspace.skworldedit.api.utils.Utils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.ClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class SkWorldEdit extends JavaPlugin implements AddonModule {

    private static SkWorldEdit instance;
    public static boolean UsesFastAsyncWorldEdit;

    public static SkWorldEdit getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        // Dependency Searching
        Plugin skript = getServer().getPluginManager().getPlugin("Skript");
        Plugin worldEdit = getServer().getPluginManager().getPlugin("WorldEdit");
        Plugin fawe = getServer().getPluginManager().getPlugin("FastAsyncWorldEdit");
        UsesFastAsyncWorldEdit = fawe != null && fawe.isEnabled();

        // Check for Skript and compare its version
        if (skript == null || !skript.isEnabled()) {
            getLogger().severe("Could not find Skript! Make sure you have it installed and that it properly loaded. Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else if (Skript.getVersion().isSmallerThan(new Version("2.14.0-pre1"))) {
            getLogger().severe("You are running an unsupported version of Skript. Please update to at least Skript 2.14.0. Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Check for WorldEdit
        if (worldEdit == null || !Objects.requireNonNull(worldEdit).isEnabled()) {
            getLogger().severe("Could not find WorldEdit! Make sure you have it installed and that it properly loaded. Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!UsesFastAsyncWorldEdit) {
            getLogger().severe("FastAsyncWorldEdit is required to use this Addon. FastAsyncWorldEdit is a fork of WorldEdit that has huge speed and memory improvements and considerably more features. Download it here: https://modrinth.com/plugin/fastasyncworldedit. Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Start Initialization
        instance = this;

        // Register with Skript
        SkriptAddon addon = Skript.instance().registerAddon(SkWorldEdit.class, "SkWorldEdit");
        addon.localizer().setSourceDirectories("lang", null);
        addon.loadModules(this);

        // Preload fawe relighter factory
        preLoadRelighterFactory();

        // Check for plugin updates from GitHub
        new UpdateChecker(this, "sorya17", "SkriptWorldEdit").checkForUpdates();

        getLogger().info("SkWorldEdit has been enabled");
    }

    @Override
    public void init(SkriptAddon skriptAddon) {
        ClassLoader.builder()
            .basePackage("me.tecspace.skworldedit.types")
            .deep(false)
            .initialize(true)
            .forEachClass(clazz -> {
                try {
                    clazz.getMethod("register", SkriptAddon.class).invoke(null, skriptAddon);
                } catch (NoSuchMethodException ignored) {
                } catch (IllegalAccessException | InvocationTargetException e) {
                    getLogger().severe("Failed to load type class: " + clazz.getSimpleName());
                }
            })
            .build()
            .loadClasses(SkWorldEdit.class, getFile());
    }

    @Override
    public void load(SkriptAddon skriptAddon) {
        ClassLoader.builder()
            .basePackage("me.tecspace.skworldedit.elements")
            .deep(true)
            .initialize(true)
            .forEachClass(clazz -> {
                if (SyntaxElement.class.isAssignableFrom(clazz)) {
                    try {
                        clazz.getMethod("register", SyntaxRegistry.class).invoke(null, skriptAddon.syntaxRegistry());
                    } catch (NoSuchMethodException ignored) {
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        getLogger().severe("Failed to load syntax class: " + e.getMessage());
                    }
                }
            })
            .build()
            .loadClasses(SkWorldEdit.class, getFile());
    }

    @Override
    public String name() {
        return "SkWorldEdit";
    }

    private static void preLoadRelighterFactory() {
        if (!UsesFastAsyncWorldEdit) return;
        Platform platform = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);
        if (platform == null) return;
        Utils.log("Pre-loading FastAsyncWorldEdit's relighter factory");
        try {
            Method relighterMethod = platform.getClass().getMethod("getRelighterFactory");
            Object relighterFactory = relighterMethod.invoke(platform);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Utils.log("Failed to pre-load Relighter factory: " + e.getMessage());
        }
    }
}
