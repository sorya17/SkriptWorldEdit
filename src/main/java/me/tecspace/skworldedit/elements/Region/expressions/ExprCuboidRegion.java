package me.tecspace.skworldedit.elements.Region.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.tecspace.skworldedit.api.RegionWrapper;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Cuboid")
@Description("""
        Creates a cuboid region, which can be used for operations.
        A cuboid region is a simple box with any width, length and height.
        """)
@Example("set {_region} to region between {_loc1} and {_loc2}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprCuboidRegion extends SimpleExpression<RegionWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, DefaultSyntaxInfos.Expression.builder(ExprCuboidRegion.class, RegionWrapper.class)
                .supplier(ExprCuboidRegion::new)
                .addPattern("[a] [new] [cuboid] region (between|within|from) %location% (and|to) %location%")
                .build()
        );
    }

    private Expression<Location> exprLoc1;
    private Expression<Location> exprLoc2;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        exprLoc1 = (Expression<Location>) exprs[0];
        exprLoc2 = (Expression<Location>) exprs[1];
        return true;
    }

    @Override
    protected RegionWrapper @Nullable [] get(Event event) {
        Location loc1 = exprLoc1.getSingle(event);
        Location loc2 = exprLoc2.getSingle(event);

        if (loc1 == null || loc2 == null || loc1.getWorld() != loc2.getWorld()) return null;

        BlockVector3 pos1 = BlockVector3.at(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ());
        BlockVector3 pos2 = BlockVector3.at(loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());

        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(loc1.getWorld()), pos1, pos2);
        RegionWrapper wrapper = new RegionWrapper(region, BukkitAdapter.adapt(loc1.getWorld()));

        return new RegionWrapper[]{wrapper};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends RegionWrapper> getReturnType() {
        return RegionWrapper.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "cuboid region between " + exprLoc1.toString(event, debug) + " and " + exprLoc2.toString(event, debug);
    }

}