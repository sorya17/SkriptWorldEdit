package me.tecspace.skriptworldedit.elements.Region.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Cylinder")
@Description("""
        Creates a cylindrical region, which can be used for operations.
        It can have any width, length and height.
        """)
@Example("""
        set {_region} to a new cylindrical region at {_loc} with radius 7 and height 1"
        set {_region} to a new cylindrical region at {_loc} with width 4, length 6 and height 3
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprCylinderRegion extends SimpleExpression<RegionWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, DefaultSyntaxInfos.Expression.builder(ExprCylinderRegion.class, RegionWrapper.class)
                .supplier(ExprCylinderRegion::new)
                .addPattern(
                        "[a] [new] cyl[ind(er|rical)] region (from|at) %location% with (radius|width) %number%[(,| and) length %-number%] and height %number%"
                )
                .build()
        );
    }

    private Expression<Location> center;
    private Expression<Number> width;
    private @Nullable Expression<Number> length;
    private Expression<Number> height;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.center = (Expression<Location>) exprs[0];
        this.width = (Expression<Number>) exprs[1];
        this.length = (Expression<Number>) exprs[2];
        this.height = (Expression<Number>) exprs[3];
        return true;
    }

    @Override
    protected RegionWrapper @Nullable [] get(Event event) {
        if (center == null || width == null || height == null) return null;

        Location loc = center.getSingle(event);
        Number widthNum = width.getSingle(event);
        Number heightNum = height.getSingle(event);
        Number lengthNum = length != null ? length.getSingle(event) : null;

        if (loc == null || widthNum == null || heightNum == null) return null;

        double w = widthNum.doubleValue();
        double l = lengthNum != null ? lengthNum.doubleValue() : w; // use width
        int h = heightNum.intValue();

        CylinderRegion region = new CylinderRegion(
                BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()),
                Vector2.at(w, l),
                loc.getBlockY(),
                loc.getBlockY() + h - 1
        );

        return new RegionWrapper[]{
                new RegionWrapper(region, loc.getWorld())
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
        return "cylindrical region at " + center.toString() + " with width " + width.toString() + ", length " + length.toString() + " and height " + height.toString();
    }
}