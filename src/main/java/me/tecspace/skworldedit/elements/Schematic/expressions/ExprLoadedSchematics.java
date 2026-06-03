package me.tecspace.skworldedit.elements.Schematic.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.tecspace.skworldedit.api.clipboard.ClipboardManager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Schematic - All loaded schematics")
@Description("Returns all cached schematics.")
@Example("send all loaded schematics")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprLoadedSchematics extends SimpleExpression<String> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprLoadedSchematics.class, String.class)
                .supplier(ExprLoadedSchematics::new)
                .addPattern("[all] [of] [the] (loaded|cached) schematics")
                .build());
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    protected String @Nullable [] get(Event event) {
        return ClipboardManager.getCachedClipboards();
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
        return "all of the loaded schematics";
    }
}
