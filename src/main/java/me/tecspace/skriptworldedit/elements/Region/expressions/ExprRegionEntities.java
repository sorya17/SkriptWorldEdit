package me.tecspace.skriptworldedit.elements.Region.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Name("Region - Entities")
@Description("Utility expression to get all the entities inside a region.")
@Examples("""
        # we can clear them
        clear region entities in {_region}
        # we can loop them
        for {_e} in region entities in {_region}:
            if {_e} is not a player
            delete entity in {_e}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprRegionEntities extends SimpleExpression<Entity> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, DefaultSyntaxInfos.Expression.builder(ExprRegionEntities.class, Entity.class)
                .supplier(ExprRegionEntities::new)
                .addPattern("region entities (of|in) %worldeditregions%")
                .build()
        );
    }

    private Expression<RegionWrapper> regionExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        regionExpr = (Expression<RegionWrapper>) exprs[0];
        return true;
    }

    @Override
    protected Entity @Nullable [] get(Event event) {
        List<Entity> entities = new ArrayList<>();
        for (RegionWrapper region : regionExpr.getArray(event)) {
            Collections.addAll(entities, region.getEntities());
        }
        return entities.toArray(new Entity[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "region entities of " + regionExpr.toString(event, debug);
    }
}
