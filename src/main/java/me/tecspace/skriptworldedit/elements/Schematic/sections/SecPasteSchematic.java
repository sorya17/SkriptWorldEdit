package me.tecspace.skriptworldedit.elements.Schematic.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.mask.Mask;
import me.tecspace.skriptworldedit.api.MaskWrapper;
import me.tecspace.skriptworldedit.api.utils.SchematicUtils;
import org.bukkit.Location;
import org.bukkit.event.Event;
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
        """)
@Examples("""
        paste schematic named "example" at {_loc}:
            paste entities: true
            paste biomes: false
            ignore air: true
            mask: {_mask}
        """)
@Since("1.0")
@RequiredPlugins("WorldEdit")
public class SecPasteSchematic extends Section {

    private static EntryValidator VALIDATOR;

    public static void register(SyntaxRegistry registry) {
        VALIDATOR = buildValidator();
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(SecPasteSchematic.class)
                .supplier(SecPasteSchematic::new)
                .addPattern("paste schematic (:named|with path) %string% at %locations%")
                .build());
    }

    private static EntryValidator buildValidator() {
        EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
        builder.addEntryData(new ExpressionEntryData<>("paste entities", new SimpleLiteral<>(false, false), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("paste biomes", new SimpleLiteral<>(false, false), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("ignore air", new SimpleLiteral<>(false, false), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("mask", null, true, Object.class));
        return builder.build();
    }

    private boolean isPath;
    private Expression<String> sourceExpr;
    private Expression<Location> locationExpr;

    private Expression<?> maskExpr;
    private Expression<Boolean> pasteEntities;
    private Expression<Boolean> pasteBiomes;
    private Expression<Boolean> ignoreAir;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {

        EntryContainer container = VALIDATOR.validate(sectionNode);
        if (container == null) return false;

        this.isPath = !parseResult.hasTag("named");
        this.sourceExpr = (Expression<String>) expressions[0];
        this.locationExpr = (Expression<Location>) expressions[1];

        this.pasteEntities = (Expression<Boolean>) container.get("paste entities", true);
        this.pasteBiomes = (Expression<Boolean>) container.get("paste biomes", true);
        this.ignoreAir = (Expression<Boolean>) container.get("ignore air", true);
        this.maskExpr = (Expression<?>) container.getOptional("mask", true);

        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        execute(event);
        return super.walk(event, false);
    }

    private void execute(Event event) {
        String source = sourceExpr.getSingle(event);
        if (source == null) return;

        Path filePath = (isPath) ? Paths.get(source) : SchematicUtils.getSchematicsFolderPath().resolve(source + ".schem");

        MaskWrapper sourceMaskW = MaskWrapper.from(maskExpr == null ? null : maskExpr.getArray(event));
        Mask sourceMask = (sourceMaskW == null) ? null : sourceMaskW.mask();

        boolean ignoreAir = Boolean.TRUE.equals(this.ignoreAir.getSingle(event));
        boolean pasteEntities = Boolean.TRUE.equals(this.pasteEntities.getSingle(event));
        boolean pasteBiomes = Boolean.TRUE.equals(this.pasteBiomes.getSingle(event));

        for (Location location : locationExpr.getAll(event)) {
            SchematicUtils.paste(filePath, location, ignoreAir, pasteEntities, pasteBiomes, sourceMask);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "paste schematic " + (isPath ? "with path  " : "named ") + sourceExpr.toString(event, debug) + " at " + locationExpr.toString(event, debug);
    }
}
