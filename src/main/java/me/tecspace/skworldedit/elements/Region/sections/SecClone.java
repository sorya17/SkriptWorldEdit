package me.tecspace.skworldedit.elements.Region.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.World;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.RegionWrapper;
import me.tecspace.skworldedit.api.lang.TestAsyncEffect;
import me.tecspace.skworldedit.api.utils.ExprUtils;
import me.tecspace.skworldedit.api.utils.TransformUtils;
import me.tecspace.skworldedit.api.utils.Utils;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Region - Clone")
@Description("""
        Copies the contents of a region to another location directly.
        
        Optional Section Entries:
        - copy entities: (boolean) whether entities should be included. false by default.
        - copy biomes: (boolean) whether biomes should be included. false by default.
        - mask: (mask) a mask of blocks to only change when placing.
        - source mask: (mask) a mask of blocks to only copy from the region.
        - rotation: (number) the rotation across the y-axis at which the build is copied. 0 by default.
        - scale: (number) lets you define how the build should be scaled. none by default.
        - offset: (vector) letting you offset the build placement. none by default.
        - center: (location) the source position from where the region is copied. region's min corner by default.
        
        lazily: Forces synchronous execution (requires fawe).
        and wait: Pauses the skript until the operation finishes. No effect without fawe or alongside 'lazily'.
        """)
@Examples("""
        clone {_region} to location(187,67,420):
            copy entities: true
            copy biomes: false
            mask: {_mask}
            source mask: (inverse mask of diorite)
            rotation: 90
            scale: vector(2,2,2)
            offset: vector(0,5,0)
            center: location(187,67,420)
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class SecClone extends TestAsyncEffect {

    private static EntryValidator VALIDATOR;

    public static void register(SyntaxRegistry registry) {
        VALIDATOR = buildValidator();
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(SecClone.class)
                .supplier(SecClone::new)
                .addPattern("[:lazily] clone [the] region %worldeditregion% to %locations% [:and wait]")
                .build());
    }

    private static EntryValidator buildValidator() {
        EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
        // common entries
        builder.addEntryData(new ExpressionEntryData<>("copy entities", null, true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("copy biomes", null, true, Boolean.class));
        builder.addEntryData(new ExpressionEntryData<>("mask", null, true, Mask.class));
        builder.addEntryData(new ExpressionEntryData<>("source mask", null, true, Mask.class));
        // transformation related entries
        builder.addEntryData(new ExpressionEntryData<>("rotation", null, true, Double.class));
        builder.addEntryData(new ExpressionEntryData<>("scale", null, true, Vector.class));
        builder.addEntryData(new ExpressionEntryData<>("offset", null, true, Vector.class));
        builder.addEntryData(new ExpressionEntryData<>("center", null, true, Location.class));
        return builder.build();
    }

    private Expression<RegionWrapper> regionsExpr;
    private Expression<Location> locationsExpr;

    private @Nullable Expression<Mask> maskExpr, sourceMaskExpr;
    private @Nullable Expression<Boolean> copyEntities, copyBiomes;
    // transformation related entries
    private @Nullable Expression<Double> rotationExpr;
    private @Nullable Expression<Vector> scaleExpr, offsetExpr;
    private @Nullable Expression<Location> centerExpr;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean load(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {

        setAsync(!parseResult.hasTag("lazily") && SkWorldEdit.UsesFastAsyncWorldEdit);
        setDelayed(parseResult.hasTag("and wait") && SkWorldEdit.UsesFastAsyncWorldEdit);

        this.regionsExpr = (Expression<RegionWrapper>) expressions[0];
        this.locationsExpr = (Expression<Location>) expressions[1];

        // section entries
        if (hasSection()) {
            if (sectionNode == null || sectionNode.isEmpty()) return false;
            EntryContainer container = VALIDATOR.validate(sectionNode);
            if (container == null) return false;

            this.copyEntities = (Expression<Boolean>) container.getOptional("copy entities", true);
            this.copyBiomes = (Expression<Boolean>) container.getOptional("copy biomes", true);
            this.maskExpr = (Expression<Mask>) container.getOptional("mask", true);
            this.sourceMaskExpr = (Expression<Mask>) container.getOptional("source mask", true);
            // transformation related entries
            this.rotationExpr = (Expression<Double>) container.getOptional("rotation", true);
            this.scaleExpr = (Expression<Vector>) container.getOptional("scale", true);
            this.offsetExpr = (Expression<Vector>) container.getOptional("offset", true);
            this.centerExpr = (Expression<Location>) container.getOptional("center", true);
        }
        return true;
    }

    @Override
    protected @Nullable Runnable execute(Event event) {

        RegionWrapper region = ExprUtils.getSingle(event, regionsExpr);
        if (region == null) {
            error("No region to copy from was provided.");
            return null;
        }

        Location[] locations = locationsExpr.getArray(event);
        if (locations == null || locations.length == 0) {
            error("No locations to clone the region to were provided.");
            return null;
        }

        // section common entries
        boolean copyEntities = ExprUtils.getSingle(event, this.copyEntities, false);
        boolean copyBiomes = ExprUtils.getSingle(event, this.copyBiomes, false);

        Mask mask = ExprUtils.getSingle(event, maskExpr);
        Mask sourceMask = ExprUtils.getSingle(event, sourceMaskExpr);

        // section transformation related entries
        Double rotation = ExprUtils.getSingle(event, rotationExpr);
        Vector scale = ExprUtils.getSingle(event, scaleExpr);
        Vector offset = ExprUtils.getSingle(event, offsetExpr);
        Location center = ExprUtils.getSingle(event, centerExpr);

        AffineTransform transform = TransformUtils.buildTransform(rotation, scale, offset);

        return () -> {
            BlockVector3 sourcePos = (center != null) ? Utils.toBlockVector3(center) : region.region().getMinimumPoint();
            World world = region.world();
            org.bukkit.World bukkitWorld = BukkitAdapter.adapt(world);
            // locations can each be in a different world, so we need to keep that in mind
            for (Location loc : locations) {
                boolean sameWorld = bukkitWorld.equals(loc.getWorld());
                try (EditSession sourceSession = WorldEdit.getInstance().newEditSession(world);
                     EditSession destSession = sameWorld ? sourceSession : WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(loc.getWorld()))) {

                    if (mask != null) destSession.setMask(mask);

                    ForwardExtentCopy copy = new ForwardExtentCopy(sourceSession, region.region(), sourcePos, destSession, Utils.toBlockVector3(loc));
                    copy.setCopyingBiomes(copyBiomes);
                    copy.setCopyingEntities(copyEntities);
                    if (transform != null) copy.setTransform(transform);
                    if (sourceMask != null) copy.setSourceMask(sourceMask);
                    Operations.complete(copy);
                }
            }
        };
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (isAsync() ? "" : "lazily ") + "clone region " + regionsExpr.toString(event, debug) + " to " + locationsExpr.toString(event, debug) + (isDelayed() ? " and wait" : "");
    }
}
