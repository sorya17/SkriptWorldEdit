package me.tecspace.skriptworldedit.elements.Schematic;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

@Name("Schematic - Save")
@Description("""
        Saves a region as a schematic.
        You can either provide the full path or just the name (which will use the default schematics directory).
        To save it as a different format like a Minecraft structure, you can use any of the format's extensions in the path/name.
        """)
@Example("""
        save {_region} as a schematic named "example"
        save {_region} as a schematic with path "./mySchematics/example.schem"
        save {_region} as a schematic with path "./mySchematics/structure.nbt"
        save {_region} as a schematic named "example" including entities
        save {_region} as a schematic named "example" including biomes and entities
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffSaveSchematic extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSaveSchematic.class)
                .supplier(EffSaveSchematic::new)
                .addPattern("save %worldeditregion% as [a] schematic (:named|with path) %string% [biomes:including biomes] [entities:[and] [including] entities]")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private boolean isPath;
    private Expression<String> sourceExpr;
    private boolean copyBiomes;
    private boolean copyEntities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.regionExpr = (Expression<RegionWrapper>) expressions[0];
        this.isPath = !parseResult.hasTag("named");
        this.sourceExpr = (Expression<String>) expressions[1];
        this.copyBiomes = parseResult.hasTag("biomes");
        this.copyEntities = parseResult.hasTag("entities");
        return false;
    }

    @Override
    protected void execute(Event event) {
        RegionWrapper region = this.regionExpr.getSingle(event);
        String source = this.sourceExpr.getSingle(event);
        if (region == null || source == null) return;

        // get save path by direct path or name using worldedit schematics directory
        Path savePath = (isPath) ? Paths.get(source) : WorldEdit.getInstance().getSchematicsFolderPath().resolve(source);

        // find clipboard format by path or file name, default to SPONGE_V3_SCHEMATIC
        ClipboardFormat format = ClipboardFormats.findByFile(savePath.toFile());
        if (format == null) format = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC;

        region.saveAsSchematic(savePath, format, copyBiomes, copyEntities);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "save "
                + regionExpr.toString(event, debug)
                + " as schematic "
                + (isPath ? "with path " : "named ")
                + sourceExpr.toString(event, debug);
    }
}