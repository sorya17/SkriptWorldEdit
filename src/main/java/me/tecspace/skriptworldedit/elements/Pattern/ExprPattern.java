package me.tecspace.skriptworldedit.elements.Pattern;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.pattern.Pattern;
import me.tecspace.skriptworldedit.api.utils.PatternUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Pattern - Create")
@Description("""
        Creates a pattern that can be used in operations.
        https://worldedit.enginehub.org/en/latest/usage/general/patterns/
        """)
@Example("""
        set {_pattern} to pattern of dirt, stone and diorite
        set {_pattern} to pattern from "grass_block,dirt,stone"
        set {_pattern} to pattern from "70%%grass_block,20%%dirt,10%%stone"
        set {_pattern} to pattern from "#perlin[5][stone,andesite]"
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprPattern extends SimpleExpression<Pattern> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPattern.class, Pattern.class)
                .supplier(ExprPattern::new)
                .addPattern("[a] pattern [(of|from|that matches)] " + PatternUtils.PARSABLE_TYPES_STRING)
                .build());
    }

    private Expression<?> source;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        source = exprs[0];
        return true;
    }

    @Override
    protected @Nullable Pattern[] get(Event event) {
        Pattern pattern = PatternUtils.parseFrom(source.getArray(event));
        return new Pattern[]{pattern};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Pattern> getReturnType() {
        return Pattern.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "pattern of " + source.toString(event, debug);
    }
}
