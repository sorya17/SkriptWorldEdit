package me.tecspace.skriptworldedit.elements.Mask;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.fastasyncworldedit.core.function.mask.InverseMask;
import me.tecspace.skriptworldedit.api.MaskWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Mask - Inverse Mask")
@Description("""
        Returns a mask that matches everything the given mask does not match.
        This Expression can be useful when constructing single line masks that should be inverse.
        Example: 'inverse mask of dirt'
        """)
@Example("set {_mask} to inverse mask of dirt, stone and cobblestone")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprInverseMask extends SimpleExpression<MaskWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprInverseMask.class, MaskWrapper.class)
                .supplier(ExprInverseMask::new)
                .addPattern("(inverse|inverted|negated) %worldeditmasks%")
                .build());
    }

    private Expression<MaskWrapper> wrapperSource;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        wrapperSource = (Expression<MaskWrapper>) exprs[0];
        return true;
    }

    @Override
    protected MaskWrapper @Nullable [] get(Event event) {
        if (wrapperSource == null) return null;
        MaskWrapper[] wrappers = wrapperSource.getArray(event);
        List<MaskWrapper> maskWrappers = new ArrayList<>();
        for (MaskWrapper wrapper : wrappers) {
            InverseMask mask = new InverseMask(wrapper.mask());
            maskWrappers.add(new MaskWrapper(mask));
        }
        return maskWrappers.toArray(new MaskWrapper[0]);
    }

    @Override
    public boolean isSingle() {
        return wrapperSource.isSingle();
    }

    @Override
    public Class<? extends MaskWrapper> getReturnType() {
        return MaskWrapper.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "inversed mask";
    }
}
