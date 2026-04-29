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

@Name("Shape - Draw Spline")
@Description("""
        Makes a spline, which is a curved line going through two or more locations.
        
        tension: The tension of between the locations.
        bias: The bias of every node. (default 0)
        continuity: The continuity of every node. (default 0)
        quality: The quality of the spline. Must be greater than 0. (default 10)
        thickness: The thickness (radius) of the spline.
        """)
@Example("""
        draw spline of stone with tension 0.5 between {_locations::*}
        draw spline of dirt with tension 0.5 and thickness 3 between {_locations::*}
        draw spline using {_pattern} with tension 0.5, bias 0.1, continuity 0.1, quality 0.1 and thickness 3 between {_locations::*}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffDrawSpline extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffDrawSpline.class)
                .supplier(EffDrawSpline::new)
                .addPattern("[:async] draw [a] [:hollow] spline (of|with|using) [pattern] " + PatternWrapper.PARSABLE_TYPES_STRING + " with tension %number%" +
                        "[(,| and) [a] bias [of] %-number%]" +
                        "[(,| and) [a] continuity [of] %-number%]" +
                        "[(,| and) [a] quality [of] %-number%]" +
                        "[(,| and) [a] thickness [of] %-number%]" +
                        " between %locations%")
                .build());
    }

    private boolean async;
    private boolean hollow;
    private Expression<?> patternExpr;
    private Expression<Number> tensionExpr;
    private @Nullable Expression<Number> biasExpr;
    private @Nullable Expression<Number> continuityExpr;
    private @Nullable Expression<Number> qualityExpr;
    private @Nullable Expression<Number> thicknessExpr;
    private Expression<Location> locationsExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.async = parseResult.hasTag("async");
        this.hollow = parseResult.hasTag("hollow");
        this.patternExpr = expressions[0];
        this.tensionExpr = (Expression<Number>) expressions[1];
        this.biasExpr = (Expression<Number>) expressions[2];
        this.continuityExpr = (Expression<Number>) expressions[3];
        this.qualityExpr = (Expression<Number>) expressions[4];
        this.thicknessExpr = (Expression<Number>) expressions[5];
        this.locationsExpr = (Expression<Location>) expressions[6];
        return true;
    }

    @Override
    protected void execute(Event event) {
        PatternWrapper pattern = PatternWrapper.from(patternExpr.getArray(event));
        if (pattern == null) return;

        Location[] locations = locationsExpr.getArray(event);
        if (locations.length < 2) return;

        Number tension = tensionExpr.getSingle(event);
        if (tension == null) return;
        Number bias = biasExpr != null ? biasExpr.getSingle(event) : null;
        Number continuity = continuityExpr != null ? continuityExpr.getSingle(event) : null;
        Number quality = qualityExpr != null ? qualityExpr.getSingle(event) : null;
        Number thickness = thicknessExpr != null ? thicknessExpr.getSingle(event) : null;

        Shapes.drawSpline(pattern.pattern(), locations,
                tension.doubleValue(),
                bias != null ? bias.doubleValue() : 0,
                continuity != null ? continuity.doubleValue() : 0,
                quality != null ? quality.doubleValue() : 10,
                thickness != null ? thickness.doubleValue() : 0,
                !hollow, async);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "async " : "") + "draw " + (hollow ? "hollow " : "") + "spline using "
                + patternExpr.toString(event, debug)
                + " with tension " + tensionExpr.toString(event, debug)
                + (biasExpr != null ? ", bias " + biasExpr.toString(event, debug) : "")
                + (continuityExpr != null ? ", continuity " + continuityExpr.toString(event, debug) : "")
                + (qualityExpr != null ? ", quality " + qualityExpr.toString(event, debug) : "")
                + (thicknessExpr != null ? ", thickness " + thicknessExpr.toString(event, debug) : "")
                + " between " + locationsExpr.toString(event, debug);
    }
}