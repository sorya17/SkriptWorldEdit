package me.tecspace.skworldedit.elements.Region.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.lang.AsyncEffectSection;
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
        
        Entries:
        - seed: (optional) the seed to use. Uses the world's seed by default.
        - biome: (optional) which biome should be generated.
        - include biomes: (optional) whether biomes should be applied. false by default.
        - mask: (optional) a mask to restrict which blocks in the region are affected.
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
public class EffSecRegenerate extends AsyncEffectSection {

    private static EntryValidator VALIDATOR;

    public static void register(SyntaxRegistry registry) {
        VALIDATOR = buildValidator();
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(EffSecRegenerate.class)
                .supplier(EffSecRegenerate::new)
                .addPattern("regenerate [the] [region] %worldeditregions%")
                .build());
    }

    private static EntryValidator buildValidator() {
        EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
        builder.addEntryData(new ExpressionEntryData<>("seed", null, true, Long.class));
        builder.addEntryData(new ExpressionEntryData<>("biome", null, true, Biome.class));
        builder.addEntryData(new ExpressionEntryData<>("include biomes", new SimpleLiteral<>(false, true), true, Boolean.class));
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
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {

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

        setAsync(SkWorldEdit.UsesFastAsyncWorldEdit);
        this.regionsExpr = (Expression<RegionWrapper>) expressions[0];

        return true;
    }

    @Override
    protected void execute(Event event) {
        if (regionsExpr == null) return;

        // section entries
        Long seed = (seedExpr != null) ? seedExpr.getSingle(event) : null;
        Biome biome = (biomeExpr != null) ? biomeExpr.getSingle(event) : null;
        boolean includeBiomes = this.includeBiomes != null && Boolean.TRUE.equals(this.includeBiomes.getSingle(event));
        Mask mask = (maskExpr != null) ? maskExpr.getSingle(event) : null;

        BiomeType biomeType = (biome != null) ? BukkitAdapter.adapt(biome) : null;

        RegenOptions options = RegenOptions.builder()
                .biomeType(biomeType)
                .seed(seed)
                .regenBiomes(includeBiomes)
                .build();

        for (RegionWrapper region : regionsExpr.getAll(event)) {
            region.regenerate(options, mask);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "regenerate " + regionsExpr.toString(event, debug);
    }
}
