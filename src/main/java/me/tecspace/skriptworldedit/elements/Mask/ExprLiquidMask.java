package me.tecspace.skriptworldedit.elements.Mask;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.fastasyncworldedit.core.extent.NullExtent;
import com.fastasyncworldedit.core.function.mask.LiquidMask;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import me.tecspace.skriptworldedit.api.MaskWrapper;
import me.tecspace.skriptworldedit.api.utils.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mask - Liquid Mask")
@Description("A simple mask that matches any liquids. (FAWE only!)")
@Example("set {_mask} to a liquid mask")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class ExprLiquidMask extends SimpleExpression<MaskWrapper> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprLiquidMask.class, MaskWrapper.class)
                .supplier(ExprLiquidMask::new)
                .addPattern("[a] liquid mask")
                .build());
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!SkriptWorldEdit.UsesFastAsyncWorldEdit) {
            Utils.SkriptError("This expression requires the FastAsyncWorldEdit plugin to be installed.");
            return false;
        }
        return true;
    }

    @Override
    protected MaskWrapper @Nullable [] get(Event event) {
        LiquidMask mask = new LiquidMask(new NullExtent());
        return new MaskWrapper[]{new MaskWrapper(mask)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends MaskWrapper> getReturnType() {
        return MaskWrapper.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "liquid mask";
    }
}
