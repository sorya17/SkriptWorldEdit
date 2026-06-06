package me.tecspace.skworldedit.elements.Region.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.lang.TestAsyncEffect;
import me.tecspace.skworldedit.api.utils.ExprUtils;
import org.bukkit.block.Biome;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Region - Regenerate")
@Description("""
        This will regenerate the region just like the //regen command. Be aware that this can take some time, since it's generating chunks.
        
        Optional Section Entries:
        - seed: (number) the seed to use. Uses the world's seed by default.
        - biome: (biome) which biome should be generated.
        - include biomes: (boolean) whether biomes should be applied. false by default.
        - mask: (mask) a mask to restrict which blocks in the region are affected.

        lazily: Forces synchronous execution (requires fawe).
        and wait: Pauses the skript until the operation finishes. No effect without fawe or alongside 'lazily'.
        """)
@Examples("""
        regenerate {_region}:
            seed: 5146159088207717555
            biome: savanna
            include biomes: true
            mask: {_mask}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class SecRegenerate extends TestAsyncEffect {

    private static EntryValidator VALIDATOR;

    public static void register(SyntaxRegistry registry) {
        VALIDATOR = buildValidator();
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(SecRegenerate.class)
                .supplier(SecRegenerate::new)
                .addPattern("[:lazily] regen[erate] [the] [region] %worldeditregions% [:and wait]")
                .build());
    }

    private static EntryValidator buildValidator() {
        EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
        builder.addEntryData(new ExpressionEntryData<>("seed", null, true, Long.class));
        builder.addEntryData(new ExpressionEntryData<>("biome", null, true, Biome.class));
        builder.addEntryData(new ExpressionEntryData<>("include biomes", null, true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("mask", null, true, Mask.class));
        return builder.build();
    }

    private Expression<RegionWrapper> regionsExpr;
    private @Nullable Expression<Long> seedExpr;
    private @Nullable Expression<Biome> biomeExpr;
    private @Nullable Expression<Boolean> includeBiomes;
    private @Nullable Expression<Mask> maskExpr;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean load(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {

        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
        setDelayed(parseResult.hasTag("and wait") && SkWorldEdit.UsesFastAsyncWorldEdit);

        this.regionsExpr = (Expression<RegionWrapper>) expressions[0];

        // section entries
        if (hasSection()) {
            if (sectionNode == null || sectionNode.isEmpty()) return false;
            EntryContainer container = VALIDATOR.validate(sectionNode);
            if (container == null) return false;

            this.seedExpr = (Expression<Long>) container.getOptional("seed", true);
            this.biomeExpr = (Expression<Biome>) container.getOptional("biome", true);
            this.includeBiomes = (Expression<Boolean>) container.getOptional("include biomes", true);
            this.maskExpr = (Expression<Mask>) container.getOptional("mask", true);
        }
        return true;
    }

    @Override
    protected @Nullable Runnable execute(Event event) {

        RegionWrapper[] regions = regionsExpr.getAll(event);
        if (regions == null || regions.length == 0) {
            error("No region to regenerate was given.");
            return null;
        }

        // section entries
        Long seed = ExprUtils.getSingle(event, seedExpr);
        Biome biome = ExprUtils.getSingle(event, biomeExpr);
        boolean includeBiomes = ExprUtils.getSingle(event, this.includeBiomes, false);
        Mask mask = ExprUtils.getSingle(event, maskExpr);

        BiomeType biomeType = (biome != null) ? BukkitAdapter.adapt(biome) : null;

        RegenOptions options = RegenOptions.builder()
                .biomeType(biomeType)
                .seed(seed)
                .regenBiomes(includeBiomes)
                .build();

        return () -> {
            // some regions can be in different worlds, thus it cant have the same session
            for (RegionWrapper region : regions) {
                World world = region.world();
                try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {

                    if (mask != null) session.setMask(mask);
                    // session.regenerate seems to be broken
                    //region.regenerate(options, mask); // this still needs testing
                    world.regenerate(region.region(), session, options);
                }
            }
        };
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (isAsync() ? "" : "lazily ") + "regenerate the region " + regionsExpr.toString(event, debug) + (isDelayed() ? " and wait" : "");
    }
}
