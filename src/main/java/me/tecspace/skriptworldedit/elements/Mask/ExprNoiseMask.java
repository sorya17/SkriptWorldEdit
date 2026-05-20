package me.tecspace.skriptworldedit.elements.Mask;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.fastasyncworldedit.core.function.mask.SimplexMask;
import com.sk89q.worldedit.function.mask.Mask;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mask - Simplex Noise Mask")
@Description("A simplex mask that can be used in operations. (FAWE only!)")
@Example("set {_mask} to simplex noise mask with scale 6, min 7 and max 9")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprNoiseMask extends SimpleExpression<Mask> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprNoiseMask.class, Mask.class)
                .supplier(ExprNoiseMask::new)
                .addPattern("[a] (simplex [noise]|noise) mask with scale %number%(,| and) min %number%(,| and) max %number%")
                .build());
    }

    private Expression<Number> scaleExpr;
    private Expression<Number> minExpr;
    private Expression<Number> maxExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!SkriptWorldEdit.UsesFastAsyncWorldEdit) {
            error("This expression requires the FastAsyncWorldEdit plugin to be installed.");
            return false;
        }
        scaleExpr = (Expression<Number>) exprs[0];
        minExpr = (Expression<Number>) exprs[1];
        maxExpr = (Expression<Number>) exprs[2];
        return true;
    }

    @Override
    protected @Nullable Mask[] get(Event event) {
        Number scale = scaleExpr.getSingle(event);
        Number min = minExpr.getSingle(event);
        Number max = maxExpr.getSingle(event);
        if (scale == null || min == null || max == null) return null;

        SimplexMask mask = new SimplexMask(scale.doubleValue(), min.doubleValue(), max.doubleValue());
        return new Mask[]{mask};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Mask> getReturnType() {
        return Mask.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "noise mask with scale " + scaleExpr.toString(event, debug) + " and " +  minExpr.toString(event, debug) + " and " + maxExpr.toString(event, debug);
    }
}
