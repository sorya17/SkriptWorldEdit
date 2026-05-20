package me.tecspace.skriptworldedit.api.utils;

import ch.njol.skript.aliases.ItemType;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.world.block.BlockType;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

public class PatternUtils {

    public static final String PARSABLE_TYPES_STRING = "%string/itemtypes/blockdatas/worldeditpattern%";
    public static final Class<?>[] PARSABLE_TYPES = {String.class, ItemType[].class, BlockData[].class, Pattern.class};

    /**
     * Attempts to create a {@link Pattern} from various types,
     * which can be any of {@link #PARSABLE_TYPES}. null if it failed to parse.
     */
    public static @Nullable Pattern parseFrom(Object[] sources) {
        if (sources.length == 0) return null;
        if (sources[0] instanceof Pattern pattern) return pattern;
        if (sources[0] instanceof String s) {
            try {
                ParserContext context = new ParserContext();
                context.setActor(BukkitAdapter.adapt(Bukkit.getConsoleSender()));
                if (!SkriptWorldEdit.UsesFastAsyncWorldEdit) {
                    context.setWorld(BukkitAdapter.adapt(Bukkit.getWorlds().getFirst()));
                    context.setPreferringWildcard(false);
                    context.setRestricted(false);
                }
                return WorldEdit.getInstance().getPatternFactory().parseFromInput(s, context);
            } catch (InputParseException ignored) {
                return null;
            }
        }
        RandomPattern pattern = new RandomPattern();
        for (Object source : sources) {
            switch (source) {
                //case ItemType item -> pattern.add(BukkitAdapter.asBlockType(item.getMaterial()), 1);
                case ItemType item -> {
                    BlockType blockType = BukkitAdapter.asBlockType(item.getMaterial());
                    assert blockType != null;
                    pattern.add(blockType.getDefaultState(), 1);
                }
                case BlockData data -> pattern.add(BukkitAdapter.adapt(data), 1);
                case null, default -> {}
            }
        }
        return pattern;
    }
}
