package me.tecspace.skriptworldedit.api;

import ch.njol.skript.aliases.ItemType;
import com.fastasyncworldedit.core.extent.NullExtent;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.*;
import com.sk89q.worldedit.world.block.BlockCategory;
import io.papermc.paper.registry.tag.Tag;
import me.tecspace.skriptworldedit.api.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record MaskWrapper(Mask mask) {

    public static final String MASK_SOURCE_TYPES = "%string/itemtypes/blockdatas/minecrafttags/biomes/worldeditregion/worldeditmask%";
    public static final String MASK_SOURCE_TYPES_OPTIONAL = "%-string/itemtypes/blockdatas/minecrafttags/biomes/worldeditregion/worldeditmask%";

    /**
     * Attempts to create a mask from various types,
     * which can be a string, region, mask, biomes, item types, or block data.
     */
    public static @Nullable MaskWrapper from(Object[] sources) {

        if (sources == null || sources.length == 0) return null;

        if (sources.length == 1) {
            if (sources[0] instanceof MaskWrapper wrapper) return wrapper;
            if (sources[0] instanceof Mask mask) return new MaskWrapper(mask);

            if (sources[0] instanceof String s) {
                try {
                    ParserContext context = new ParserContext();
                    context.setActor(BukkitAdapter.adapt(Bukkit.getConsoleSender()));
                    Mask mask = WorldEdit.getInstance().getMaskFactory().parseFromInput(s, context);
                    return new MaskWrapper(mask);
                } catch (InputParseException ignored) {
                    return null;
                }
            }

            if (sources[0] instanceof RegionWrapper region) {
                return new MaskWrapper(new RegionMask(region.region()));
            }
        }

        BlockMask blockMask = new BlockMask(new NullExtent());
        BiomeMask biomeMask = new BiomeMask(new NullExtent());
        List<BlockCategoryMask> categoryMasks = new ArrayList<>();
        boolean containsBiomes = false;
        boolean hasBlocks = false;

        for (Object source : sources) {
            if (source instanceof ItemType item) {
                blockMask.add(BukkitAdapter.asBlockType(item.getMaterial()));
                hasBlocks = true;
            } else if (source instanceof BlockData data) {
                blockMask.add(BukkitAdapter.adapt(data));
                hasBlocks = true;
            } else if (source instanceof Biome biome) {
                containsBiomes = true;
                biomeMask.add(BukkitAdapter.adapt(biome));
            } else if (source instanceof Tag<?> tag && Utils.isMaterialTag(tag)) {
                categoryMasks.add(new BlockCategoryMask(new NullExtent(), (BlockCategory) tag));
            }
        }

        if (!hasBlocks && !containsBiomes && categoryMasks.isEmpty()) return null;

        if (!containsBiomes && categoryMasks.isEmpty()) return new MaskWrapper(blockMask);

        MaskUnion maskUnion = new MaskUnion();
        if (hasBlocks) maskUnion.add(blockMask);
        if (containsBiomes) maskUnion.add(biomeMask);
        if (!categoryMasks.isEmpty()) maskUnion.add(categoryMasks.toArray(new BlockCategoryMask[0]));
        return new MaskWrapper(maskUnion);
    }

    /**
     * Describes the mask in natural language
     */
    public String describe() {
        return "WorldEdit Mask";
    }
}
