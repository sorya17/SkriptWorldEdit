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

@Name("Region - Volume")
@Description("""
        The total amount of blocks in a region.
        To count specific blocks or apply a mask, use the Count Blocks expression.
        """)
@Example("set {_blocks} to volume of {_region}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class PropRegionVolume extends PropertyExpression<RegionWrapper, Long> {

    public static void register(SyntaxRegistry registry) {
        registry.register(
                SyntaxRegistry.EXPRESSION,
                infoBuilder(
                        PropRegionVolume.class,
                        Long.class,
                        "[region] (volume|area)",
                        "worldeditregions",
                        false
                )
                        .supplier(PropRegionVolume::new)
                        .build()
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends RegionWrapper>) expressions[0]);
        return true;
    }

    @Override
    protected Long[] get(Event event, RegionWrapper[] regions) {
        List<Long> volumes = new ArrayList<>();
        for (RegionWrapper region : regions) {
            volumes.add(region.region().getVolume());
        }
        return volumes.toArray(new Long[0]);
    }

    @Override
    public boolean isSingle() {
        return getExpr().isSingle();
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "volume of " + getExpr().toString(event, debug);
    }
}