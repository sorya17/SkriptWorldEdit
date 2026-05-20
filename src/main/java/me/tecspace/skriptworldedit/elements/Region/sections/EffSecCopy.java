package me.tecspace.skriptworldedit.elements.Region.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.transform.AffineTransform;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import me.tecspace.skriptworldedit.api.lang.AsyncEffectSection;
import me.tecspace.skriptworldedit.api.utils.TransformUtils;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Region - Copy")
@Description("""
        Copies the contents of a region to another location directly.
        
        Entries:
        - copy entities: (optional) whether entities should be included. false by default.
        - copy biomes: (optional) whether biomes should be included. false by default.
        - mask: (optional) a mask of blocks to only change when placing.
        - source mask: (optional) a mask of blocks to only copy from the region.
        - rotation: (optional) the rotation across the y-axis at which the build is copied. 0 by default.
        - scale: (optional) vector that lets you define how the build should be scaled. none by default.
        - offset: (optional) vector letting you offset the build placement. none by default.
        """)
@Examples("""
        copy {_region} to location(187,67,420):
            copy entities: true
            copy biomes: false
            mask: {_mask}
            source mask: (inverse mask of diorite)
            rotation: 90
            scale: vector(2,2,2)
            offset: vector(0,5,0)
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffSecCopy extends AsyncEffectSection {

    private static EntryValidator VALIDATOR;

    public static void register(SyntaxRegistry registry) {
        VALIDATOR = buildValidator();
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(EffSecCopy.class)
                .supplier(EffSecCopy::new)
                .addPattern("[:lazily] copy [the] region %worldeditregions% to %locations%")
                .build());
    }

    private static EntryValidator buildValidator() {
        EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
        // common entries
        builder.addEntryData(new ExpressionEntryData<>("copy entities", new SimpleLiteral<>(false, true), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("copy biomes", new SimpleLiteral<>(false, true), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("mask", null, true, Mask.class));
        builder.addEntryData(new ExpressionEntryData<>("source mask", null, true, Mask.class));
        // transformation related entries
        builder.addEntryData(new ExpressionEntryData<>("rotation", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("scale", null, true, Vector.class));
        builder.addEntryData(new ExpressionEntryData<>("offset", null, true, Vector.class));
        return builder.build();
    }

    private Expression<RegionWrapper> regionsExpr;
    private Expression<Location> locationsExpr;

    private @Nullable Expression<Mask> maskExpr, sourceMaskExpr;
    private @Nullable Expression<Boolean> copyEntities, copyBiomes;
    // transformation related entries
    private @Nullable Expression<Double> rotationExpr;
    private @Nullable Expression<Vector> scaleExpr, offsetExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {

        // section entries
        if (hasSection()) {
            if (sectionNode == null || sectionNode.isEmpty()) return false;
            EntryContainer container = VALIDATOR.validate(sectionNode);
            if (container == null) return false;

            this.copyEntities = (Expression<Boolean>) container.getOptional("copy entities", true);
            this.copyBiomes = (Expression<Boolean>) container.getOptional("copy biomes", true);
            this.maskExpr = (Expression<Mask>) container.getOptional("mask", true);
            this.sourceMaskExpr = (Expression<Mask>) container.getOptional("source mask", true);
            // transformation related entries
            this.rotationExpr = (Expression<Double>) container.getOptional("rotation", true);
            this.scaleExpr = (Expression<Vector>) container.getOptional("scale", true);
            this.offsetExpr = (Expression<Vector>) container.getOptional("offset", true);
        }

        setAsync(!parseResult.hasTag("lazily") && SkriptWorldEdit.UsesFastAsyncWorldEdit);
        this.regionsExpr = (Expression<RegionWrapper>) expressions[0];
        this.locationsExpr = (Expression<Location>) expressions[1];

        return true;
    }

    public void execute(Event event) {

        // section common entries
        boolean copyEntities = this.copyEntities != null && Boolean.TRUE.equals(this.copyEntities.getSingle(event));
        boolean copyBiomes = this.copyBiomes != null && Boolean.TRUE.equals(this.copyBiomes.getSingle(event));

        Mask mask = (maskExpr != null) ? maskExpr.getSingle(event) : null;
        Mask sourceMask = (sourceMaskExpr != null) ? sourceMaskExpr.getSingle(event) : null;

        // section transformation related entries
        Double rotation = (rotationExpr == null) ? null : rotationExpr.getSingle(event);
        Vector scale = (scaleExpr == null) ? null : scaleExpr.getSingle(event);
        Vector offset = (offsetExpr == null) ? null : offsetExpr.getSingle(event);

        AffineTransform transform = TransformUtils.buildTransform(rotation, scale, offset);

        if (locationsExpr == null || regionsExpr == null) return;

        for (RegionWrapper region : regionsExpr.getArray(event)) {
            for (Location location : locationsExpr.getArray(event)) {
                region.copy(
                        location,
                        null,
                        copyBiomes,
                        copyEntities,
                        mask,
                        sourceMask,
                        transform
                );
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (!isAsync() ? "lazily " : "") + "copy region " + regionsExpr.toString(event, debug) + " to " + locationsExpr.toString(event, debug);
    }
}
