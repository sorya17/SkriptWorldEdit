package me.tecspace.skriptworldedit.elements.Region.properties;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;// implement getMaximumPoint and getMinimumPoint

@Name("Region - Minimum & Maximum point")
@Description("The 2 corners of a region.")
@Example("set {_origin} to minimum point of {_region}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class PropRegionPoints extends SimplePropertyExpression<RegionWrapper, Location> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(PropRegionPoints.class, Location.class,
                "(:min[imum]|max[imum]) point[s]", "worldeditregions", false)
                .supplier(PropRegionPoints::new)
                .build());
    }

    private boolean isMin;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        isMin = parseResult.hasTag("min");
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Location convert(RegionWrapper wrapper) {
        BlockVector3 point;
        if (isMin) {
            point = wrapper.region().getMinimumPoint();
        } else {
            point = wrapper.region().getMaximumPoint();
        }
        return new Location(
                BukkitAdapter.adapt(wrapper.world()),
                point.x(),
                point.y(),
                point.z()
        );
    }

    @Override
    public Class<? extends Location> getReturnType() {
        return Location.class;
    }

    @Override
    protected String getPropertyName() {
        return "region point";
    }
}
