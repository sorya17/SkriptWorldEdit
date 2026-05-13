package me.tecspace.skriptworldedit.elements.Region.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import me.tecspace.skriptworldedit.api.utils.Utils;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Cylinder")
@Description("""
        Creates a cylindrical region, which can be used for operations.
        """)
@Example("""
        set {_region} to cylindrical region at {_loc} with radius 7 and height 1
        set {_region} to cylindrical region at {_loc} with radii (4,7) and height 2
        set {_region} to cylindrical region at {_loc} with size vector(5,3,7)
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprCylinderRegion extends SimpleExpression<RegionWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, DefaultSyntaxInfos.Expression.builder(ExprCylinderRegion.class, RegionWrapper.class)
                .supplier(ExprCylinderRegion::new)
                .addPattern("[a] [new] cyl[ind(er|rical)] region (from|at) %location% with radi(i|us) %numbers% and height %integer%")
                .addPattern("[a] [new] cyl[ind(er|rical)] region (from|at) %location% with size %vector%")
                .build()
        );
    }

    private Expression<Location> centerExpr;
    private @Nullable Expression<Number> radiiExpr;
    private @Nullable Expression<Vector> sizeExpr;
    private @Nullable Expression<Number> heightExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.centerExpr = (Expression<Location>) exprs[0];
        if (matchedPattern == 0) {
            this.radiiExpr = (Expression<Number>) exprs[1];
            this.heightExpr = (Expression<Number>) exprs[2];
        } else {
            this.sizeExpr = (Expression<Vector>) exprs[1];
        }
        return true;
    }

    @Override
    protected RegionWrapper @Nullable [] get(Event event) {
        if (centerExpr == null) return null;
        Location center = centerExpr.getSingle(event);
        if (center == null) return null;

        double width, length;
        int height;

        if (sizeExpr != null) {
            Vector size = this.sizeExpr.getSingle(event);
            if (size == null) return null;

            width = size.getX();
            height = size.getBlockY();
            length = size.getZ();

        } else {
            if (radiiExpr == null || heightExpr == null) return null;

            Number[] radii = radiiExpr.getAll(event);
            if (radii == null || radii.length == 0) return null;
            width = radii[0].doubleValue();
            length = (radii.length == 1) ? width : radii[1].doubleValue();

            Number heightNum = heightExpr.getSingle(event);
            if (heightNum == null) return null;
            height = heightNum.intValue();
        }

        CylinderRegion region = new CylinderRegion(
                Utils.toBlockVector3(center),
                Vector2.at(width, length),
                center.getBlockY(),
                center.getBlockY() + height - 1
        );

        return new RegionWrapper[]{
                new RegionWrapper(region, center.getWorld())
        };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends RegionWrapper> getReturnType() {
        return RegionWrapper.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (sizeExpr != null) {
            return "cylindrical region at " + centerExpr.toString() + " with size " + sizeExpr.toString();
        } else {
            assert heightExpr != null;
            assert radiiExpr != null;
            return "cylindrical region at " + centerExpr.toString() + " with radii " + radiiExpr.toString() + " and height " + heightExpr.toString();
        }
    }
}