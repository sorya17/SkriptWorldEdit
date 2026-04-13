package me.tecspace.skriptworldedit.elements.Region.properties;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import me.tecspace.skriptworldedit.api.PatternWrapper;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Walls")
@Description("The walls of a region as a new region (cuboid regions only).")
@Example("set {_hollow} to walls of {_region}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class PropRegionWalls extends SimplePropertyExpression<RegionWrapper, RegionWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(PropRegionWalls.class, RegionWrapper.class,
                "[region] walls", "worldeditregions", false)
                .supplier(PropRegionWalls::new)
                .build());
    }

    @Override
    public @Nullable RegionWrapper convert(RegionWrapper wrapper) {
        if (wrapper.region() instanceof CuboidRegion cuboid)
            return new RegionWrapper(cuboid.getWalls(), wrapper.world());
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET -> PatternWrapper.PARSABLE_TYPES;
            case DELETE -> new Class[]{};
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
        PatternWrapper pattern;
        switch (mode) {
            case SET -> {
                if (delta == null) return;
                pattern = PatternWrapper.from(delta);
            }
            case DELETE -> pattern = new PatternWrapper(BlockTypes.AIR);
            default -> {
                return;
            }
        }
        RegionWrapper[] regions = getExpr().getAll(event);
        if (regions == null) return;
        for (RegionWrapper wrapper : regions) {
            Region walls = wrapper.region().getBoundingBox().getWalls();
            var region = new RegionWrapper(walls, wrapper.world());
            region.setBlocks(pattern, true);
        }
    }

    @Override
    public Class<? extends RegionWrapper> getReturnType() {
        return RegionWrapper.class;
    }

    @Override
    protected String getPropertyName() {
        return "region walls";
    }
}
