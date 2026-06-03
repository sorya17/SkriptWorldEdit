package me.tecspace.skworldedit.api;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.StateApplyingPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.convolution.GaussianKernel;
import com.sk89q.worldedit.math.convolution.HeightMapFilter;
import com.sk89q.worldedit.math.convolution.SnowHeightMap;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.*;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockTypes;
import me.tecspace.skworldedit.api.utils.SchematicUtils;
import me.tecspace.skworldedit.api.utils.Utils;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Pairs a {@link Region} with a {@link World}.
 */
public record RegionWrapper(Region region, World world) {

    public @NotNull String toString() {
        return switch (region) {
            case CylinderRegion cylinder ->
                    String.format("Cylindrical Region at %s in world \"%s\" with size %s",
                            cylinder.getCenter(),
                            world.getName(),
                            cylinder.getDimensions()
                    );
            case EllipsoidRegion ellipsoid ->
                    String.format("Ellipsoid Region at %s in world \"%s\" with size %s",
                            ellipsoid.getCenter(),
                            world.getName(),
                            ellipsoid.getDimensions()
                    );
            case ConvexPolyhedralRegion convex ->
                    String.format("Convex Polyhedral Region at %s in world \"%s\" with size %s and %s vertices",
                            convex.getCenter(),
                            world.getName(),
                            convex.getDimensions(),
                            convex.getVertices().size()
                    );
            default ->
                    String.format("Cuboid Region between %s and %s in world \"%s\" with size %s",
                            region.getMinimumPoint(),
                            region.getMaximumPoint(),
                            world.getName(),
                            region.getDimensions()
                    );
        };
    }

    /**
     * Sets the blocks in the region using a given pattern.
     */
    public void setBlocks(Pattern pattern) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.setBlocks(region, pattern);
        }
    }

    /**
     * Replaces the blocks matching a given mask with a pattern in the region.
     */
    public void replaceBlocks(Mask mask, Pattern pattern) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.replaceBlocks(region, mask, pattern);
        }
    }

    /**
     * Counts the blocks in the region matching a given mask.
     * @return Amount of blocks counted
     */
    public int countBlocks(Mask mask) {
        return Objects.requireNonNull(Utils.getAsyncBlocking(-1, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
                return session.countBlocks(region, mask);
            }
        }));
    }

    /**
     * Makes the walls of the region using a given pattern.
     */
    public void makeWalls(Pattern pattern, @Nullable Mask mask) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            if (mask != null) session.setMask(mask);
            session.makeWalls(region, pattern);
        }
    }

    /**
     * Makes the faces of the region using a given pattern.
     */
    public void makeFaces(Pattern pattern, @Nullable Mask mask) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            if (mask != null) session.setMask(mask);
            session.makeFaces(region, pattern);
        }
    }

    /**
     * Makes the region hollow (inverse of faces).
     */
    public void makeHollow(@Nullable Pattern pattern, @Nullable Mask mask, int thickness) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            Pattern actualPattern = (pattern != null) ? pattern : BlockTypes.AIR;
            Mask actualMask = (mask != null) ? mask : new SolidBlockMask(session);
            session.hollowOutRegion(region, thickness, actualPattern, actualMask);
        }
    }

    /**
     * Overlays the top blocks in the region using a pattern.
     */
    public void overlay(Pattern pattern) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.overlayCuboidBlocks(region, pattern);
        }
    }

    /**
     * Naturalizes the region. This changes the top 3 layers of blocks into grass & dirt, and the bottom layers into stone.
     */
    public void naturalize() {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.naturalizeCuboidBlocks(region);
        }
    }

    /**
     * Copies the contents of the region to a location.
     */
    public void copy(Location to, @Nullable Location center, boolean copyBiomes, boolean copyEntities, @Nullable Mask mask, @Nullable Mask sourceMask, @Nullable Transform transform) {
        BlockVector3 sourcePos = (center != null) ? Utils.toBlockVector3(center) : region.getMinimumPoint();

        boolean sameWorld = BukkitAdapter.adapt(world).equals(to.getWorld());
        try (EditSession sourceSession = WorldEdit.getInstance().newEditSession(world);
             EditSession destSession = sameWorld ? sourceSession : WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(to.getWorld()))) {

            if (mask != null) destSession.setMask(mask);

            ForwardExtentCopy copy = new ForwardExtentCopy(sourceSession, region, sourcePos, destSession, Utils.toBlockVector3(to));
            copy.setCopyingBiomes(copyBiomes);
            copy.setCopyingEntities(copyEntities);
            if (transform != null) copy.setTransform(transform);
            if (sourceMask != null) copy.setSourceMask(sourceMask);
            Operations.complete(copy);
        }
    }

    /**
     * Saves the region (as a schematic for example, depending on given format).
     */
    public void save(Path savePath, ClipboardFormat format, boolean copyEntities, boolean copyBiomes, @Nullable Location origin, @Nullable Mask sourceMask, @Nullable Transform transform) {
        SchematicUtils.create(this, savePath, format, copyEntities, copyBiomes, origin, sourceMask, transform);
    }

    /**
     * Regenerates the region
     */
    public void regenerate(RegenOptions options, @Nullable Mask mask) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {

            if (mask != null) session.setMask(mask);
            // session.regenerate seems to be broken
            world.regenerate(region, session, options);
        }
    }

    /**
     * Drains the region of water, while also removing the waterlogged state of any blocks.
     */
    public void drain() {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            Map<String, String> states = new HashMap<>();
            states.put("waterlogged", "false");
            StateApplyingPattern pattern = new StateApplyingPattern(session, states);
            BlockTypeMask waterMask = new BlockTypeMask(session, BlockTypes.WATER);
            session.setBlocks(region, pattern);
            session.replaceBlocks(region, waterMask, BlockTypes.AIR);
        }
    }

    /**
     * Floods the region with water, while also waterlogging all compatible blocks.
     */
    public void flood() {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            Map<String, String> states = new HashMap<>();
            states.put("waterlogged", "true");
            StateApplyingPattern pattern = new StateApplyingPattern(session, states);
            BlockTypeMask airMask = new BlockTypeMask(session, BlockTypes.AIR);
            session.setBlocks(region, pattern);
            session.replaceBlocks(region, airMask, BlockTypes.WATER);
        }
    }

    /**
     * Sets the biome in the region.
     */
    public void setBiome(BiomeType biome) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            for (BlockVector3 point : region) {
                session.setBiome(point, biome);
            }
        }
    }

    /**
     * Makes vanilla-like single snow layers and ice in the region.
     */
    public void simulateSnow() {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            session.simulateSnow(region.getBoundingBox(), true);
        }
    }

    /**
     * Makes smooth snow layers and ice in the region.
     * @param iterations how many smoothing passes (default 1)
     * @param snowBlockCount how many solid snow blocks under the top snow layer (default 1)
     * @param mask optional mask to restrict which blocks are used as the heightmap
     */
    public void simulateSmoothSnow(int iterations, int snowBlockCount, @Nullable Mask mask) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            SnowHeightMap heightMap = new SnowHeightMap(session, region, mask);
            HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
            float[] changed = heightMap.applyFilter(filter, iterations);
            heightMap.applyChanges(changed, snowBlockCount);
        }
    }
}