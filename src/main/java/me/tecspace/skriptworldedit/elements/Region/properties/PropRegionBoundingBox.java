package me.tecspace.skriptworldedit.elements.Region.properties;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Region - Bounding box")
@Description("""
        Creates a new cuboid region from the bounding box of a region.
        If the region is already a cuboid, it will return the same region.
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class PropRegionBoundingBox extends PropertyExpression<RegionWrapper, RegionWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(
                SyntaxRegistry.EXPRESSION,
                infoBuilder(
                        PropRegionBoundingBox.class,
                        RegionWrapper.class,
                        "(bounding box|cuboid) [region]",
                        "worldeditregions",
                        false
                )
                        .supplier(PropRegionBoundingBox::new)
                        .build()
        );
    }

    @Override
    protected RegionWrapper[] get(Event event, RegionWrapper[] regions) {
        List<RegionWrapper> wrappers = new ArrayList<>();
        for (RegionWrapper region : regions) {
            RegionWrapper wrapper = new RegionWrapper(region.region().getBoundingBox(), region.world());
            wrappers.add(wrapper);
        }
        return wrappers.toArray(new RegionWrapper[0]);
    }

    @Override
    public boolean isSingle() {
        return getExpr().isSingle();
    }

    @Override
    public Class<? extends RegionWrapper> getReturnType() {
        return RegionWrapper.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "bounding box region of " + getExpr().toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends RegionWrapper>) expressions[0]);
        return true;
    }
}