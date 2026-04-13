package me.tecspace.skriptworldedit.elements.Region.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Copy")
@Description("""
        Copies the contents of a region to another place.
        This is better than creating a clipboard from a region first and pasting it, if you only want to copy the region anyway.
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffCopy extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffCopy.class)
                .supplier(EffCopy::new)
                .addPattern("[:async] copy %worldeditregions% to %locations%")
                .build());
    }

    private Expression<RegionWrapper> regions;
    private Expression<Location> locations;
    private boolean async;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regions = (Expression<RegionWrapper>) expressions[0];
        locations = (Expression<Location>) expressions[1];
        async = parseResult.hasTag("async");
        if (async && !SkriptWorldEdit.UsesFastAsyncWorldEdit) {
            Skript.warning("Async is only supported with FastAsyncWorldEdit. The operation will run synchronously.");
            async = false;
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (regions == null || locations == null) return;
        for (RegionWrapper region : regions.getArray(event)) {
            for (Location location : locations.getArray(event)) {
                region.copy(
                        location,
                        null,
                        false,
                        false,
                        null,
                        null,
                        async
                );
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "async " : "") + "copy " + regions.toString(event, debug) + " to " + locations.toString(event, debug);
    }
}
