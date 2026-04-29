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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Shape - Make Sphere")
@Description("Makes a sphere.")
@Examples("""
        make sphere of stone with radius 5 at {_location}
        make sphere using {_pattern} with radius 5 at {_location}
        make hollow sphere of dirt with size vector(5,10,5) at {_location}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffMakeSphere extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffMakeSphere.class)
                .supplier(EffMakeSphere::new)
                .addPattern("[:lazily] make [a] [:hollow] sphere (of|with|using) [pattern] " + PatternWrapper.PARSABLE_TYPES_STRING + " with [a] (radius|size) [of] %number/vector% at %locations%")
                .build());
    }

    private boolean async;
    private boolean hollow;
    private Expression<?> patternExpr;
    private Expression<?> radiusExpr;
    private Expression<Location> locationsExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.async = parseResult.hasTag("async");
        this.hollow = parseResult.hasTag("hollow");
        this.patternExpr = expressions[0];
        this.radiusExpr = expressions[1];
        this.locationsExpr = (Expression<Location>) expressions[2];
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

        for (Location location : locationsExpr.getArray(event)) {
            Shapes.makeSphere(location, pattern.pattern(), radiusX, radiusY, radiusZ, !hollow, async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "async " : "") + "make " + (hollow ? "hollow " : "") + "sphere using " + patternExpr.toString(event, debug);
    }
}