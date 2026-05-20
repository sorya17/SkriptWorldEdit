package me.tecspace.skriptworldedit.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.ContainsHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

public class RegionClassInfo {

    @SuppressWarnings("UnstableApiUsage")
    public static void register(SkriptAddon addon) {
        Classes.registerClass(new ClassInfo<>(RegionWrapper.class, "worldeditregion")
                .user("worldedit ?regions?")
                .name("WorldEdit Region")
                .description("Represents a WorldEdit Region (cuboid, polygon, ellipsoid, etc.)")
                .requiredPlugins("WorldEdit")
                .since("1.0")
                .parser(new Parser<>() {
                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(RegionWrapper region, int flags) {
                        return region.toString();
                    }

                    @Override
                    public String toVariableNameString(RegionWrapper region) {
                        return "worldeditregion:" + region.toString();
                    }
                })
                .property(Property.SCALE, "The scale of a region. returns a vector. If the region is not a cuboid it will still return the cuboid dimensions.", addon,
                        ExpressionPropertyHandler.of(region -> {
                            BlockVector3 dimensions = region.region().getDimensions();
                            return new Vector(dimensions.x(), dimensions.y(), dimensions.z());
                        }, Vector.class))

                .property(Property.SIZE, "The size/volume of a region. returns the amount of blocks.", addon,
                        ExpressionPropertyHandler.of(region -> region.region().getVolume(), Long.class))

                .property(Property.CONTAINS, "Regions can contain locations, blocks, chunks and other regions.", addon,
                        new ContainsHandler<RegionWrapper, Object>() {
                            @Override
                            public boolean contains(RegionWrapper region, Object element) {
                                return switch (element) {
                                    case Chunk chunk -> region.region().containsChunk(chunk.getX(), chunk.getZ());
                                    case Location location -> region.region().contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                                    case RegionWrapper wrapper -> {
                                        var min = wrapper.region().getMinimumPoint();
                                        var max = wrapper.region().getMaximumPoint();
                                        yield region.region().containsEntireCuboid(
                                                min.x(), max.x(),
                                                min.y(), max.y(),
                                                min.z(), max.z()
                                        );
                                    }
                                    case Block block -> region.region().contains(block.getX(), block.getY(), block.getZ());
                                    default -> false;
                                };
                            }

                            @Override
                            public Class<?>[] elementTypes() {
                                return new Class[]{Chunk.class, Location.class, RegionWrapper.class};
                            }
                        })

                // region serializer
                .serializer(new Serializer<>() {
                    @Override
                    public Fields serialize(RegionWrapper regionWrapper) throws NotSerializableException {
                        Fields fields = new Fields();
                        fields.putObject("world", regionWrapper.world());
                        var region = regionWrapper.region();
                        switch (region) {
                            case CuboidRegion cuboid -> {
                                fields.putObject("regionType", "cuboid");
                                BlockVector3 min = cuboid.getMinimumPoint();
                                BlockVector3 max = cuboid.getMaximumPoint();
                                fields.putPrimitive("minX", min.x());
                                fields.putPrimitive("minY", min.y());
                                fields.putPrimitive("minZ", min.z());
                                fields.putPrimitive("maxX", max.x());
                                fields.putPrimitive("maxY", max.y());
                                fields.putPrimitive("maxZ", max.z());
                            }
                            case CylinderRegion cylinder -> {
                                fields.putObject("regionType", "cylinder");
                                fields.putPrimitive("height", cylinder.getHeight());
                                Vector2 radius = cylinder.getRadius();
                                fields.putPrimitive("radiusX", radius.x());
                                fields.putPrimitive("radiusZ", radius.z());
                                Vector3 center = cylinder.getCenter().withY(cylinder.getMinimumY());
                                fields.putPrimitive("centerX", center.x());
                                fields.putPrimitive("centerY", center.y());
                                fields.putPrimitive("centerZ", center.z());
                            }
                            case EllipsoidRegion ellipsoid -> {
                                fields.putObject("regionType", "ellipsoid");
                                Vector3 center = ellipsoid.getCenter();
                                fields.putPrimitive("centerX", center.x());
                                fields.putPrimitive("centerY", center.y());
                                fields.putPrimitive("centerZ", center.z());
                                Vector3 radius = ellipsoid.getRadius();
                                fields.putPrimitive("radiusX", radius.x());
                                fields.putPrimitive("radiusY", radius.y());
                                fields.putPrimitive("radiusZ", radius.z());
                            }
                            case ConvexPolyhedralRegion convex -> {
                                fields.putObject("regionType", "convex");
                                BlockVector3[] vertices = convex.getVertices().toArray(new BlockVector3[0]);
                                fields.putPrimitive("size", vertices.length);
                                for (int i = 0; i < vertices.length; i++) {
                                    fields.putPrimitive(String.format("vertex-%dX", i), vertices[i].x());
                                    fields.putPrimitive(String.format("vertex-%dY", i), vertices[i].y());
                                    fields.putPrimitive(String.format("vertex-%dZ", i), vertices[i].z());
                                }
                            }
                            default -> throw new NotSerializableException();
                        }
                        return fields;
                    }

                    @Override
                    protected RegionWrapper deserialize(Fields fields) throws StreamCorruptedException {
                        String regionType = fields.getObject("regionType", String.class);
                        World bukkitWorld = fields.getObject("world", World.class);
                        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(bukkitWorld);
                        assert regionType != null;
                        return switch (regionType) {
                            case "cuboid" -> {
                                int minX = fields.getPrimitive("minX", int.class);
                                int minY = fields.getPrimitive("minY", int.class);
                                int minZ = fields.getPrimitive("minZ", int.class);
                                BlockVector3 pos1 = BlockVector3.at(minX, minY, minZ);
                                int maxX = fields.getPrimitive("maxX", int.class);
                                int maxY = fields.getPrimitive("maxY", int.class);
                                int maxZ = fields.getPrimitive("maxZ", int.class);
                                BlockVector3 pos2 = BlockVector3.at(maxX, maxY, maxZ);
                                yield new RegionWrapper(new CuboidRegion(pos1, pos2), world);
                            }
                            case "cylinder" -> {
                                double radiusX = fields.getPrimitive("radiusX", double.class);
                                double radiusZ = fields.getPrimitive("radiusZ", double.class);
                                Vector2 radius = Vector2.at(radiusX, radiusZ);
                                int centerX = fields.getPrimitive("centerX", int.class);
                                int centerY = fields.getPrimitive("centerY", int.class);
                                int centerZ = fields.getPrimitive("centerZ", int.class);
                                BlockVector3 center = BlockVector3.at(centerX, centerY, centerZ);
                                int height = fields.getPrimitive("height", int.class);
                                yield new RegionWrapper(new CylinderRegion(center, radius, centerY, (centerY + height)), world);
                            }
                            case "ellipsoid" -> {
                                int centerX = fields.getPrimitive("centerX", int.class);
                                int centerY = fields.getPrimitive("centerY", int.class);
                                int centerZ = fields.getPrimitive("centerZ", int.class);
                                BlockVector3 center = BlockVector3.at(centerX, centerY, centerZ);
                                int radiusX = fields.getPrimitive("radiusX", int.class);
                                int radiusY = fields.getPrimitive("radiusY", int.class);
                                int radiusZ = fields.getPrimitive("radiusZ", int.class);
                                Vector3 radius = Vector3.at(radiusX, radiusY, radiusZ);
                                yield new RegionWrapper(new EllipsoidRegion(center, radius), world);
                            }
                            case "convex" -> {
                                ConvexPolyhedralRegion region = new ConvexPolyhedralRegion(world);
                                int size = fields.getPrimitive("size", int.class);
                                for (int i = 0; i < size; i++) {
                                    int x = fields.getPrimitive(String.format("vertex-%dX", i), int.class);
                                    int y = fields.getPrimitive(String.format("vertex-%dY", i), int.class);
                                    int z = fields.getPrimitive(String.format("vertex-%dZ", i), int.class);
                                    region.addVertex(BlockVector3.at(x, y, z));
                                }
                                yield new RegionWrapper(region, world);
                            }
                            default -> throw new StreamCorruptedException();
                        };
                    }

                    @Override
                    public boolean mustSyncDeserialization() {
                        return false;
                    }

                    @Override
                    protected boolean canBeInstantiated() {
                        return false;
                    }
                })
        );
    }
}