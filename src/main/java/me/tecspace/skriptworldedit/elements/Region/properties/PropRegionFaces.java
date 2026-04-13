package me.tecspace.skriptworldedit.elements.Region.properties;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.tecspace.skriptworldedit.api.RegionWrapper;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Region - Faces")
@Description("""
        The faces of a region as a new region.
        Only works on cuboid regions.
        """)
@Example("set {_hollow} to region faces of {_region}")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class PropRegionFaces extends SimplePropertyExpression<RegionWrapper, RegionWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(PropRegionFaces.class, RegionWrapper.class,
                "[region] faces", "worldeditregions", false)
                .supplier(PropRegionFaces::new)
                .build());
    }

    @Override
    public @Nullable RegionWrapper convert(RegionWrapper wrapper) {
        if (wrapper.region() instanceof CuboidRegion cuboid)
            return new RegionWrapper(cuboid.getFaces(), wrapper.world());
        return null;
    }

    @Override
    public Class<? extends RegionWrapper> getReturnType() {
        return RegionWrapper.class;
    }

    @Override
    protected String getPropertyName() {
        return "region faces";
    }
}
