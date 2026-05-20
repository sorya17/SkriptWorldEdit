package me.tecspace.skriptworldedit.api.utils;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PlayerUtils {

    public static SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();

    public static LocalSession getSession(Player player) {
        Actor actor = BukkitAdapter.adapt(player);
        return sessionManager.getIfPresent(actor);
    }

    public static @Nullable Region getRegion(Player player) {
        return getRegion(player, null);
    }

    public static @Nullable Region getRegion(Player player, @Nullable World world) {
        LocalSession session = getSession(player);
        world = (world == null) ? session.getSelectionWorld(): world;
        if (world == null) throw new IncompleteRegionException();
        return session.getSelection(world);
    }
}
