package me.tecspace.skriptworldedit.elements.Shapes;

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

@Name("Shape - Make Cone")
@Description("Makes a cone.")
@Example("""
        make hollow cone of dirt with size vector(5,5,5) and thickness 2 at {_location}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffMakeCone extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffMakeCone.class)
                .supplier(EffMakeCone::new)
                .addPattern("[:async] make [a] [:hollow] cone (of|with|using) [pattern] " + PatternWrapper.PARSABLE_TYPES_STRING + " with [a] size [of] %vector%"
                        + "[(,| and) [a] thickness [of] %-number%]"
                        + " at %locations%")
                .build());
    }

    private boolean async;
    private boolean hollow;
    private Expression<?> patternExpr;
    private Expression<Vector> sizeExpr;
    private @Nullable Expression<Number> thicknessExpr;
    private Expression<Location> locationsExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.async = parseResult.hasTag("async");
        this.hollow = parseResult.hasTag("hollow");
        this.patternExpr = expressions[0];
        this.sizeExpr = (Expression<Vector>) expressions[1];
        this.thicknessExpr = (Expression<Number>) expressions[2];
        this.locationsExpr = (Expression<Location>) expressions[3];
        return true;
    }

    @Override
    protected void execute(Event event) {
        PatternWrapper pattern = PatternWrapper.from(patternExpr.getArray(event));
        if (pattern == null) return;

        Vector size = sizeExpr.getSingle(event);
        if (size == null) return;

        double lenX = size.getX();
        double lenZ = size.getZ();
        int height = (int) size.getY();

        Number thicknessE = (thicknessExpr != null) ? thicknessExpr.getSingle(event) : null;
        double thickness = (thicknessE != null) ? thicknessE.doubleValue() : 1.0;

        for (Location location : locationsExpr.getArray(event)) {
            Shapes.makeCone(location, pattern.pattern(), lenX, lenZ, height, !hollow, thickness, async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "async " : "") + "make " + (hollow ? "hollow " : "") + "cone using " + patternExpr.toString(event, debug);
    }
}