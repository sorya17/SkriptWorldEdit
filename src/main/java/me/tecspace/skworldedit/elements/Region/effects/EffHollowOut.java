package me.tecspace.skworldedit.elements.Region.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.pattern.Pattern;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.utils.PatternUtils;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.lang.ConditionalAsyncEffect;
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
public class EffHollowOut extends ConditionalAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffHollowOut.class)
                .supplier(EffHollowOut::new)
                .addPattern("[:lazily] hollow out %worldeditregions% [(using|leaving behind) " + PatternUtils.PARSABLE_TYPES_STRING + "] [with [a] thickness of %-integer%]")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private @Nullable Expression<?> patternExpr;
    private @Nullable Expression<Integer> thicknessExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        patternExpr = exprs[1];
        thicknessExpr = (Expression<Integer>) exprs[2];
        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (patternExpr == null) return;

        Pattern pattern = PatternUtils.parseFrom(patternExpr.getArray(event));
        if (pattern == null) {
            error("Couldn't parse pattern '" + patternExpr.toString(event, false) + "'. Make sure it's a valid pattern.");
            return;
        }

        Integer thicknessNum = thicknessExpr != null ? thicknessExpr.getSingle(event) : null;
        int thickness = thicknessNum != null ? thicknessNum : 1;

        for (RegionWrapper region : regionExpr.getAll(event)) {
            region.makeHollow(pattern, null, thickness);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (!isAsync() ? "lazily " : "") + "hollow out " + regionExpr.toString(event, debug);
    }
}
