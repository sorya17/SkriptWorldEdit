package me.tecspace.skriptworldedit.elements.Schematic.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.transform.AffineTransform;
import me.tecspace.skriptworldedit.api.MaskWrapper;
import me.tecspace.skriptworldedit.api.utils.SchematicUtils;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Name("Schematic - Paste")
@Description("""
        Pastes a schematic at a location, with a bunch of configurable options.
        
        Entries:
        - paste entities: (optional) whether entities from the schematic should be included. false by default.
        - paste biomes: (optional) whether biomes from the schematic should be included. false by default.
        - ignore air: (optional) whether air blocks should be ignored. false by default.
        - mask: (optional) a mask of blocks to ignore from the schematic. none by default.
        - rotation: (optional) the rotation across the y-axis at which the schematic is placed. 0 by default.
        - scale: (optional) vector that lets you define how the schematic should be scaled. none by default.
        - offset: (optional) vector letting you offset the schematic placement. none by default.
        """)
@Examples("""
        paste schematic named "example" at {_loc}:
            paste entities: true
            paste biomes: false
            ignore air: true
            mask: {_mask}
            rotation: 90
            scale: vector(2,2,2)
            offset: vector(0,5,0)
        """)
@Since("1.0")
@RequiredPlugins("WorldEdit")
public class SecPasteSchematic extends EffectSection {

    private static EntryValidator VALIDATOR;

    public static void register(SyntaxRegistry registry) {
        VALIDATOR = buildValidator();
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(SecPasteSchematic.class)
                .supplier(SecPasteSchematic::new)
                .addPattern("paste [the] schematic (:named|with path) %string% at %locations%")
                .build());
    }

    private static EntryValidator buildValidator() {
        EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
        // common entries
        builder.addEntryData(new ExpressionEntryData<>("paste entities", new SimpleLiteral<>(false, true), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("paste biomes", new SimpleLiteral<>(false, true), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("ignore air", new SimpleLiteral<>(false, true), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("mask", null, true, Object.class));
        // transformation related entries
        builder.addEntryData(new ExpressionEntryData<>("rotation", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("scale", null, true, Vector.class));
        builder.addEntryData(new ExpressionEntryData<>("offset", null, true, Vector.class));
        return builder.build();
    }

    private boolean isPath;
    private Expression<String> sourceExpr;
    private Expression<Location> locationExpr;

    private @Nullable Expression<?> maskExpr;
    private @Nullable Expression<Boolean> pasteEntities;
    private @Nullable Expression<Boolean> pasteBiomes;
    private @Nullable Expression<Boolean> ignoreAir;
    // transformation related entries
    private @Nullable Expression<Double> rotationExpr;
    private @Nullable Expression<Vector> scaleExpr;
    private @Nullable Expression<Vector> offsetExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {

        // section entries
        if (hasSection()) {
            if (sectionNode == null || sectionNode.isEmpty()) return false;
            EntryContainer container = VALIDATOR.validate(sectionNode);
            if (container == null) return false;

            this.pasteEntities = (Expression<Boolean>) container.getOptional("paste entities", true);
            this.pasteBiomes = (Expression<Boolean>) container.getOptional("paste biomes", true);
            this.ignoreAir = (Expression<Boolean>) container.getOptional("ignore air", true);
            this.maskExpr = (Expression<?>) container.getOptional("mask", true);
            // transformation related entries
            this.rotationExpr = (Expression<Double>) container.getOptional("rotation", true);
            this.scaleExpr = (Expression<Vector>) container.getOptional("scale", true);
            this.offsetExpr = (Expression<Vector>) container.getOptional("offset", true);
        }

        this.isPath = !parseResult.hasTag("named");
        this.sourceExpr = (Expression<String>) expressions[0];
        this.locationExpr = (Expression<Location>) expressions[1];

        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        execute(event);
        return super.walk(event, false);
    }

    private void execute(Event event) {
        String source = sourceExpr.getSingle(event);
        if (source == null) {
            error("Schematic name or path is missing");
            return;
        }

        Path filePath = (isPath) ? Paths.get(source) : SchematicUtils.getSchematicsFolderPath().resolve(source + ".schem");

        // section common entries
        boolean ignoreAir = this.ignoreAir != null && Boolean.TRUE.equals(this.ignoreAir.getSingle(event));
        boolean pasteEntities = this.pasteEntities != null && Boolean.TRUE.equals(this.pasteEntities.getSingle(event));
        boolean pasteBiomes = this.pasteBiomes != null && Boolean.TRUE.equals(this.pasteBiomes.getSingle(event));

        MaskWrapper sourceMaskW = MaskWrapper.from(maskExpr == null ? null : maskExpr.getArray(event));
        Mask sourceMask = (sourceMaskW == null) ? null : sourceMaskW.mask();

        // section transformation related entries
        Double rotation = (rotationExpr == null) ? null : rotationExpr.getSingle(event);
        Vector scale = (scaleExpr == null) ? null : scaleExpr.getSingle(event);
        Vector offset = (offsetExpr == null) ? null : offsetExpr.getSingle(event);

        AffineTransform transform = TransformUtils.buildTransform(rotation, scale, offset);

        for (Location location : locationExpr.getAll(event)) {
            SchematicUtils.paste(
                    filePath,
                    location,
                    ignoreAir,
                    pasteEntities,
                    pasteBiomes,
                    sourceMask,
                    transform);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "paste schematic " + (isPath ? "with path  " : "named ") + sourceExpr.toString(event, debug) + " at " + locationExpr.toString(event, debug);
    }
}
