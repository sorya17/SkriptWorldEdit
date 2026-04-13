package me.tecspace.skriptworldedit.elements.Schematic;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.api.utils.SchematicUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.File;
import java.nio.file.Path;

@Name("Schematic - All saved schematics")
@Description("Returns the names of all schematics saved in the default schematics folder.")
@Example("send all saved schematics")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprAllSchematics extends SimpleExpression<String> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprAllSchematics.class, String.class)
                .supplier(ExprAllSchematics::new)
                .addPattern("[all] [[of] the] saved schematics")
                .build());
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    protected String @Nullable [] get(Event event) {
        Path folder = SchematicUtils.getSchematicsFolderPath();
        File dir = folder.toFile();

        if (!dir.exists() || !dir.isDirectory()) return new String[0];

        String[] files = dir.list((d, name) -> new File(d, name).isFile());
        return files != null ? files : new String[0];
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "all saved schematics";
    }
}
