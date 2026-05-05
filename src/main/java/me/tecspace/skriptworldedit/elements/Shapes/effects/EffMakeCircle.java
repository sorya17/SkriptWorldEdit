package me.tecspace.skriptworldedit.elements.Shapes.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.math.Vector3;
import me.tecspace.skriptworldedit.api.PatternWrapper;
import me.tecspace.skriptworldedit.api.Shapes;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Shape - Make Circle")
@Description("""
        Makes a circle (disc) at the location.
        Normal: the orientation/tilt of the circle. defaults to (0,1,0).
        """)
@Example("""
        make circle of diorite with radius 3 at {_location}
        make hollow circle of andesite with radius 2 at {_location}
        make circle of granite with size vector(5,5,5) and normal vector(1,0,0) at {_location}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffMakeCircle extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffMakeCircle.class)
                .supplier(EffMakeCircle::new)
                .addPattern("[:lazily] make [a] [:hollow] circle (of|with|using) [pattern] " + PatternWrapper.PARSABLE_TYPES_STRING + " with [a] (size|radi(i|us)) [of] %number/vector%"
                        + "[(,| and) [a] (normal|tilt|orientation) [of] %-vector%]"
                        + " at %locations%")
                .build());
    }

    private boolean async;
    private boolean hollow;
    private Expression<?> patternExpr;
    private Expression<?> radiusExpr;
    private @Nullable Expression<Vector> normalExpr;
    private Expression<Location> locationsExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.async = !parseResult.hasTag("lazily");
        this.hollow = parseResult.hasTag("hollow");
        this.patternExpr = expressions[0];
        this.radiusExpr = expressions[1];
        this.normalExpr = (Expression<Vector>) expressions[2];
        this.locationsExpr = (Expression<Location>) expressions[3];
        return true;
    }

    @Override
    protected void execute(Event event) {
        PatternWrapper pattern = PatternWrapper.from(patternExpr.getArray(event));
        if (pattern == null) return;

        // get radius
        double radiusX, radiusY, radiusZ;
        switch (radiusExpr.getSingle(event)) {
            case Number number -> radiusX = radiusY = radiusZ = number.doubleValue();
            case Vector vector -> {
                radiusX = vector.getX();
                radiusY = vector.getY();
                radiusZ = vector.getZ();
            }
            case null, default -> {
                return;
            }
        }

        // get normal of circle, default to flat orientation
        Vector normalVec = (normalExpr != null) ? normalExpr.getSingle(event) : null;
        Vector3 normal = (normalVec != null) ? Vector3.at(normalVec.getX(), normalVec.getY(), normalVec.getZ()) : Vector3.UNIT_Y;

        for (Location location : locationsExpr.getArray(event)) {
            Shapes.makeCircle(location, pattern.pattern(), radiusX, radiusY, radiusZ, !hollow, normal, async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "" : "lazily ") + "make " + (hollow ? "hollow " : "") + "spline using " + patternExpr.toString(event, debug) + " at " + locationsExpr.toString(event, debug) + " with radius " + radiusExpr.toString(event, debug);
    }
}
