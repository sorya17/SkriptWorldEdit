package me.tecspace.skworldedit.elements.Player;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Player - Region Selection")
@Description("""
        Gets the selected region of a player.
        Selections are world-specific, so optionally you can provide the world you want. By default it will use the normal world.
        Note that the selection must be complete for this to return a region (example: has both points in a cuboid selection).
        """)
@Example("""
        set {_region} to region selection of player
        set {_region} to region selection of player in world "world_nether"
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprRegionSelection extends SimpleExpression<RegionWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprRegionSelection.class, RegionWrapper.class)
                .supplier(ExprRegionSelection::new)
                .addPattern("[the] (region selection|selected region)[s] of %players% [in [world] %-world%]")
                .build());
    }

    private Expression<Player> playersExpr;
    private @Nullable Expression<World> worldExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        playersExpr = (Expression<Player>) exprs[0];
        worldExpr = (exprs.length > 1) ? (Expression<World>) exprs[1] : null;
        return true;
    }

    @Override
    protected @Nullable RegionWrapper[] get(Event event) {
        List<RegionWrapper> regions = new ArrayList<>();
        World world = (worldExpr != null) ? worldExpr.getSingle(event) : Bukkit.getWorlds().getFirst();
        if (world == null) return null;
        for (Player player : playersExpr.getAll(event)) {
            try {
                Region region = PlayerUtils.getRegion(player, BukkitAdapter.adapt(world));
                regions.add(new RegionWrapper(region, BukkitAdapter.adapt(world)));
            } catch (IncompleteRegionException ignored) {}
        }
        return regions.toArray(new RegionWrapper[0]);
    }

    @Override
    public boolean isSingle() {
        return playersExpr.isSingle();
    }

    @Override
    public Class<? extends RegionWrapper> getReturnType() {
        return RegionWrapper.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "selection of " + playersExpr.toString(event, debug);
    }
}