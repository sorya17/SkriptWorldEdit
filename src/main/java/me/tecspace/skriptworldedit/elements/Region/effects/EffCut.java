package me.tecspace.skriptworldedit.elements.Region.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.world.block.BlockTypes;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import me.tecspace.skriptworldedit.api.lang.ConditionalAsyncEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Cut")
@Description("Replaces all blocks in the region with air.")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffCut extends ConditionalAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffCut.class)
                .supplier(EffCut::new)
                .addPattern("[:lazily] cut %worldeditregions%")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        setAsync(!parseResult.hasTag("lazily") && SkriptWorldEdit.UsesFastAsyncWorldEdit);
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (regionExpr == null) return;

        for (RegionWrapper region : regionExpr.getAll(event)) {
            region.setBlocks(BlockTypes.AIR);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (!isAsync() ? "lazily " : "") + "cut " + regionExpr.toString(event, debug);
    }
}
