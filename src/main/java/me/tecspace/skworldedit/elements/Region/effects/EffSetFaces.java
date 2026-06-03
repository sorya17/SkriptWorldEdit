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

@Name("Region - Set Faces")
@Description("Set the faces of a region.")
@Examples("""
        set faces of {_region} to dirt
        set faces of {_region} to sculk_catalyst[bloom=true]
        set faces of {_region} to pattern "#simplex[5][dirt,coarse_dirt,rooted_dirt]"
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffSetFaces extends ConditionalAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSetFaces.class)
                .supplier(EffSetFaces::new)
                .addPattern("[:lazily] set [the] faces of [region] %worldeditregions% to " + PatternUtils.PARSABLE_TYPES_STRING)
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private Expression<?> patternExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        patternExpr = exprs[1];
        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
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
            region.makeFaces(pattern, null);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (!isAsync() ? "lazily " : "") + "set faces of region " + regionExpr.toString(event, debug) + " to " + patternExpr.toString(event, debug);
    }
}
