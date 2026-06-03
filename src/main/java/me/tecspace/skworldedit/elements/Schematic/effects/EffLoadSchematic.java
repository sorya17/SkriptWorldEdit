package me.tecspace.skworldedit.elements.Schematic.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.tecspace.skworldedit.api.clipboard.ClipboardManager;
import me.tecspace.skworldedit.api.utils.SchematicUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Name("Schematic - Load")
@Description("""
        Loads a schematic and keeps it cached. When you paste the schematic later, it will not have to load it again.
        This can be useful if you paste a schematic frequently.
        """)
@Example("""
        load schematic named "example"
        load schematic with path "./mySchematics/example.schem"
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffLoadSchematic extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffLoadSchematic.class)
                .supplier(EffLoadSchematic::new)
                .addPattern("(load|cache) [the] schematic (:named|with path) %string%")
                .build());
    }

    private boolean isPath;
    private Expression<String> sourceExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.isPath = !parseResult.hasTag("named");
        this.sourceExpr = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String source = sourceExpr.getSingle(event);
        if (source == null) return;

        Path path = (isPath) ? Paths.get(source) : SchematicUtils.getSchematicsFolderPath().resolve(source + ".schem");

        try {
            ClipboardManager.getClipboard(path, true);
        } catch (IOException e) {
            error("Schematic could not be loaded. Check console for more information.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "load the schematic " + (isPath ? "with path " : "named ") + sourceExpr.toString(event, debug);
    }
}