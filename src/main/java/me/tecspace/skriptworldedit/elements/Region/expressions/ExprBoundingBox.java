package me.tecspace.skriptworldedit.elements.Region.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Region - Bounding box")
@Description("""
        Creates a new cuboid region from the bounding box of a region.
        If the region is already a cuboid, it will return the same region.
        """)
@Example("set {_cuboid} to region from bounding box of {_region}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprBoundingBox extends SimpleExpression<RegionWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprBoundingBox.class, RegionWrapper.class)
                .supplier(ExprBoundingBox::new)
                .addPattern("[cuboid] region[s] from bounding box[es] of %worldeditregions%")
                .build());
    }

    private Expression<RegionWrapper> regionsExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        regionsExpr = (Expression<RegionWrapper>) exprs[0];
        return true;
    }

    @Override
    protected RegionWrapper [] get(Event event) {
        RegionWrapper[] wrappers = regionsExpr.getArray(event);
        List<RegionWrapper> cuboids = new ArrayList<>();
        for (RegionWrapper wrapper : wrappers) {
            cuboids.add(new RegionWrapper(wrapper.region().getBoundingBox(), wrapper.world()));
        }
        return cuboids.toArray(new RegionWrapper[0]);
    }

    @Override
    public boolean isSingle() {
        return regionsExpr.isSingle();
    }

    @Override
    public Class<? extends RegionWrapper> getReturnType() {
        return RegionWrapper.class;
    }

    @Override
    public boolean isLoopOf(String input) {
        return input.equalsIgnoreCase("region");
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "cuboid region from bounding box of " + regionsExpr.toString(event, debug);
    }
}
