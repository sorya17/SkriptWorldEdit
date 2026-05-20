package me.tecspace.skriptworldedit.elements.Pattern;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import me.tecspace.skriptworldedit.api.utils.PatternUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Pattern - Random Pattern")
@Description("""
        Add a pattern to a random pattern with a given chance.
        The chance does not have to add up to any number.
        The default chance for patterns in a random pattern is 1.
        """)
@Example("""
        set {_pattern} to a empty random pattern
        add dirt with a chance of 0.2 to {_pattern}
        add stone with a chance of 0.7 to {_pattern}
        add bedrock with a chance of 0.1 to {_pattern}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffAddRandomPattern extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffAddRandomPattern.class)
                .supplier(EffAddRandomPattern::new)
                .addPattern("add " + PatternUtils.PARSABLE_TYPES_STRING + " with [a] (chance|weight|probability) [of] %number% to %worldeditpatterns%")
                .build());
    }

    private Expression<?> patternExpr;
    private Expression<Number> chanceExpr;
    private Expression<Pattern> randomPatternExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        patternExpr = expressions[0];
        chanceExpr = (Expression<Number>) expressions[1];
        randomPatternExpr = (Expression<Pattern>) expressions[2];
        return true;
    }

    @Override
    protected void execute(Event event) {
        Pattern pattern = PatternUtils.parseFrom(patternExpr.getArray(event));
        if (pattern == null) return;

        Number chanceN = chanceExpr.getSingle(event);
        if (chanceN == null) return;
        double chance = chanceN.doubleValue();

        for (Pattern rp : randomPatternExpr.getArray(event)) {
            if (rp instanceof RandomPattern p) {
                p.add(pattern, chance);
            }
        }

    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "add pattern " + patternExpr.toString(event, debug) + " with chance of " + chanceExpr.toString(event, debug) + " to " + randomPatternExpr.toString(event, debug);
    }
}