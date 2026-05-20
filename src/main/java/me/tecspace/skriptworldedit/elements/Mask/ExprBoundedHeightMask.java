package me.tecspace.skriptworldedit.elements.Mask;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.mask.BoundedHeightMask;
import com.sk89q.worldedit.function.mask.Mask;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mask - Bounded Height Mask")
@Description("A Mask that matches only blocks within a range of Y values")
@Example("set {_mask} to a bounded height mask between 0 and 63")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprBoundedHeightMask extends SimpleExpression<Mask> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprBoundedHeightMask.class, Mask.class)
                .supplier(ExprBoundedHeightMask::new)
                .addPattern("[a] bounded height mask (between|from) %integer% (and|to) %integer%")
                .build());
    }

    private Expression<Integer> firstExpression;
    private Expression<Integer> secondExpression;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        firstExpression = (Expression<Integer>) exprs[0];
        secondExpression = (Expression<Integer>) exprs[1];
        return true;
    }

    @Override
    protected Mask @Nullable [] get(Event event) {
        Integer first = firstExpression.getSingle(event);
        Integer second = secondExpression.getSingle(event);
        if (first == null || second == null) return null;

        BoundedHeightMask mask = new BoundedHeightMask(Math.min(first, second), Math.max(first, second));
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
        return "bounded height mask";
    }
}
