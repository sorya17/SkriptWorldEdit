package me.tecspace.skworldedit.api;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.World;
import me.tecspace.skworldedit.api.utils.Utils;
import org.bukkit.Location;

import java.util.List;

public class Shapes {

    /**
     * Draws a line between two or more points.
     */
    public static void drawLine(Pattern pattern, Location[] points, double radius, boolean filled) {
        if (points == null || points.length < 2) return;
        World world = BukkitAdapter.adapt(points[0].getWorld());
        BlockVector3[] vectors = Utils.toBlockVector3(points);
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            if (vectors.length == 2) {
                session.drawLine(pattern, vectors[0], vectors[1], radius, filled);
            } else {
                session.drawLine(pattern, List.of(vectors), radius, filled);
            }
        }
    }

    /**
     * Draws a spline between two or more points.
     */
    public static void drawSpline(Pattern pattern, Location[] points, double tension, double bias, double continuity, double quality, double radius, boolean filled) {
        if (points == null || points.length < 2) return;
        World world = BukkitAdapter.adapt(points[0].getWorld());
        BlockVector3[] vectors = Utils.toBlockVector3(points);
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.drawSpline(pattern, List.of(vectors), tension, bias, continuity, quality, radius, filled);
        }
    }

    /**
     * Makes a distorted sphere (blob) at the given location.
     *
     * @param position center of the blob
     * @param pattern block pattern
     * @param size base size
     * @param frequency noise frequency (higher = more distorted)
     * @param amplitude noise amplitude (higher = more distorted)
     * @param radius per-axis radius as a Vector3
     * @param sphericity 1.0 = sphere, 0.0 = fully noise-driven
     */
    public static void makeBlob(Location position, Pattern pattern, double size, double frequency, double amplitude, Vector3 radius, double sphericity) {
        World world = BukkitAdapter.adapt(position.getWorld());
        BlockVector3 pos = Utils.toBlockVector3(position);
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.makeBlob(pos, pattern, size, frequency, amplitude, radius, sphericity);
        }
    }

    /**
     * Makes a circle (disc) at the given location.
     *
     * @param position center
     * @param pattern block pattern
     * @param radiusX X radius
     * @param radiusY Y radius
     * @param radiusZ Z radius
     * @param filled whether to fill the circle
     * @param normal normal vector (orientation of the circle plane)
     */
    public static void makeCircle(Location position, Pattern pattern, double radiusX, double radiusY, double radiusZ, boolean filled, Vector3 normal) {
        World world = BukkitAdapter.adapt(position.getWorld());
        BlockVector3 pos = Utils.toBlockVector3(position);
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.makeCircle(pos, pattern, radiusX, radiusY, radiusZ, filled, normal);
        }
    }

    /**
     * Makes a cone at the given location.
     *
     * @param position apex of the cone
     * @param pattern block pattern
     * @param radiusX X radius at base
     * @param radiusZ Z radius at base
     * @param height height of the cone
     * @param filled whether to fill the cone
     * @param thickness wall thickness (0 = use filled)
     */
    public static void makeCone(Location position, Pattern pattern, double radiusX, double radiusZ, int height, boolean filled, double thickness) {
        World world = BukkitAdapter.adapt(position.getWorld());
        BlockVector3 pos = Utils.toBlockVector3(position);
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.makeCone(pos, pattern, radiusX, radiusZ, height, filled, thickness);
        }
    }

    /**
     * Makes a cylinder at the given location.
     *
     * @param position center of the base
     * @param pattern block pattern
     * @param radiusX X radius
     * @param radiusZ Z radius
     * @param height height
     * @param filled whether to fill the cylinder
     */
    public static void makeCylinder(Location position, Pattern pattern, double radiusX, double radiusZ, int height, boolean filled) {
        World world = BukkitAdapter.adapt(position.getWorld());
        BlockVector3 pos = Utils.toBlockVector3(position);
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.makeCylinder(pos, pattern, radiusX, radiusZ, height, filled);
        }
    }

    /**
     * Makes a pyramid at the given location.
     *
     * @param position center base position
     * @param pattern block pattern
     * @param size size (half-width of base)
     * @param filled whether to fill the pyramid
     */
    public static void makePyramid(Location position, Pattern pattern, int size, boolean filled) {
        World world = BukkitAdapter.adapt(position.getWorld());
        BlockVector3 pos = Utils.toBlockVector3(position);
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.makePyramid(pos, pattern, size, filled);
        }
    }

    /**
     * Makes a sphere or ellipsoid at the given location.
     *
     * @param position center
     * @param pattern block pattern
     * @param radiusX X radius
     * @param radiusY Y radius
     * @param radiusZ Z radius
     * @param filled whether to fill the sphere
     */
    public static void makeSphere(Location position, Pattern pattern, double radiusX, double radiusY, double radiusZ, boolean filled) {
        World world = BukkitAdapter.adapt(position.getWorld());
        BlockVector3 pos = Utils.toBlockVector3(position);
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            //session.setMask();
            session.makeSphere(pos, pattern, radiusX, radiusY, radiusZ, filled);
        }
    }
}