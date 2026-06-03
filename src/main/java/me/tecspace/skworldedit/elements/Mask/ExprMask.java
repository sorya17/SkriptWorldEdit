package me.tecspace.skworldedit.elements.Mask;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.mask.Mask;
import me.tecspace.skworldedit.api.utils.MaskUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mask - Create")
@Description("""
        Creates a mask that can be used to affect only specific blocks in operations.
        https://worldedit.enginehub.org/en/latest/usage/general/masks/
        """)
@Example("""
        set {_mask} to mask of dirt, grass_block and piston[facing=west]
        set {_mask} to mask of tag "logs", "beds" and "paper:stained_glass"
        set {_mask} to mask of farmland and tag "crops"
        set {_mask} to mask that matches "!##beds,>grass_block,barrier[waterlogged=true]"
        set {_mask} to mask of plains, desert and river
        set {_mask} to mask of region between location(0,0,0) and location(10,10,10)
        set {_mask} to inverse mask of waxed weathered cut copper stairs
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprMask extends SimpleExpression<Mask> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprMask.class, Mask.class)
                .supplier(ExprMask::new)
                .addPattern("[a] mask (of|from|that matches) " + MaskUtils.MASK_SOURCE_TYPES)
                .build());
    }

    private Expression<?> source;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        source = exprs[0];
        return true;
    }

    @Override
    protected @Nullable Mask[] get(Event event) {
        Mask mask = MaskUtils.parseFrom(source.getArray(event));
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
        return "mask of " + source.toString(event, debug);
    }
}
