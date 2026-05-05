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

@Name("Region - Overlay")
@Description("Overlays the top blocks in a region with a pattern.")
@Examples("""
        overlay {_region} with grass block
        overlay {_region} with cobblestone, stone and gravel
        overlay {_region} with pattern "90%%stone,10%%cobblestone"
        overlay {_region} with pattern "#perlin[5][rooted_dirt,dirt,coarse_dirt]"
        overlay {_region} with {_pattern}
""")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffOverlay extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffOverlay.class)
                .supplier(EffOverlay::new)
                .addPattern("[:lazily] overlay %worldeditregions% (with|using) [pattern] " + PatternWrapper.PARSABLE_TYPES_STRING)
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private Expression<?> patternExpr;
    private boolean async;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        patternExpr = exprs[1];
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
        for (RegionWrapper region : regionExpr.getAll(event)) {
            region.overlay(pattern, async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "" : "lazily ") + "overlay " + regionExpr.toString(event, debug) + " with " + patternExpr.toString(event, debug);
    }
}
