package me.tecspace.skworldedit.elements.Region.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.world.block.BlockTypes;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.lang.CondAsyncEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Cut")
@Description("Replaces all blocks in the region with air.")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffCut extends CondAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffCut.class)
                .supplier(EffCut::new)
                .addPattern("[:lazily] cut [all] blocks in %worldeditregions% [:and wait]")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean load(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
        setDelayed(parseResult.hasTag("and wait") && SkWorldEdit.UsesFastAsyncWorldEdit);
        this.regionExpr = (Expression<RegionWrapper>) exprs[0];
        return true;
    }

    @Override
    protected @Nullable Runnable runnable(Event event) {
        RegionWrapper[] regions = regionExpr.getAll(event);
        if (regions == null || regions.length == 0) {
            error("No region to cut was given.");
            return null;
        }
        return () -> {
            for (RegionWrapper region : regions) {
                try (EditSession session = WorldEdit.getInstance().newEditSession(region.world())) {
                    session.setBlocks(region.region(), BlockTypes.AIR);
                }
            }
        };
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (isAsync() ? "" : " lazily") + "cut " + regionExpr.toString(event, debug) + (isDelayed() ? " and wait" : "");
    }
}
