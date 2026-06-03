package me.tecspace.skworldedit.elements.Region.properties;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.math.BlockVector3;
import me.tecspace.skworldedit.api.RegionWrapper;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Region - Dimensions")
@Description("""
        The dimensions of a region. Returns a vector.
        This will always be the bounding box dimensions of any region, no matter the shape.
        """)
@Example("set {_scale} to dimensions of {_region}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class PropRegionDimensions extends PropertyExpression<RegionWrapper, Vector> {

    public static void register(SyntaxRegistry registry) {
        registry.register(
                SyntaxRegistry.EXPRESSION,
                infoBuilder(
                        PropRegionDimensions.class,
                        Vector.class,
                        "[region] dimension[s]",
                        "worldeditregions",
                        false
                )
                        .supplier(PropRegionDimensions::new)
                        .build()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends RegionWrapper>) expressions[0]);
        return true;
    }

    @Override
    protected Vector[] get(Event event, RegionWrapper[] regions) {
        List<Vector> dimensions = new ArrayList<>();
        for (RegionWrapper region : regions) {
            BlockVector3 dim = region.region().getDimensions();
            dimensions.add(new Vector(dim.x(), dim.y(), dim.z()));
        }
        return dimensions.toArray(new Vector[0]);
    }

    @Override
    public boolean isSingle() {
        return getExpr().isSingle();
    }

    @Override
    public Class<? extends Vector> getReturnType() {
        return Vector.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "dimensions of " + getExpr().toString(event, debug);
    }
}