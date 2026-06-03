package me.tecspace.skworldedit.elements.Region.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.lang.ConditionalAsyncEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Make Snow")
@Description("Creates snow layers and ice in the region. Smooth will make smooth layers.")
@Examples("""
        make snow in {_region}
        make smooth snow in {_region}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffSimulateSnow extends ConditionalAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSimulateSnow.class)
                .supplier(EffSimulateSnow::new)
                .addPattern("[:lazily] make (:smooth snow|snow) in %worldeditregions%")
                 .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private boolean smooth;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        smooth = parseResult.hasTag("smooth snow");
        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (regionExpr == null) return;

        for (RegionWrapper region : regionExpr.getAll(event)) {
            if (smooth) {
                region.simulateSmoothSnow(1, 1, null);
            } else {
                region.simulateSnow();
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (smooth) {
            return (!isAsync() ? "lazily " : "") + "make smooth snow in " + regionExpr.toString(event, debug);
        } else {
            return (!isAsync() ? "lazily " : "") + "make snow in " + regionExpr.toString(event, debug);
        }
    }
}