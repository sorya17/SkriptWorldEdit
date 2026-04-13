package me.tecspace.skriptworldedit.elements.Region.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Transform (expand & contract)")
@Description("""
        Expand moves the boundaries of the region outwards.
        Contract moves the boundaries of the region inwards.
        Expand vertically expands the region from the highest y-level to the lowest.
        """)
@Examples("""
        expand {_region} vertically
        expand {_region} up by 1
        contract {_region} down by 1
        expand {_region} west, east, south and north by 1 block
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
// plan on redoing this, but works as is
public class EffTransform extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffTransform.class)
                .supplier(EffTransform::new)
                .addPattern("(:expand|contract) %worldeditregions% by vector %vectors%")
                .addPattern("(:expand|contract) %worldeditregions% %directions% by %number% [blocks]")
                .addPattern("expand %worldeditregions% vert[ically]")
                .build());
    }

    private boolean expand;
    private Expression<RegionWrapper> regionExpr;
    private Expression<Direction> directionExpr;
    private Expression<Number> blockAmount;
    private Expression<Vector> vectorExpr;
    private boolean vertically;
    private int matchedPattern;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        this.matchedPattern = matchedPattern;
        vertically = matchedPattern == 2;
        expand = parseResult.hasTag("expand");
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        if (matchedPattern == 0) {
            vectorExpr = (Expression<Vector>) exprs[1];
        } else if (matchedPattern == 1) {
            directionExpr = (Expression<Direction>) exprs[1];
            blockAmount = (Expression<Number>) exprs[2];
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        RegionWrapper[] wrappers = regionExpr.getArray(event);
        if (wrappers.length == 0) return;

        if (vertically) {
            for (RegionWrapper region : wrappers) {
                World weWorld = BukkitAdapter.adapt(region.world());
                region.region().expand(BlockVector3.at(0, weWorld.getMaxY() - region.region().getMaximumPoint().y(), 0));
                region.region().expand(BlockVector3.at(0, -(region.region().getMinimumPoint().y() - weWorld.getMinY()), 0));
            }
        } else if (matchedPattern == 0) {
            Vector[] vectors = vectorExpr.getArray(event);
            if (vectors.length == 0) return;

            for (RegionWrapper wrapper : wrappers) {
                for (Vector v : vectors) {
                    BlockVector3 vector = BlockVector3.at(v.getX(), v.getY(), v.getZ());
                    if (expand) {
                        wrapper.region().expand(vector);
                    } else {
                        wrapper.region().contract(vector);
                    }
                }
            }
        } else {
            Direction[] directions = directionExpr.getArray(event);
            Number amount = blockAmount.getSingle(event);
            if (directions.length == 0 || amount == null) return;

            for (RegionWrapper wrapper : wrappers) {
                for (Direction direction : directions) {
                    Vector dirVector = direction.getDirection();
                    BlockVector3 vector = BlockVector3.at(
                            dirVector.getX() * amount.doubleValue(),
                            dirVector.getY() * amount.doubleValue(),
                            dirVector.getZ() * amount.doubleValue()
                    );
                    if (expand) {
                        wrapper.region().expand(vector);
                    } else {
                        wrapper.region().contract(vector);
                    }
                }
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (vertically) return "expand " + regionExpr.toString(event, debug) + " vertically";
        String type = expand ? "expand " : "contract ";
        if (matchedPattern == 0) return type + regionExpr.toString(event, debug) + " by " + vectorExpr.toString(event, debug);
        return type + regionExpr.toString(event, debug) + " " + directionExpr.toString(event, debug) + " by " + blockAmount.toString(event, debug) + " blocks";
    }
}