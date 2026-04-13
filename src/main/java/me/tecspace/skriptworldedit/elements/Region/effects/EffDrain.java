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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Drain")
@Description("""
        Drains the region of water, while also removing the waterlogged state of any blocks.
        If you want to simply replace water with air you should use the replace effect instead.
""")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class EffDrain extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffDrain.class)
                .supplier(EffDrain::new)
                .addPattern("[:async] drain %worldeditregions%")
                .build());
    }

    private Expression<RegionWrapper> regionExpr;
    private boolean async;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        async = parseResult.hasTag("async");
        if (async && !SkriptWorldEdit.UsesFastAsyncWorldEdit) {
            Skript.warning("Async is only supported with FastAsyncWorldEdit. The operation will run synchronously.");
            async = false;
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        for (RegionWrapper region : regionExpr.getAll(event)) {
            region.drain(async);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (async ? "async " : "") + "drain " + regionExpr.toString(event, debug);
    }
}
