package me.tecspace.skriptworldedit.elements.Mask;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.MaskUnion;
import me.tecspace.skriptworldedit.api.MaskWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Mask - Combined Mask")
@Description("""
        Combines multiple masks to use as a single mask.
        'all' requires that all masks return true when a certain position is tested. It serves as a logical AND operation on a list of masks.
        'any' requires that one or more masks return true when a certain position is tested. It serves as a logical OR operation on a list of masks.
        """)
@Example("""
        # this mask only matches stone blocks exposed to the surface
        set {_mask} to a combined mask matching all of (mask of stone, surface mask)
        # this mask matches stone blocks AND blocks exposed to the surface
        set {_mask} to a combined mask matching any of (mask of stone, surface mask)
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprCombineMasks extends SimpleExpression<MaskWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprCombineMasks.class, MaskWrapper.class)
                .supplier(ExprCombineMasks::new)
                .addPattern("[a] [combined] mask (that matches|matching) (:all|any) of %worldeditmasks%")
                .build());
    }

    private boolean matchAll;
    private Expression<MaskWrapper> wrapperSource;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        matchAll = parseResult.hasTag("all");
        wrapperSource =  (Expression<MaskWrapper>) expressions[0];
        return true;
    }

    @Override
    protected MaskWrapper @Nullable [] get(Event event) {
        if (wrapperSource == null) return null;
        MaskWrapper[] wrappers = wrapperSource.getArray(event);
        if (wrappers == null || wrappers.length == 0) return null;

        List<Mask> maskList = new ArrayList<>();
        for (MaskWrapper wrapper : wrappers) {
            if (wrapper != null && wrapper.mask() != null) {
                maskList.add(wrapper.mask());
            }
        }

        if (maskList.isEmpty()) return null;

        Mask combinedMask;
        if (matchAll) {
            combinedMask = new MaskIntersection(maskList);
        } else {
            combinedMask = new MaskUnion(maskList);
        }

        return new MaskWrapper[]{new MaskWrapper(combinedMask)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends MaskWrapper> getReturnType() {
        return MaskWrapper.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "combined mask";
    }
}
