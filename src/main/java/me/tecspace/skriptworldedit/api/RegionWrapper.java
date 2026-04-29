package me.tecspace.skriptworldedit.api;

import com.fastasyncworldedit.core.extent.NullExtent;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
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
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockTypes;
import me.tecspace.skriptworldedit.api.utils.SchematicUtils;
import me.tecspace.skriptworldedit.api.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public record RegionWrapper(Region region, World world) {

    public String getRegionType() {
        return region.getClass().getSimpleName().replace("Region", "");
    }

    /**
     * Describes the region in natural language (for skript)
     */
    public String describe() {
        // To-do: maybe add more descriptive strings for different types of regions
        // Cuboid Region between (10, 64, 10) to (20, 70, 20) in world "world" (160 blocks)
        return String.format("%s Region between %s and %s in world \"%s\" with scale %s",
                region.getClass().getSimpleName().replace("Region", ""),
                region.getMinimumPoint().toString(),
                region.getMaximumPoint().toString(),
                world.getName(),
                region.getDimensions().toString()
        );
    }

    /**
     * Sets the blocks in the region using a given pattern.
     */
    public void setBlocks(PatternWrapper pattern, boolean async) {
        Utils.run(async, () -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                editSession.setBlocks(region, pattern.pattern());
            } catch (MaxChangedBlocksException ignored) {}
        });
    }

    /**
     * Replaces the blocks matching a given mask with a pattern in the region.
     */
    public void replaceBlocks(MaskWrapper mask, PatternWrapper pattern, boolean async) {
        Utils.run(async, () -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                editSession.replaceBlocks(region, mask.mask(), pattern.pattern());
            } catch (MaxChangedBlocksException ignored) {}
        });
    }

    /**
     * Counts the blocks in the region matching a given mask.
     * @return Amount of blocks counted
     */
    public int countBlocks(MaskWrapper mask) {
        return Objects.requireNonNull(Utils.evalAsyncBlocking(-1, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                return session.countBlocks(region, mask.mask());
            }
        }));
    }

    /**
     * Makes the walls of the region using a given pattern.
     */
    public void makeWalls(PatternWrapper pattern, @Nullable MaskWrapper mask, boolean async) {
        Utils.run(async, () -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                if (mask != null) editSession.setMask(mask.mask());
                editSession.makeWalls(region, pattern.pattern());
            } catch (MaxChangedBlocksException ignored) {}
        });
    }

    /**
     * Makes the faces of the region using a given pattern.
     */
    public void makeFaces(PatternWrapper pattern, @Nullable MaskWrapper mask, boolean async) {
        Utils.run(async, () -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                if (mask != null) editSession.setMask(mask.mask());
                editSession.makeFaces(region, pattern.pattern());
            } catch (MaxChangedBlocksException ignored) {}
        });
    }

    /**
     * Makes the region hollow (inverse of faces).
     */
    public void makeHollow(@Nullable PatternWrapper pattern, @Nullable MaskWrapper mask, int thickness, boolean async) {
        Utils.run(async, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                Pattern actualPattern = pattern != null ? pattern.pattern() : BlockTypes.AIR;
                Mask actualMask = mask != null ? mask.mask() : new SolidBlockMask(session);
                session.hollowOutRegion(region, thickness, actualPattern, actualMask);
            }
        });
    }

    /**
     * Overlays the top blocks in the region using a pattern.
     */
    public void overlay(PatternWrapper pattern, boolean async) {
        Utils.run(async, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                session.overlayCuboidBlocks(region, pattern.pattern());
            }
        });
    }

    /**
     * Naturalizes the region. This changes the top 3 layers of blocks into grass & dirt, and the bottom layers into stone.
     */
    public void naturalize(boolean async) {
        Utils.run(async, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                session.naturalizeCuboidBlocks(region);
            }
        });
    }

    /**
     * Copies the contents of the region to a location.
     */
    public void copy(Location to, @Nullable Location center, boolean copyBiomes, boolean copyEntities, @Nullable Mask mask, @Nullable Transform transform, boolean async) {
        BlockVector3 fromSource = (center != null) ? Utils.toBlockVector3(center) : region.getMinimumPoint();
        if (!to.isWorldLoaded()) return;
        Utils.run(async, () -> {
            try (EditSession destinationSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(to.getWorld()))) {
                ForwardExtentCopy copy = new ForwardExtentCopy(
                        BukkitAdapter.adapt(world),
                        region,
                        fromSource,
                        destinationSession,
                        Utils.toBlockVector3(to));
                copy.setCopyingBiomes(copyBiomes);
                copy.setCopyingEntities(copyEntities);
                if (transform != null) copy.setTransform(transform);
                if (mask != null) copy.setSourceMask(mask);
                Operations.complete(copy);
            }
        });
    }

    /**
     * Saves the region (as a schematic for example, depending on given format).
     */
    public void save(Path savePath, ClipboardFormat format, boolean copyEntities, boolean copyBiomes, @Nullable Location origin, @Nullable Mask sourceMask, @Nullable Transform transform) {
        SchematicUtils.create(this, savePath, format, copyEntities, copyBiomes, origin, sourceMask, transform);
    }

    /**
     * Regenerates the region, optionally with a set seed, else uses the seed of the world that the region is in.
     */
    public void regenerate(@Nullable Long seed, boolean regenBiomes, boolean async) {
        Utils.run(async, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                RegenOptions options = RegenOptions.builder()
                    .seed(seed != null ? seed : world.getSeed())
                    .regenBiomes(regenBiomes)
                    .build();
                BukkitAdapter.adapt(world).regenerate(region, session, options);
            } catch (MaxChangedBlocksException ignored) {}
        });
    }

    /**
     * Drains the region of water, while also removing the waterlogged state of any blocks.
     */
    public void drain(boolean async) {
        Utils.run(async, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                Map<String, String> states = new HashMap<>();
                states.put("waterlogged", "false");
                StateApplyingPattern pattern = new StateApplyingPattern(new NullExtent(), states);
                BlockTypeMask waterMask = new BlockTypeMask(new NullExtent(), BlockTypes.WATER);
                session.setBlocks(region, pattern);
                session.replaceBlocks(region, waterMask, BlockTypes.AIR);
            } catch (MaxChangedBlocksException ignored) {}
        });
    }

    /**
     * Floods the region with water, while also waterlogging all compatible blocks.
     */
    public void flood(boolean async) {
        Utils.run(async, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                Map<String, String> states = new HashMap<>();
                states.put("waterlogged", "true");
                StateApplyingPattern pattern = new StateApplyingPattern(new NullExtent(), states);
                BlockTypeMask airMask = new BlockTypeMask(new NullExtent(), BlockTypes.AIR);
                session.setBlocks(region, pattern);
                session.replaceBlocks(region, airMask, BlockTypes.WATER);
            } catch (MaxChangedBlocksException ignored) {}
        });
    }

    /**
     * Sets the biome in the region.
     */
    public void setBiome(BiomeType biome, boolean async) {
        Utils.run(async, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                for (BlockVector3 point : region) {
                    session.setBiome(point, biome);
                }
            }
        });
    }

    /**
     * Gets all entities in the region.
     */
    public Entity[] getEntities() {
        return world.getEntities().stream()
            .filter(e -> region.contains(BukkitAdapter.asBlockVector(e.getLocation())))
            .toArray(Entity[]::new);
    }

    /**
     * Makes vanilla-like single snow layers and ice in the region.
     */
    public void simulateSnow(boolean async) {
        Utils.run(async, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                session.simulateSnow(region.getBoundingBox(), true);
            }
        });
    }

    /**
     * Makes smooth snow layers and ice in the region.
     * @param iterations how many smoothing passes (default 1)
     * @param snowBlockCount how many solid snow blocks under the top snow layer (default 1)
     * @param mask optional mask to restrict which blocks are used as the heightmap
     */
    public void simulateSmoothSnow(int iterations, int snowBlockCount, @Nullable MaskWrapper mask, boolean async) {
        Utils.run(async, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                Mask actualMask = mask != null ? mask.mask() : null;
                SnowHeightMap heightMap = new SnowHeightMap(session, region, actualMask);
                HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
                float[] changed = heightMap.applyFilter(filter, iterations);
                heightMap.applyChanges(changed, snowBlockCount);
            }
        });
    }
}