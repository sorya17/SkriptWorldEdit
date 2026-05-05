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

@Name("Shape - Make Cylinder")
@Description("Makes a cylinder.")
@Example("""
        make cylinder of dirt with size vector(7,7,7) at {_location}
        make hollow cylinder using {_pattern} with size vector(5,9,3) at {_location}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffMakeCylinder extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffMakeCylinder.class)
                .supplier(EffMakeCylinder::new)
                .addPattern("[:asnyc] make [a] [:hollow] cylinder (of|with|using) [pattern] " + PatternWrapper.PARSABLE_TYPES_STRING + " with [a] size [of] %vector% at %locations%")
                .build());
    }

    private boolean async;
    private boolean hollow;
    private Expression<?> patternExpr;
    private Expression<Vector> sizeExpr;
    private Expression<Location> locationsExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.async = parseResult.hasTag("async");
        this.hollow = parseResult.hasTag("hollow");
        this.patternExpr = expressions[0];
        this.sizeExpr = (Expression<Vector>) expressions[1];
        this.locationsExpr = (Expression<Location>) expressions[2];
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

        for (Location location : locationsExpr.getArray(event)) {
            Shapes.makeCylinder(location, pattern.pattern(), lenX, lenZ, height, !hollow, async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "" : "lazily ") + "make " + (hollow ? "hollow " : "") + "cylinder using " + patternExpr.toString(event, debug);
    }
}