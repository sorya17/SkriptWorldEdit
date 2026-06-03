package me.tecspace.skworldedit.elements.Mask;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import com.sk89q.worldedit.function.mask.Mask;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mask - Replaces Air")
@Description("Whether the mask will replace air.")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class CondMaskReplacesAir extends PropertyCondition<Mask> {

    public static void register(SyntaxRegistry registry) {
        registry.register(
                SyntaxRegistry.CONDITION,
                infoBuilder(
                        CondMaskReplacesAir.class,
                        PropertyType.WILL,
                        "replace air",
                        "worldeditmasks"
                )
                        .supplier(CondMaskReplacesAir::new)
                        .build()
        );
    }

    @Override
    public boolean check(Mask mask) {
        return mask.replacesAir();
    }

    @Override
    protected String getPropertyName() {
        return "replaces air";
    }
}
