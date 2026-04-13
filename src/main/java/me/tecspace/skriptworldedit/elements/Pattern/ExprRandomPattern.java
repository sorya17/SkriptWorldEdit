package me.tecspace.skriptworldedit.elements.Pattern;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import me.tecspace.skriptworldedit.api.PatternWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Pattern - Random Pattern")
@Description("A empty random pattern, which other patterns can be added to with a custom weight.")
@Example("""
        set {_pattern} to a empty random pattern
        add dirt with a chance of 0.2 to {_pattern}
        add stone with a chance of 0.7 to {_pattern}
        add bedrock with a chance of 0.1 to {_pattern}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprRandomPattern extends SimpleExpression<PatternWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, DefaultSyntaxInfos.Expression.builder(ExprRandomPattern.class, PatternWrapper.class)
                .supplier(ExprRandomPattern::new)
                .addPattern("[a[n]] (empty|blank|new) random pattern")
                .build()
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    protected PatternWrapper[] get(Event event) {
        PatternWrapper region = new PatternWrapper(new RandomPattern());
        return new PatternWrapper[]{region};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends PatternWrapper> getReturnType() {
        return PatternWrapper.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "empty random pattern";
    }
}
