package me.tecspace.skriptworldedit.elements.Schematic.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.api.utils.SchematicUtils;
import me.tecspace.skriptworldedit.api.utils.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.File;
import java.nio.file.Paths;

@Name("Schematic - Delete")
@Description("Deletes a schematic.")
@Example("""
        delete schematic named "example"
        delete schematic with path "./mySchematics/example.schem"
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffDeleteSchematic extends AsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffDeleteSchematic.class)
                .supplier(EffDeleteSchematic::new)
                .addPattern("delete schematic (:named|with path) %strings%")
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
        File file = (isPath) ? SchematicUtils.getSchematicFile(Paths.get(source)) : SchematicUtils.getSchematicFile(source);
        if (file == null) return;
        if (!file.delete()) {
            Utils.log("Could not delete schematic file: " + source);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "delete schematic " + (isPath ? "with path  " : "named ") + sourceExpr.toString(event, debug);
    }
}
