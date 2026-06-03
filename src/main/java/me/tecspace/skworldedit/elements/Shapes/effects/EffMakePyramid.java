package me.tecspace.skworldedit.elements.Shapes.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.pattern.Pattern;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.lang.ConditionalAsyncEffect;
import me.tecspace.skworldedit.api.utils.PatternUtils;
import me.tecspace.skworldedit.api.Shapes;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Shape - Make Pyramid")
@Description("Makes a pyramid.")
@Examples("""
        make pyramid of sandstone with size 21 at {_location}
        make hollow pyramid using {_pattern} with size 17 at {_location}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffMakePyramid extends ConditionalAsyncEffect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffMakePyramid.class)
                .supplier(EffMakePyramid::new)
                .addPattern("[:lazily] make [a] [:hollow] pyramid (of|with|using) [pattern] " + PatternUtils.PARSABLE_TYPES_STRING + " with [a] size [of] %integer% at %locations%")
                .build());
    }

    private boolean hollow;
    private Expression<?> patternExpr;
    private Expression<Integer> sizeExpr;
    private Expression<Location> locationsExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
        this.hollow = parseResult.hasTag("hollow");
        this.patternExpr = expressions[0];
        this.sizeExpr = (Expression<Integer>) expressions[1];
        this.locationsExpr = (Expression<Location>) expressions[2];
        return true;
    }

    @Override
    protected void execute(Event event) {
        Pattern pattern = PatternUtils.parseFrom(patternExpr.getArray(event));
        if (pattern == null) return;

        Integer size = sizeExpr.getSingle(event);
        if (size == null) return;

        for (Location location : locationsExpr.getArray(event)) {
            Shapes.makePyramid(location, pattern, size, !hollow);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (!isAsync() ? "lazily " : "") + "make " + (hollow ? "hollow " : "") + "pyramid using " + patternExpr.toString(event, debug);
    }
}