package me.tecspace.skriptworldedit.api;

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

public record PatternWrapper(Pattern pattern) {

    public static final String PARSABLE_TYPES_STRING = "%string/itemtypes/blockdatas/worldeditpattern%";
    public static final Class<?>[] PARSABLE_TYPES = {String.class, ItemType[].class, BlockData[].class, PatternWrapper.class};

    /**
     * Attempts to create a {@link PatternWrapper} from various types,
     * which can be a string, pattern, item types, or block data.
     */
    public static @Nullable PatternWrapper from(Object[] sources) {
        if (sources.length == 0) return null;
        if (sources[0] instanceof PatternWrapper wrapper) { return wrapper; }
        if (sources[0] instanceof String s) {
            try {
                ParserContext context = new ParserContext();
                context.setActor(BukkitAdapter.adapt(Bukkit.getConsoleSender()));
                if (!SkriptWorldEdit.UsesFastAsyncWorldEdit) {
                    context.setWorld(BukkitAdapter.adapt(Bukkit.getWorlds().getFirst()));
                    context.setPreferringWildcard(false);
                    context.setRestricted(false);
                }
                Pattern pattern = WorldEdit.getInstance().getPatternFactory().parseFromInput(s, context);
                return new PatternWrapper(pattern);
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
                    pattern.add(blockType.getDefaultState(), 1);
                }
                case BlockData data -> pattern.add(BukkitAdapter.adapt(data), 1);
                case null, default -> {}
            }
        }
        return new PatternWrapper(pattern);
    }

    /**
     * Describes the pattern in natural language lol
     */
    public String describe() {
        return "worldedit pattern";
    }
}
