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

@Name("Region - Replace Blocks")
@Description("Replace certain blocks in a region.")
@Examples("""
        replace dirt with air in region {_region}
        replace dirt in {_region} with stone, dirt
""")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffReplaceBlocks extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffReplaceBlocks.class)
                .supplier(EffReplaceBlocks::new)
                .addPattern("[:async] replace " + MaskWrapper.MASK_SOURCE_TYPES + " with " + PatternWrapper.PARSABLE_TYPES_STRING + " in region %worldeditregions%")
                .addPattern("[:async] replace " + MaskWrapper.MASK_SOURCE_TYPES + " in region %worldeditregions% with " + PatternWrapper.PARSABLE_TYPES_STRING)
                .build());
    }

    private Expression<?> maskExpr;
    private Expression<?> patternExpr;
    private Expression<RegionWrapper> regionExpr;
    private boolean async;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        maskExpr = exprs[0];
        patternExpr = exprs[matchedPattern + 1];
        regionExpr = (Expression<RegionWrapper>) exprs[matchedPattern == 0 ? 2 : 1];
        async = parseResult.hasTag("async");
        if (async && !SkriptWorldEdit.UsesFastAsyncWorldEdit) {
            Skript.warning("Async is only supported with FastAsyncWorldEdit. The operation will run synchronously.");
            async = false;
        }
        return true;
    }

    @Override
    protected void execute(Event event) {

        MaskWrapper mask = MaskWrapper.from(maskExpr.getArray(event));
        PatternWrapper pattern = PatternWrapper.from(patternExpr.getArray(event));

        if (mask == null || pattern == null) return;

        for (RegionWrapper region : regionExpr.getArray(event)) {
            region.replaceBlocks(mask, pattern, async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "async " : "") + "replace " + maskExpr.toString(event, debug) + " with " + patternExpr.toString(event, debug) + " in " + regionExpr.toString(event, debug);
    }
}
