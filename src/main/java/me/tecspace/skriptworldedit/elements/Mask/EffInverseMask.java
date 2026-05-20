package me.tecspace.skriptworldedit.elements.Mask;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.mask.Mask;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mask - Inverse")
@Description("Makes the mask match everything except what it currently matches.")
@Example("""
        set {_mask} to mask of dirt, stone and cobblestone
        inverse {_mask}
        # {_mask} now matches anything except dirt, stone and cobblestone
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffInverseMask extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffInverseMask.class)
                .supplier(EffInverseMask::new)
                .addPattern("(inverse|invert|negate) %worldeditmasks%")
                .build());
    }

    private Expression<Mask> maskExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        maskExpr = (Expression<Mask>) exprs[0];
        return true;
    }

    @Override
    protected void execute(Event event) {
        for (Mask mask : maskExpr.getArray(event)) {
            mask.inverse();
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "inverse mask";
    }
}
