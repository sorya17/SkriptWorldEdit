package me.tecspace.skworldedit.elements.Region.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.math.BlockVector3;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.utils.Utils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Shift")
@Description("Moves the entire region without changing its size.")
@Examples("shift {_region} by vector(0, 5, 0)")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffShift extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffShift.class)
                .supplier(EffShift::new)
                .addPattern("(shift|move) %worldeditregions% (by|along) %vector%")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private Expression<Vector> vectorExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        vectorExpr = (Expression<Vector>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (regionExpr == null || vectorExpr == null) return;

        Vector vector = vectorExpr.getSingle(event);
        if (vector == null) return;

        BlockVector3 vector3 = Utils.toBlockVector3(vector);

        for (RegionWrapper region : regionExpr.getArray(event)) {
            region.region().shift(vector3);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "shift " + regionExpr.toString() + " by " + vectorExpr.toString();
    }
}
