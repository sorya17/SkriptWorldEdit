package me.tecspace.skriptworldedit.elements.Region.properties;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - World")
@Description("The world a region is located in.")
@Example("set {_world} to world of {_region}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class PropRegionWorld extends SimplePropertyExpression<RegionWrapper, World> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(PropRegionWorld.class, World.class,
                "[region] world", "worldeditregions", false)
                .supplier(PropRegionWorld::new)
                .build());
    }

    @Override
    public @Nullable World convert(RegionWrapper wrapper) {
        return BukkitAdapter.adapt(wrapper.world());
    }

    @Override
    protected String getPropertyName() {
        return "region world";
    }

    @Override
    public Class<? extends World> getReturnType() {
        return World.class;
    }
}
