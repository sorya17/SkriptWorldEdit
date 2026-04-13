package me.tecspace.skriptworldedit.elements.Region.properties;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.math.BlockVector2;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Region - Chunks")
@Description("Gets all the chunks overlapping a region.")
@Examples("""
        set {_chunks::*} to overlapping chunks of {_region}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class PropRegionChunks extends PropertyExpression<RegionWrapper, Chunk> {

    public static void register(SyntaxRegistry registry) {
        registry.register(
                SyntaxRegistry.EXPRESSION,
                infoBuilder(
                        PropRegionChunks.class,
                        Chunk.class,
                        "[overlapping] chunks",
                        "worldeditregions",
                        false
                )
                        .supplier(PropRegionChunks::new)
                        .build()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends RegionWrapper>) expressions[0]);
        return true;
    }

    @Override
    protected Chunk[] get(Event event, RegionWrapper[] regions) {
        List<Chunk> chunks = new ArrayList<>();
        for (RegionWrapper region : regions) {
            World world = region.world();
            for (BlockVector2 vector : region.region().getChunks()) {
                chunks.add(world.getChunkAt(vector.x(), vector.z()));
            }
        }
        return chunks.toArray(new Chunk[0]);
    }

    @Override
    public Class<? extends Chunk> getReturnType() {
        return Chunk.class;
    }

    public boolean isSingle() {
        return getExpr().isSingle();
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "";
    }
}
