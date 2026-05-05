package me.tecspace.skriptworldedit.elements.Region.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import me.tecspace.skriptworldedit.api.PatternWrapper;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Hollow out")
@Description("Hollows out a region optionally using a pattern, thickness and mask.")
@Examples("""
        hollow out {_region}
        hollow out {_region} using dirt
        hollow out {_region} with thickness of 2
        hollow out {_region} using stone with thickness of 3
""")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffHollowOut extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffHollowOut.class)
                .supplier(EffHollowOut::new)
                .addPattern("[:lazily] hollow out %worldeditregions% [(using|leaving behind) " + PatternWrapper.PARSABLE_TYPES_STRING + "] [with [a] thickness of %-integer%]")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private @Nullable Expression<?> patternExpr;
    private @Nullable Expression<Integer> thicknessExpr;
    private boolean async;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        patternExpr = exprs[1];
        thicknessExpr = (Expression<Integer>) exprs[2];
        async = !parseResult.hasTag("lazily");
        if (async && !SkriptWorldEdit.UsesFastAsyncWorldEdit) {
            Skript.warning("Async is only supported with FastAsyncWorldEdit. The operation will run synchronously.");
            async = false;
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        PatternWrapper pattern = (patternExpr != null) ? PatternWrapper.from(patternExpr.getArray(event)) : null;
        if (patternExpr != null && pattern == null) return;

        Integer thicknessNum = thicknessExpr != null ? thicknessExpr.getSingle(event) : null;
        int thickness = thicknessNum != null ? thicknessNum : 1;

        for (RegionWrapper region : regionExpr.getAll(event)) {
            region.makeHollow(pattern, null, thickness, async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "" : "lazily ") + "hollow out " + regionExpr.toString(event, debug);
    }
}
