package me.tecspace.skworldedit.elements.Region.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.biome.BiomeType;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.lang.ConditionalAsyncEffect;
import org.bukkit.block.Biome;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Set Biome")
@Description("Sets the biome in a region.")
@Examples("set region biome of {_region} to cherry grove")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffSetBiome extends ConditionalAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSetBiome.class)
                .supplier(EffSetBiome::new)
                .addPattern("[:lazily] set [the] region biome of %worldeditregions% to %biome%")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private Expression<Biome> biomeExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        biomeExpr = (Expression<Biome>) exprs[1];
        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (biomeExpr == null) return;
        BiomeType biomeType = BukkitAdapter.adapt(biomeExpr.getSingle(event));

        if (biomeType == null) {
            error("The biome is not a valid biome type.");
            return;
        }

        for (RegionWrapper region : regionExpr.getAll(event)) {
            region.setBiome(biomeType);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (!isAsync() ? "lazily " : "") + "set the region biome of " + regionExpr.toString(event, debug) + " to " + biomeExpr.toString(event, debug);
    }
}
