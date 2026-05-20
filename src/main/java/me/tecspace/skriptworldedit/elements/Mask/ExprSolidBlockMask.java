package me.tecspace.skriptworldedit.elements.Mask;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.fastasyncworldedit.core.extent.NullExtent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mask - Solid Block Mask")
@Description("A simple mask that matches only solid blocks.")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprSolidBlockMask extends SimpleExpression<Mask> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSolidBlockMask.class, Mask.class)
                .supplier(ExprSolidBlockMask::new)
                .addPattern("[a] solid block mask")
                .build());
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    protected @Nullable Mask[] get(Event event) {
        SolidBlockMask mask = new SolidBlockMask(new NullExtent());
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
        return "solid block mask";
    }
}
