package me.tecspace.skriptworldedit.elements.Schematic.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.Mask;
import me.tecspace.skriptworldedit.api.MaskWrapper;
import me.tecspace.skriptworldedit.api.RegionWrapper;
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

@Name("Region - Save as schematic or structure")
@Description("""
        Saves a region as a schematic or structure, with a bunch of configurable options.
        You can either provide a full path or just a name, which will use the default schematics directory (usually './plugins/FastAsyncWorldEdit/schematics/').
        
        Entries:
        - include entities: (optional) whether entities should be included. true by default.
        - include biomes: (optional) whether biomes should be included. true by default.
        - overwrite existing: (optional) whether it should override the schematic if it already exists with that name/path. true by default.
        - origin: (optional) the location of which the schematic will be pasted around. region's lesser most corner by default.
        - mask: (optional) a mask of blocks to NOT include in the schematic. none by default.
        """)
@Example("""
        save {_region} as a schematic named "example"
        save {_region} as a schematic with path "./mySchematics/example.schem"
        save {_region} as a structure with path "./mySchematics/structure.nbt"
        
        save {_region} as a schematic named "example":
            include entities: true
            include biomes: true
            overwrite existing: false
            origin: location(1,2,3)
            mask: dirt, stone, air
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class SecSaveSchematic extends EffectSection {

    private static EntryValidator VALIDATOR;

    public static void register(SyntaxRegistry registry) {
        VALIDATOR = buildValidator();
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(SecSaveSchematic.class)
                .supplier(SecSaveSchematic::new)
                .addPattern("save %worldeditregions% as [a] (:schematic|structure) (:named|with path) %string%")
                .build());
    }

    private static EntryValidator buildValidator() {
        EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
        // common entries
        builder.addEntryData(new ExpressionEntryData<>("include entities", new SimpleLiteral<>(true, true), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("include biomes", new SimpleLiteral<>(true, true), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("overwrite existing", new SimpleLiteral<>(true, true), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("origin", null, true, Location.class));
        builder.addEntryData(new ExpressionEntryData<>("mask", null, true, Object.class));
        return builder.build();
    }

    private Expression<RegionWrapper> regionsExpr;
    private boolean asSchematic;
    private boolean isPath;
    private Expression<String> sourceExpr;

    private @Nullable Expression<Boolean> includeEntities;
    private @Nullable Expression<Boolean> includeBiomes;
    private @Nullable Expression<Boolean> overwriteExisting;
    private @Nullable Expression<Location> originExpr;
    private @Nullable Expression<?> maskExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {

        // section entries
        if (hasSection()) {
            if (sectionNode == null || sectionNode.isEmpty()) return false;
            EntryContainer container = VALIDATOR.validate(sectionNode);
            if (container == null) return false;

            this.includeEntities = (Expression<Boolean>) container.getOptional("include entities", true);
            this.includeBiomes = (Expression<Boolean>) container.getOptional("include biomes", true);
            this.overwriteExisting = (Expression<Boolean>) container.getOptional("overwrite existing", true);
            this.originExpr = (Expression<Location>) container.getOptional("origin", true);
            this.maskExpr = (Expression<?>) container.getOptional("mask", true);
        }

        this.regionsExpr = (Expression<RegionWrapper>) expressions[0];
        this.asSchematic = parseResult.hasTag("schematic");
        this.isPath = !parseResult.hasTag("named");
        this.sourceExpr = (Expression<String>) expressions[1];

        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        execute(event);
        return super.walk(event, false);
    }

    private void execute(Event event) {
        // effect entries
        String source = sourceExpr.getSingle(event);
        if (source == null) {
            error("Schematic name or path is missing");
            return;
        }

        Path savePath = (isPath) ? Paths.get(source) : SchematicUtils.getSchematicsFolderPath().resolve(
                source + ((asSchematic) ? ".schem" : ".nbt")
        );

        // section common entries
        MaskWrapper sourceMaskW = MaskWrapper.from(maskExpr == null ? null : maskExpr.getArray(event));
        Mask sourceMask = (sourceMaskW == null) ? null : sourceMaskW.mask();

        boolean includeEntities = this.includeEntities == null || Boolean.TRUE.equals(this.includeEntities.getSingle(event));
        boolean includeBiomes = this.includeBiomes == null || Boolean.TRUE.equals(this.includeBiomes.getSingle(event));
        boolean overwriteExisting = this.overwriteExisting == null || Boolean.TRUE.equals(this.overwriteExisting.getSingle(event));

        if (overwriteExisting && savePath.toFile().exists()) return;

        Location origin = (originExpr == null) ? null : originExpr.getSingle(event);

        // get format
        ClipboardFormat format = (asSchematic) ? BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC : BuiltInClipboardFormat.MINECRAFT_STRUCTURE;

        for (RegionWrapper region : regionsExpr.getAll(event)) {
            region.save(
                    savePath,
                    format,
                    includeEntities,
                    includeBiomes,
                    origin,
                    sourceMask,
                    null
            );
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "save " + regionsExpr.toString(event, debug)
                + ((asSchematic) ? "as a schematic " : "as a structure ")
                + (isPath ? "with path " : "named ")
                + sourceExpr.toString(event, debug);
    }
}