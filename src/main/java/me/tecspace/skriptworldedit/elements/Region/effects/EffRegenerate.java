package me.tecspace.skriptworldedit.elements.Region.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Regenerate")
@Description("""
        Regenerates the area in a region, optionally with a set seed.
        If no seed is given it will use the seed of the region's world.
        By default it will not include biomes.
        NOTE that regenerating areas can be resource intensive
""")
@Examples("""
        regenerate {_region}
        regenerate {_region} including biomes
        regenerate {_region} using seed 5146159088207717555 including biomes
""")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffRegenerate extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffRegenerate.class)
                .supplier(EffRegenerate::new)
                .addPattern("[:lazily] regenerate [region] %worldeditregions% [using seed %-number%] [:including biomes]")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private @Nullable Expression<Number> seedExpr;
    private boolean includingBiomes;
    private boolean async;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        seedExpr = (Expression<Number>) exprs[1];
        includingBiomes = parseResult.hasTag("including biomes");
        async = !parseResult.hasTag("lazily");
        if (async && !SkriptWorldEdit.UsesFastAsyncWorldEdit) {
            Skript.warning("Async is only supported with FastAsyncWorldEdit. The operation will run synchronously.");
            async = false;
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        RegionWrapper[] regions = regionExpr.getArray(event);
        if (regions == null) return;
        Long seed = (seedExpr != null && seedExpr.getSingle(event) != null)
                ? seedExpr.getSingle(event).longValue()
                : null;
        for (RegionWrapper region : regions) {
            region.regenerate(seed, includingBiomes, async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "" : "lazily ") + "regenerate region";
    }
}
