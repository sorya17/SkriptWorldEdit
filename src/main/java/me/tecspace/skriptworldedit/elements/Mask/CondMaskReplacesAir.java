package me.tecspace.skriptworldedit.elements.Mask;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import me.tecspace.skriptworldedit.api.MaskWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mask - Replaces Air")
@Description("Returns whether the mask will replace air.")
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class CondMaskReplacesAir extends PropertyCondition<MaskWrapper> {

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
    public boolean check(MaskWrapper wrapper) {
        return wrapper.mask().replacesAir();
    }

    @Override
    protected String getPropertyName() {
        return "replaces air";
    }
}
