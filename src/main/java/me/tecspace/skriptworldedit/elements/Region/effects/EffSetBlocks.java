package me.tecspace.skriptworldedit.elements.Region.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.pattern.Pattern;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import me.tecspace.skriptworldedit.api.utils.PatternUtils;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import me.tecspace.skriptworldedit.api.lang.ConditionalAsyncEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Set Blocks")
@Description("Sets all blocks in the region.")
@Examples("""
        set blocks in {_region} to dirt
        set blocks in {_region} to sculk_catalyst[bloom=true]
        set blocks in {_region} to pattern of stone, cobblestone and andesite
        set blocks in {_region} to pattern from "#simplex[5][dirt,coarse_dirt,rooted_dirt]"
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffSetBlocks extends ConditionalAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSetBlocks.class)
                .supplier(EffSetBlocks::new)
                .addPattern("[:lazily] set [all] [the] blocks (of|in) [region] %worldeditregions% to " + PatternUtils.PARSABLE_TYPES_STRING)
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private Expression<?> patternExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        patternExpr = exprs[1];
        setAsync(!parseResult.hasTag("lazily") && SkriptWorldEdit.UsesFastAsyncWorldEdit);
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (patternExpr == null || regionExpr == null) return;

        Pattern pattern = PatternUtils.parseFrom(patternExpr.getArray(event));
        if (pattern == null) {
            error("Couldn't parse pattern '" + patternExpr.toString(event, false) + "'. Make sure it's a valid pattern.");
            return;
        }

        for (RegionWrapper region : regionExpr.getArray(event)) {
            region.setBlocks(pattern);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (!isAsync() ? "lazily " : "") + "set blocks in region " + regionExpr.toString(event, debug) + " to " + patternExpr.toString(event, debug);
    }
}
