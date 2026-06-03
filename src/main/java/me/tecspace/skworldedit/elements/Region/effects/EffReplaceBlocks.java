package me.tecspace.skworldedit.elements.Region.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.utils.MaskUtils;
import me.tecspace.skworldedit.api.utils.PatternUtils;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.lang.ConditionalAsyncEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Replace Blocks")
@Description("Replace certain blocks in a region.")
@Examples("""
        replace dirt with air in region {_region}
        replace dirt in region {_region} with stone, dirt
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffReplaceBlocks extends ConditionalAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffReplaceBlocks.class)
                .supplier(EffReplaceBlocks::new)
                // a lot of conflicts with Skript's replace in string
                .addPattern("[:lazily] replace " + MaskUtils.MASK_SOURCE_TYPES + " with " + PatternUtils.PARSABLE_TYPES_STRING + " in region " + getString())
                .addPattern("[:lazily] replace " + MaskUtils.MASK_SOURCE_TYPES + " in region %worldeditregions% with " + PatternUtils.PARSABLE_TYPES_STRING)
                //.addPattern("[:lazily] replace [all] blocks matching " + MaskWrapper.MASK_SOURCE_TYPES + " with " + PatternWrapper.PARSABLE_TYPES_STRING + "in %worldeditregions%")
                .build());
    }

    private static @NonNull String getString() {
        return "%worldeditregions%";
    }

    private Expression<?> maskExpr;
    private Expression<?> patternExpr;
    private Expression<RegionWrapper> regionExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        maskExpr = exprs[0];
        patternExpr = exprs[matchedPattern + 1];
        regionExpr = (Expression<RegionWrapper>) exprs[matchedPattern == 0 ? 2 : 1];
        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (maskExpr == null || regionExpr == null) return;

        Mask mask = MaskUtils.parseFrom(maskExpr.getArray(event));
        if (mask == null) {
            error("Couldn't parse mask '" + maskExpr.toString(event, false) + "'. Make sure it's a valid mask.");
            return;
        }

        Pattern pattern = PatternUtils.parseFrom(patternExpr.getArray(event));
        if (pattern == null) {
            error("Couldn't parse pattern '" + patternExpr.toString(event, false) + "'. Make sure it's a valid pattern.");
            return;
        }

        for (RegionWrapper region : regionExpr.getArray(event)) {
            region.replaceBlocks(mask, pattern);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (!isAsync() ? "lazily " : "") + "replace " + maskExpr.toString(event, debug) + " with " + patternExpr.toString(event, debug) + " in " + regionExpr.toString(event, debug);
    }
}
