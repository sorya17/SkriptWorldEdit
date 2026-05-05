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

@Name("Region - Set Blocks")
@Description("Set the blocks in a region.")
@Examples("""
        set blocks in {_region} to dirt
        set blocks in {_region} to sculk_catalyst[bloom=true]
        set blocks in {_region} to pattern of stone, cobblestone and andesite
        set blocks in {_region} to pattern from "#simplex[5][dirt,coarse_dirt,rooted_dirt]"
""")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffSetBlocks extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSetBlocks.class)
                .supplier(EffSetBlocks::new)
                .addPattern("[:lazily] set [all] [the] blocks (of|in) [region] %worldeditregions% to " + PatternWrapper.PARSABLE_TYPES_STRING)
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
        for (RegionWrapper region : regionExpr.getArray(event)) {
            region.setBlocks(pattern, async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "" : "lazily ") + "set blocks in region " + regionExpr.toString(event, debug) + " to " + patternExpr.toString(event, debug);
    }
}
