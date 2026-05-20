package me.tecspace.skriptworldedit.elements.Mask;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.fastasyncworldedit.core.extent.NullExtent;
import com.fastasyncworldedit.core.function.mask.SurfaceMask;
import com.sk89q.worldedit.function.mask.Mask;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mask - Surface Mask")
@Description("A simple mask that matches blocks that are exposed to air on at least one side. (FAWE only!)")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprSurfaceMask extends SimpleExpression<Mask> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSurfaceMask.class, Mask.class)
                .supplier(ExprSurfaceMask::new)
                .addPattern("[a] surface mask")
                .build());
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!SkriptWorldEdit.UsesFastAsyncWorldEdit) {
            error("This expression requires the FastAsyncWorldEdit plugin to be installed.");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable Mask[] get(Event event) {
        SurfaceMask mask = new SurfaceMask(new NullExtent());
        return new Mask[]{mask};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Mask> getReturnType() {
        return Mask.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "surface mask";
    }
}
