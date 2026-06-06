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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Shapes - Line/Spline")
@Description("""
        Creates a line (out of blocks) between two or more locations, in the order given.
        
        Optional Section Entries:
        - thickness: (number) The radius (thickness) of the line.
        - hollow: (boolean) If true, only a shell will be generated.
        - mask: (mask) A mask to respect (:
        - tension: (number) The tension of every node. default 0.
        - bias: (number) The bias of every node. default 0.
        - continuity: (number) The continuity of every node. default 0.
        - quality: (number) The quality of the spline. Must be greater than 0. default 10.
        
        lazily: Forces synchronous execution (requires fawe).
        and wait: Pauses the skript until the operation finishes. No effect without fawe or alongside 'lazily'.
        """)
@Example("create line of stone between {_loc1} and {_loc2}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class SecCreateLine extends TestAsyncEffect {

    private static EntryValidator VALIDATOR;

    public static void register(SyntaxRegistry registry) {
        VALIDATOR = buildValidator();
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(SecCreateLine.class)
                .supplier(SecCreateLine::new)
                .addPattern("[:lazily] create [a] (line|spline) [out] of " + PatternUtils.PARSABLE_TYPES_STRING + " between %locations% [:and wait]")
                .build());
    }

    private static EntryValidator buildValidator() {
        EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
        // common entries
        builder.addEntryData(new ExpressionEntryData<>("thickness", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("hollow", null, true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("mask", null, true, Mask.class));
        // spline entries
        builder.addEntryData(new ExpressionEntryData<>("tension", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("bias", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("continuity", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("quality", null, true, Double.class));
        return builder.build();
    }

    private Expression<?> patternExpr;
    private Expression<Location> locationsExpr;
    private boolean isSpline = false;

    // section common entries
    private @Nullable Expression<Double> thicknessExpr;
    private @Nullable Expression<Boolean> hollowExpr;
    private @Nullable Expression<Mask> maskExpr;
    // section spline entries
    private @Nullable Expression<Double> tensionExpr, biasExpr, continuityExpr, qualityExpr;

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
            this.thicknessExpr = (Expression<Double>) container.getOptional("thickness", true);
            this.hollowExpr = (Expression<Boolean>) container.getOptional("hollow", true);
            this.maskExpr = (Expression<Mask>) container.getOptional("mask", true);

            // spline entries
            this.tensionExpr = (Expression<Double>) container.getOptional("tension", true);
            this.biasExpr = (Expression<Double>) container.getOptional("bias", true);
            this.continuityExpr = (Expression<Double>) container.getOptional("continuity", true);
            this.qualityExpr = (Expression<Double>) container.getOptional("quality", true);

            this.isSpline = container.hasEntry("tension") || container.hasEntry("bias") || container.hasEntry("continuity") || container.hasEntry("quality");
        }
        return true;
    }

    @Override
    protected @Nullable Runnable execute(Event event) {

        Location[] locations = locationsExpr.getArray(event);
        if (locations == null || locations.length < 2) {
            error("A Line/Spline needs at least 2 locations, but only 1 or none were given.");
            return null;
        }

        World world = locations[0].getWorld();
        // This probably can't happen anyway, but is here because I'm not sure
        if (world == null) {
            error("A world is needed for this operation, but no world was found. Make sure the first location includes a world.");
            return null;
        }

        Pattern pattern = PatternUtils.parseFrom(patternExpr.getArray(event));
        if (pattern == null) {
            error("Could not parse pattern '"+ patternExpr.toString() + "'. Make sure it's a valid block type or pattern.");
            return null;
        }

        // section entries
        double thickness = ExprUtils.getSingle(event, thicknessExpr, 0);
        boolean hollow = ExprUtils.getSingle(event, hollowExpr, false);
        Mask mask = ExprUtils.getSingle(event, maskExpr);

        // section spline entries
        double tension = ExprUtils.getSingle(event, tensionExpr, 0);
        double bias = ExprUtils.getSingle(event, biasExpr, 0);
        double continuity = ExprUtils.getSingle(event, continuityExpr, 0);
        double quality = ExprUtils.getSingle(event, qualityExpr, 10);

        return () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                session.setMask(mask);
                if (!isSpline) {
                    session.drawLine(pattern, List.of(Utils.toBlockVector3(locations)), thickness, !hollow);
                } else {
                    session.drawSpline(pattern, List.of(Utils.toBlockVector3(locations)), tension, bias, continuity, quality, thickness, !hollow);
                }
            }
        };
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (isAsync() ? "" : "lazily ") + "create line of " + patternExpr.toString(event, debug) + " at " + locationsExpr.toString(event, debug) + (isDelayed() ? " and wait" : "");
    }
}
