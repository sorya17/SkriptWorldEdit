package me.tecspace.skriptworldedit.elements.Schematic;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import me.tecspace.skriptworldedit.api.utils.SchematicUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

@Name("Region - Save as schematic or structure")
@Description("""
        Saves a region as a schematic or structure.
        You can either provide a full path or just a name, which will use the default schematics directory (usually './plugins/FastAsyncWorldEdit/schematics/').
        """)
@Example("""
        save {_region} as a schematic named "example"
        save {_region} as a schematic with path "./mySchematics/example.schem"
        save {_region} as a structure with path "./mySchematics/structure.nbt"
        save {_region} as a schematic named "example" including biomes
        save {_region} as a schematic named "example" including entities and biomes
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffSaveSchematic extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSaveSchematic.class)
                .supplier(EffSaveSchematic::new)
                .addPattern("save %worldeditregion% as [a] (:schematic|structure) (:named|with path) %string% [biomes:including entities] [entities:[and] [including] biomes]")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private boolean asSchematic;
    private boolean isPath;
    private Expression<String> sourceExpr;
    private boolean copyBiomes;
    private boolean copyEntities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.regionExpr = (Expression<RegionWrapper>) expressions[0];
        this.asSchematic = parseResult.hasTag("schematic");
        this.isPath = !parseResult.hasTag("named");
        this.sourceExpr = (Expression<String>) expressions[1];
        this.copyBiomes = parseResult.hasTag("biomes");
        this.copyEntities = parseResult.hasTag("entities");
        return true;
    }

    @Override
    protected void execute(Event event) {
        RegionWrapper region = this.regionExpr.getSingle(event);
        String source = this.sourceExpr.getSingle(event);
        if (region == null || source == null) return;

        Path savePath = (isPath) ? Paths.get(source) : SchematicUtils.getSchematicsFolderPath().resolve(
                source + ((asSchematic) ? ".schem" : ".nbt")
        );
        ClipboardFormat format = (asSchematic) ? BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC : BuiltInClipboardFormat.MINECRAFT_STRUCTURE;
        region.save(savePath, format, copyBiomes, copyEntities, null, null);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "save "
                + regionExpr.toString(event, debug)
                + ((asSchematic) ? "as a schematic " : "as a structure ")
                + (isPath ? "with path " : "named ")
                + sourceExpr.toString(event, debug);
    }
}