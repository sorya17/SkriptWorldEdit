package me.tecspace.skworldedit.elements.Region.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.world.World;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.utils.Utils;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Convex polyhedral")
@Description("""
        Creates a convex polyhedral region, which can be used for operations.
        The region will form in the order of the locations given.
        You may add or remove vertices using the respective expression later.
        """)
@Example("set {_region} to a new convex polyhedral region from {_locations::*}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprConvexPolyhedralRegion extends SimpleExpression<RegionWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, DefaultSyntaxInfos.Expression.builder(ExprConvexPolyhedralRegion.class, RegionWrapper.class)
                .supplier(ExprConvexPolyhedralRegion::new)
                .addPattern("[a] [new] convex poly[hedral] region (from|with|using) [vertices] %locations%")
                .build()
        );
    }

    private Expression<Location> locationsExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        this.locationsExpr = (Expression<Location>) exprs[0];
        return true;
    }

    @Override
    protected RegionWrapper @Nullable [] get(Event event) {
        if (this.locationsExpr == null) return null;

        Location[] vertices = this.locationsExpr.getArray(event);
        if (vertices.length == 0) return null;

        World world = BukkitAdapter.adapt(vertices[0].getWorld());

        ConvexPolyhedralRegion region = new ConvexPolyhedralRegion(world);

        for (Location loc : vertices) {
            region.addVertex(Utils.toBlockVector3(loc));
        }

        RegionWrapper wrapper = new RegionWrapper(region, world);
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
    public String toString(@Nullable Event event, boolean b) {
        return "convex polyhedral region";
    }
}
