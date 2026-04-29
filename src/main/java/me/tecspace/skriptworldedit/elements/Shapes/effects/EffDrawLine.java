package me.tecspace.skriptworldedit.elements.Shapes.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.api.PatternWrapper;
import me.tecspace.skriptworldedit.api.Shapes;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Shape - Draw Line")
@Description("Draw a line between between points.")
@Example("""
        draw line of stone between {_loc1} and {_loc2}
        draw line of stone with thickness 5 between {_loc1} and {_loc2}
        draw hollow line of emerald block with thickness 5 between {_loc1} and {_loc2}
        draw line using {_pattern} between {_locations::*}
        draw line using pattern "20%%dirt,80%%stone" between {_locations::*}
        draw line with white wool between all sheep
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffDrawLine extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffDrawLine.class)
                .supplier(EffDrawLine::new)
                .addPattern("[:async] draw [a] [:hollow] line (of|with|using) [pattern] " + PatternWrapper.PARSABLE_TYPES_STRING + " [with [a] thickness [of] %-number%] between %locations%")
                .build());
    }

    private boolean async;
    private boolean filled;
    private Expression<?> patternExpr;
    private @Nullable Expression<Number> radiusExpr;
    private Expression<Location> locationsExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.async = parseResult.hasTag("async");
        this.filled = !parseResult.hasTag("hollow");
        this.patternExpr = expressions[0];
        this.radiusExpr = (Expression<Number>) expressions[1];
        this.locationsExpr = (Expression<Location>) expressions[2];
        return true;
    }

    @Override
    protected void execute(Event event) {
        Number radius = (radiusExpr != null) ? radiusExpr.getSingle(event) : null;
        double rad = (radius != null) ? radius.doubleValue() : 0d;
        PatternWrapper pattern = PatternWrapper.from(patternExpr.getArray(event));
        if (pattern == null) return;
        Shapes.drawLine(pattern.pattern(), locationsExpr.getArray(event), rad, filled, async);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "async " : "") + "draw a " + (filled ? "hollow " : "") + "line of " + patternExpr.toString(event, debug) + " between " + locationsExpr.toString(event, debug);
    }
}
