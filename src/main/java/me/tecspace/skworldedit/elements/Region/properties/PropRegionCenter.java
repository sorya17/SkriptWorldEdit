package me.tecspace.skworldedit.elements.Region.properties;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import me.tecspace.skworldedit.api.RegionWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Region - Center")
@Description("""
        The center location of a region.
        NOTE: Because regions use block coordinates, the true center may be offset by 0.5 blocks -
        Use 'exact region center' for the precise center location.
        """)
@Example("set block at region center of {_region} to gold_block")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class PropRegionCenter extends PropertyExpression<RegionWrapper, Location> {

    public static void register(SyntaxRegistry registry) {
        registry.register(
                SyntaxRegistry.EXPRESSION,
                infoBuilder(
                        PropRegionCenter.class,
                        Location.class,
                        "[exact:(exact|true)] region cent(re|er)[s]",
                        "worldeditregions",
                        false
                )
                        .supplier(PropRegionCenter::new)
                        .build()
        );
    }

    private boolean isExact;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends RegionWrapper>) expressions[0]);
        isExact = parseResult.hasTag("exact");
        return true;
    }

    @Override
    protected Location[] get(Event event, RegionWrapper[] regions) {
        List<Location> centres = new ArrayList<>();
        for (RegionWrapper region : regions) {
            Region weRegion = region.region();
            Vector3 center = weRegion.getCenter();
            World world = BukkitAdapter.adapt(region.world());
            if (isExact) {
                // getCenter gets the center between the lesser corner of the greater block of the region so let's adjust for that
                centres.add(new Location(world, center.x() + 0.5, center.y() + 0.5, center.z() + 0.5));
            } else {
                centres.add(new Location(world, center.x(), center.y(), center.z()));
            }
        }
        return centres.toArray(new Location[0]);
    }

    public boolean isSingle() {
        return getExpr().isSingle();
    }

    @Override
    public Class<? extends Location> getReturnType() {
        return Location.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "region center of " + getExpr().toString(event, debug);
    }
}