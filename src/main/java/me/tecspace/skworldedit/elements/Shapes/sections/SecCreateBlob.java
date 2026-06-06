package me.tecspace.skworldedit.elements.Shapes.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.lang.TestAsyncEffect;
import me.tecspace.skworldedit.api.utils.ExprUtils;
import me.tecspace.skworldedit.api.utils.PatternUtils;
import me.tecspace.skworldedit.api.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Shapes - Blob")
@Description("""
        Makes a distorted sphere.
        
        Optional Section Entries:
        - size: (number) overall size of the blob
        - frequency: (number) distortion amount (0 to 1)
        - amplitude: (number) distortion amplitude (0 to 1)
        - radius: (vector) radii to multiply x/y/z by
        - sphericity: (number) how spherical to make the blob. 1 = very spherical, 0 = not
        - mask: (mask) a mask to respect
        
        lazily: Forces synchronous execution (requires fawe).
        and wait: Pauses the skript until the operation finishes. No effect without fawe or alongside 'lazily'.
        """)
@Example("create blob of stone at {_loc}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class SecCreateBlob extends TestAsyncEffect {

    private static EntryValidator VALIDATOR;

    public static void register(SyntaxRegistry registry) {
        VALIDATOR = buildValidator();
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(SecCreateBlob.class)
                .supplier(SecCreateBlob::new)
                .addPattern("[:lazily] create [a] blob [out] of " + PatternUtils.PARSABLE_TYPES_STRING + " at %locations% [:and wait]")
                .build());
    }

    private static EntryValidator buildValidator() {
        EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
        // common entries
        builder.addEntryData(new ExpressionEntryData<>("size", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("frequency", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("amplitude", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("radius", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("sphericity", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("mask", null, true, Mask.class));
        return builder.build();
    }

    private Expression<?> patternExpr;
    private Expression<Location> locationsExpr;
    // section entries
    private @Nullable Expression<Double> sizeExpr, frequencyExpr, amplitudeExpr;
    private @Nullable Expression<Vector> radiusExpr;
    private @Nullable Expression<Double> sphericityExpr;
    private @Nullable Expression<Mask> maskExpr;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean load(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {

        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
        setDelayed(parseResult.hasTag("and wait") && SkWorldEdit.UsesFastAsyncWorldEdit);

        this.patternExpr = expressions[0];
        this.locationsExpr = (Expression<Location>) expressions[1];

        // section entries
        if (hasSection()) {
            if (sectionNode.isEmpty()) return false;
            EntryContainer container = VALIDATOR.validate(sectionNode);
            if (container == null) return false;

            // common entries
            this.sizeExpr = (Expression<Double>) container.getOptional("size", true);
            this.frequencyExpr = (Expression<Double>) container.getOptional("frequency", true);
            this.amplitudeExpr = (Expression<Double>) container.getOptional("amplitude", true);
            this.radiusExpr = (Expression<Vector>) container.getOptional("radius", true);
            this.sphericityExpr = (Expression<Double>) container.getOptional("sphericity", true);
            this.maskExpr = (Expression<Mask>) container.getOptional("mask", true);
        }
        return true;
    }

    @Override
    protected @Nullable Runnable execute(Event event) {

        Location[] locations = locationsExpr.getArray(event);
        if (locations == null || locations.length == 0) {
            error("No Location to generate the blob at was given.");
            return null;
        }

        Pattern pattern = PatternUtils.parseFrom(patternExpr.getArray(event));
        if (pattern == null) {
            error("Could not parse pattern '"+ patternExpr.toString() + "'. Make sure it's a valid block type or pattern.");
            return null;
        }

        // section entries
        double size = ExprUtils.getSingle(event, this.sizeExpr, 5);
        double frequency = ExprUtils.getSingle(event, this.frequencyExpr, 0.5);
        double amplitude = ExprUtils.getSingle(event, this.amplitudeExpr, 0.5);
        Vector radius = ExprUtils.getSingle(event, this.radiusExpr, new Vector(1,1,1));
        double sphericity = ExprUtils.getSingle(event, this.sphericityExpr, 0.5);
        Mask mask = ExprUtils.getSingle(event, this.maskExpr);

        return () -> {
            for (Location loc : locations) {
                World world = loc.getWorld();
                if (world == null) continue;
                try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                    session.setMask(mask);
                    session.makeBlob(Utils.toBlockVector3(loc), pattern, size, frequency, amplitude, Utils.toVector3(radius), sphericity);
                }
            }
        };
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (isAsync() ? "" : "lazily ") + "create blob of " + patternExpr.toString(event, debug) + " at " + locationsExpr.toString(event, debug) + (isDelayed() ? " and wait" : "");
    }
}
