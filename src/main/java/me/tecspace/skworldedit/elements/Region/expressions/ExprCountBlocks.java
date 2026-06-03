package me.tecspace.skworldedit.elements.Region.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.sk89q.worldedit.function.mask.Mask;
import me.tecspace.skworldedit.api.utils.MaskUtils;
import me.tecspace.skworldedit.api.RegionWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Count Blocks")
@Description("Counts the number of blocks in a region with a certain type or matching a given mask.")
@Examples("""
        set {_amount} to amount of dirt in {_region}
        set {_amount} to amount of barrier[waterlogged=true] in {_region}
        set {_amount} to number of blocks in {_region} that match {_mask}
        set {_amount} to number of blocks in {_region} that match mask of dirt, stone and andesite
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprCountBlocks extends SimpleExpression<Number> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprCountBlocks.class, Number.class)
                .supplier(ExprCountBlocks::new)
                .addPattern("[the] (count|number|amount) of blocks in %worldeditregions% (that match|matching) [mask] " + MaskUtils.MASK_SOURCE_TYPES)
                .addPattern("[the] (count|number|amount) of " + MaskUtils.MASK_SOURCE_TYPES + " [blocks] in %worldeditregions%")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private Expression<?> maskExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[matchedPattern];
        maskExpr = exprs[matchedPattern ^ 1];
        return true;
    }

    @Override
    protected @Nullable Number[] get(Event event) {
        Mask mask = MaskUtils.parseFrom(maskExpr.getArray(event));
        if (mask == null) {
            error("Couldn't parse mask '" + maskExpr.toString(event, false) + "'. Make sure it's a valid mask.");
            return null;
        }

        int count = 0;
        for (RegionWrapper region : regionExpr.getArray(event)) {
            count += region.countBlocks(mask);
        }
        return CollectionUtils.array(count);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "the count of " +  maskExpr.toString(event, debug) + " in " + regionExpr.toString(event, debug);
    }
}
