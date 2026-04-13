package me.tecspace.skriptworldedit.elements.Schematic;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.api.utils.SchematicUtils;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.File;
import java.nio.file.Paths;

@Name("Schematic - Paste")
@Description("Pastes a schematic at a location.")
@Example("""
        paste schematic named "example" at {_location}
        paste schematic with path "./mySchematics/example.schem" at {_location}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffPasteSchematic extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffPasteSchematic.class)
                .supplier(EffPasteSchematic::new)
                .addPattern("paste schematic (:named|with path) %string% at %locations%")
                .build());
    }

    private boolean isPath;
    private Expression<String> sourceExpr;
    private Expression<Location> locationExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.isPath = !parseResult.hasTag("named");
        this.sourceExpr = (Expression<String>) expressions[0];
        this.locationExpr = (Expression<Location>) expressions[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String source = sourceExpr.getSingle(event);
        if (source == null) return;
        File file = (isPath) ? SchematicUtils.getSchematicFile(Paths.get(source)) : SchematicUtils.getSchematicFile(source);
        if (file == null) return;
        for (Location location : locationExpr.getAll(event)) {
            SchematicUtils.paste(file.toPath(), location, true, true, true);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "paste schematic " + (isPath ? "with path  " : "named ") + sourceExpr.toString(event, debug) + " at " + locationExpr.toString(event, debug);
    }
}