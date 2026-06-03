package me.tecspace.skworldedit.elements.Shapes.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.Vector3;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.lang.ConditionalAsyncEffect;
import me.tecspace.skworldedit.api.utils.PatternUtils;
import me.tecspace.skworldedit.api.Shapes;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Shape - Make Blob")
@Description("""
        Makes a distorted sphere, which, for example, could be used to create rocks.
        
        size: overall size of the blob
        frequency: distortion amount (0 to 1)
        amplitude: distortion amplitude (0 to 1)
        radius: radii to multiply x/y/z by
        sphericity: how spherical to make the blob. 1 = very spherical, 0 = not
        """)
@Examples("""
        make blob of stone with size 5, frequency 0.1, amplitude 0, radius vector(1,1,1) and sphericity 0.4 at {_loc}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffMakeBlob extends ConditionalAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffMakeBlob.class)
                .supplier(EffMakeBlob::new)
                .addPattern("[:lazily] make [a] blob (of|with|using) [pattern] " + PatternUtils.PARSABLE_TYPES_STRING + " with [a] size [of] %number%" +
                        "[(,| and) [a] frequency [of] %-number%]" +
                        "[(,| and) [a] amplitude [of] %-number%]" +
                        "[(,| and) [a] radius [of] %-vector%]" +
                        "[(,| and) [a] sphericity [of] %-number%]" +
                        " at %locations%")
                .build());
    }

    private Expression<?> patternExpr;
    private Expression<Number> sizeExpr;
    private @Nullable Expression<Number> frequencyExpr;
    private @Nullable Expression<Number> amplitudeExpr;
    private @Nullable Expression<Vector> radiusExpr;
    private @Nullable Expression<Number> sphericityExpr;
    private Expression<Location> locationsExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
        this.patternExpr = exprs[0];
        this.sizeExpr = (Expression<Number>) exprs[1];
        this.frequencyExpr = (Expression<Number>) exprs[2];
        this.amplitudeExpr = (Expression<Number>) exprs[3];
        this.radiusExpr = (Expression<Vector>) exprs[4];
        this.sphericityExpr = (Expression<Number>) exprs[5];
        this.locationsExpr = (Expression<Location>) exprs[6];
        return true;
    }

    @Override
    protected void execute(Event event) {
        Pattern pattern = PatternUtils.parseFrom(patternExpr.getArray(event));
        Location[] locations = locationsExpr.getArray(event);

        if (pattern == null || locations == null || locations.length == 0) return;

        Double size = getDouble(sizeExpr, event);
        Double freq = getDouble(frequencyExpr, event);
        Double amp = getDouble(amplitudeExpr, event);
        Double sph = getDouble(sphericityExpr, event);

        Vector rawRad = (radiusExpr != null) ? radiusExpr.getSingle(event) : null;

        if (size == null || freq == null || amp == null || sph == null || rawRad == null) return;

        Vector3 radiusD = Vector3.at(rawRad.getX(), rawRad.getY(), rawRad.getZ());

        for (Location loc : locations) {
            Shapes.makeBlob(loc, pattern, size, freq, amp, radiusD, sph);
        }
    }

    private Double getDouble(Expression<? extends Number> expr, Event e) {
        if (expr == null) return null;
        Number n = expr.getSingle(e);
        return (n != null) ? n.doubleValue() : null;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (!isAsync() ? "lazily " : "") + "make a blob using " + patternExpr.toString(event, debug) + " with size " + sizeExpr.toString(event, debug) +
                ", frequency " + frequencyExpr.toString(event, debug) +
                ", amplitude " + amplitudeExpr.toString(event, debug) +
                ", radius " + radiusExpr.toString(event, debug) +
                " and sphericity " + sphericityExpr.toString(event, debug) +
                " at " + locationsExpr.toString(event, debug);
    }
}