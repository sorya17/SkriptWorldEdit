package me.tecspace.skriptworldedit.elements.Schematic.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.api.clipboard.ClipboardManager;
import me.tecspace.skriptworldedit.api.utils.SchematicUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

@Name("Schematic - Unload")
@Description("Unloads a schematic that has been cached.")
@Example("""
        unload schematic named "example"
        unload schematic with path "./mySchematics/example.schem"
        unload all schematics
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffUnloadSchematic extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffUnloadSchematic.class)
                .supplier(EffUnloadSchematic::new)
                .addPattern("unload [the] schematic (:named|with path) %string%")
                .addPattern("unload all [of] [the] [loaded|cached] schematics")
                .build());
    }

    private boolean all;
    private boolean isPath;
    private Expression<String> sourceExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.all = matchedPattern == 1;
        if (!all) {
            this.isPath = !parseResult.hasTag("named");
            this.sourceExpr = (Expression<String>) expressions[0];
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (all) {
            String source = sourceExpr.getSingle(event);
            if (source == null) return;

            Path path = (isPath) ? Paths.get(source) : SchematicUtils.getSchematicsFolderPath().resolve(source + ".schem");
            ClipboardManager.removeClipboard(path);
        }
        ClipboardManager.removeAll();
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (!all) {
            return "unload schematic named " + sourceExpr.toString(event, debug);
        } else {
            return "unload all schematics";
        }
    }
}
