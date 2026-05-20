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
public class EffOverlay extends ConditionalAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffOverlay.class)
                .supplier(EffOverlay::new)
                .addPattern("[:lazily] overlay %worldeditregions% (with|using) [pattern] " + PatternUtils.PARSABLE_TYPES_STRING)
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
        if (regionExpr == null) return;

        Pattern pattern = PatternUtils.parseFrom(patternExpr.getArray(event));
        if (pattern == null) {
            error("Couldn't parse pattern '" + patternExpr.toString(event, false) + "'. Make sure it's a valid pattern.");
            return;
        }

        for (RegionWrapper region : regionExpr.getAll(event)) {
            region.overlay(pattern);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (!isAsync() ? "lazily " : "") + "overlay " + regionExpr.toString(event, debug) + " with " + patternExpr.toString(event, debug);
    }
}
