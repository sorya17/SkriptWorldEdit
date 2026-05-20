package me.tecspace.skriptworldedit.elements.Shapes.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.function.mask.Mask;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import me.tecspace.skriptworldedit.api.lang.AsyncSection;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

//TODO
public class SecCreateShape extends AsyncSection {

    private static EntryValidator VALIDATOR;

    public static void register(SyntaxRegistry registry) {
        VALIDATOR = buildValidator();
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(SecCreateShape.class)
                .supplier(SecCreateShape::new)
                .addPattern("[:lazily] create (shape:(line|spline|blob|circle|cone|cylinder|pyramid|sphere)) at %locations%")
                .build());
    }

    private static EntryValidator buildValidator() {
        EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
        // common entries
        builder.addEntryData(new ExpressionEntryData<>("copy entities", new SimpleLiteral<>(false, true), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("copy biomes", new SimpleLiteral<>(false, true), true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("mask", null, true, Mask.class));
        builder.addEntryData(new ExpressionEntryData<>("source mask", null, true, Mask.class));
        // transformation related entries
        builder.addEntryData(new ExpressionEntryData<>("rotation", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("scale", null, true, Vector.class));
        builder.addEntryData(new ExpressionEntryData<>("offset", null, true, Vector.class));
        return builder.build();
    }

    private Expression<RegionWrapper> regionsExpr;
    private Expression<Location> locationsExpr;

    private @Nullable Expression<Mask> maskExpr, sourceMaskExpr;
    private @Nullable Expression<Boolean> copyEntities, copyBiomes;
    // transformation related entries
    private @Nullable Expression<Double> rotationExpr;
    private @Nullable Expression<Vector> scaleExpr, offsetExpr;


    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        return false;
    }

    @Override
    protected void execute(Event event) {

    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "create shape";
    }
}
