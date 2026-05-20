package me.tecspace.skriptworldedit.elements.Region.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import me.tecspace.skriptworldedit.api.utils.Utils;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Ellipse / Sphere")
@Description("""
        Creates an ellipse or simple spherical region which can be used in operations.
        An ellipse is a regular oval shape that looks like a circle that has been stretched out or flattened.
        If you want a flat ellipse, create a cylindrical region instead.
        """)
@Example("""
        set {_region} to a spherical region at {_loc} with radius 5
        set {_region} to a ellipsoid region at {_loc} with width 5, length 2 and height 3
        set {_region} to a ellipsoid region at {_loc} with size vector(5, 3, 2)
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprEllipsoidRegion extends SimpleExpression<RegionWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, DefaultSyntaxInfos.Expression.builder(ExprEllipsoidRegion.class, RegionWrapper.class)
                .supplier(ExprEllipsoidRegion::new)
                .addPattern("[a] [new] spher(e|ical) region (from|at) %location% with radius %number%")
                .addPattern("[a] [new] ellips(e|oid) region (from|at) %location% with width %number%[(,| and) length %-number% and height %-number%]")
                .addPattern("[a] [new] ellips(e|oid) region (from|at) %location% with size %vector%")
                .build()
        );
    }

    private int pattern;
    private Expression<Location> centerExpr;
    private @Nullable Expression<Number> radius1Expr;
    private @Nullable Expression<Number> radius2Expr;
    private @Nullable Expression<Number> radius3Expr;
    private @Nullable Expression<Vector> scaleExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        // shared values
        pattern = matchedPattern;
        centerExpr = (Expression<Location>) exprs[0];

        if (pattern == 0) { // sphere
            radius1Expr = (Expression<Number>) exprs[1];
        } else if (pattern == 1) { // ellipse
            radius1Expr = (Expression<Number>) exprs[1];
            radius2Expr = (Expression<Number>) exprs[2]; // length
            radius3Expr = (Expression<Number>) exprs[3]; // height
        } else { // from vector
            scaleExpr = (Expression<Vector>) exprs[1];
        }
        return true;
    }

    @Override
    protected RegionWrapper @Nullable [] get(Event event) {
        Location loc = centerExpr.getSingle(event);
        if (loc == null || loc.getWorld() == null) return null;

        Vector3 radiusVec;

        if (pattern == 0 && radius1Expr != null) { // sphere
            Number r = radius1Expr.getSingle(event);
            if (r == null) return null;
            radiusVec = Vector3.at(r.doubleValue(), r.doubleValue(), r.doubleValue());

        } else if (pattern == 1 && radius1Expr != null) { // ellipse
            Number r1 = radius1Expr.getSingle(event);
            Number r2 = (radius2Expr != null) ? radius2Expr.getSingle(event) : r1;
            Number r3 = (radius3Expr != null) ? radius3Expr.getSingle(event) : r1;

            if (r1 == null || r2 == null || r3 == null) return null;
            radiusVec = Vector3.at(r1.doubleValue(), r3.doubleValue(), r2.doubleValue());

        } else if (pattern == 2 && scaleExpr != null) { // from vector
            Vector v = scaleExpr.getSingle(event);
            if (v == null) return null;
            radiusVec = Vector3.at(v.getX(), v.getY(), v.getZ());

        } else {
            return null;
        }

        EllipsoidRegion region = new EllipsoidRegion(
                BukkitAdapter.adapt(loc.getWorld()),
                Utils.toBlockVector3(loc),
                radiusVec
        );

        return new RegionWrapper[]{new RegionWrapper(region, BukkitAdapter.adapt(loc.getWorld()))};
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
        return "ellipsoid region at " + centerExpr.toString(event, debug);
    }
}