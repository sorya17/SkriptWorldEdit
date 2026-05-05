package me.tecspace.skriptworldedit.elements.Region.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import me.tecspace.skriptworldedit.api.MaskWrapper;
import me.tecspace.skriptworldedit.api.PatternWrapper;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Set Walls")
@Description("Sets the walls of a region, optionally with a mask.")
@Examples("""
        set walls of {_region} to dirt
        set walls of {_region} to sculk_catalyst[bloom=true]
        set walls of {_region} to pattern "#simplex[5][dirt,coarse_dirt,rooted_dirt]"
        set walls of {_region} to white_concrete with mask of air
""")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffSetWalls extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSetWalls.class)
                .supplier(EffSetWalls::new)
                .addPattern("[:lazily] set [the] walls of [region] %worldeditregions% to " + PatternWrapper.PARSABLE_TYPES_STRING + " [with %-worldeditmask%]")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private Expression<?> patternExpr;
    private @Nullable Expression<MaskWrapper> maskExpr;
    private boolean async;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        patternExpr = exprs[1];
        maskExpr =  (Expression<MaskWrapper>) exprs[2];
        async = !parseResult.hasTag("lazily");
        if (async && !SkriptWorldEdit.UsesFastAsyncWorldEdit) {
            Skript.warning("Async is only supported with FastAsyncWorldEdit. The operation will run synchronously.");
            async = false;
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        PatternWrapper pattern = PatternWrapper.from(patternExpr.getArray(event));
        if (pattern == null) return;
        MaskWrapper mask = (maskExpr != null) ? maskExpr.getSingle(event) : null;
        for (RegionWrapper region : regionExpr.getArray(event)) {
            region.makeWalls(pattern, mask, async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "" : "lazily ") + "set the walls of region " + regionExpr.toString(event, debug) + " to " + patternExpr.toString(event, debug);
    }
}
