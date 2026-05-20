package me.tecspace.skriptworldedit.elements.Region.properties;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Region - Vertices")
@Description("""
        The vertices of a convex polyhedral or cuboid region.
        You can add or remove locations.
        """)
@Examples("""
        set {_locations::*} to all region vertices of {_region}
        add player's location to vertices of {_region}
        remove player's location from vertices of {_region}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class PropRegionVertices extends SimplePropertyExpression<RegionWrapper, Location> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(PropRegionVertices.class, Location.class,
                "[region] vertices", "worldeditregions", false)
                .supplier(PropRegionVertices::new)
                .addPattern("all [[of] the] [region] vertices of %worldeditregions%")
                .build());
    }

    @Override
    protected Location[] get(Event event, RegionWrapper[] regions) {
        List<Location> locations = new ArrayList<>();
        for (RegionWrapper wrapper : regions) {
            if (wrapper.region() instanceof ConvexPolyhedralRegion convex) {
                for (BlockVector3 vertex : convex.getVertices()) {
                    locations.add(new Location(
                            BukkitAdapter.adapt(wrapper.world()),
                            vertex.x(),
                            vertex.y(),
                            vertex.z())
                    );
                }
            }
        }
        return locations.toArray(new Location[0]);
    }

    @Override
    public @Nullable Location convert(RegionWrapper wrapper) {
        if (wrapper.region() instanceof ConvexPolyhedralRegion convex) {
            BlockVector3 first = convex.getVertices().stream().findFirst().orElse(null);
            if (first != null) {
                return new Location(BukkitAdapter.adapt(wrapper.world()),
                        first.x(),
                        first.y(),
                        first.z()
                );
            }
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE -> new Class[]{Location.class};
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
        if (delta == null) return;
        RegionWrapper[] regions = getExpr().getAll(event);
        if (regions == null) return;

        for (RegionWrapper wrapper : regions) {
            if (!(wrapper.region() instanceof ConvexPolyhedralRegion convex)) continue;

            for (Object obj : delta) {
                if (!(obj instanceof Location loc)) continue;

                if (!loc.getWorld().equals(wrapper.world())) continue;

                BlockVector3 vector = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

                if (mode == Changer.ChangeMode.ADD) {
                    convex.addVertex(vector);
                } else if (mode == Changer.ChangeMode.REMOVE) {
                    removeVertex(convex, vector);
                }
            }
        }
    }

    private static void removeVertex(ConvexPolyhedralRegion region, BlockVector3 toRemove) {
        List<BlockVector3> vertices = new ArrayList<>(region.getVertices());
        if (vertices.remove(toRemove)) {
            region.clear();
            for (BlockVector3 v : vertices) {
                region.addVertex(v);
            }
        }
    }

    @Override
    protected String getPropertyName() {
        return "region vertices";
    }

    @Override
    public Class<? extends Location> getReturnType() {
        return Location.class;
    }
}