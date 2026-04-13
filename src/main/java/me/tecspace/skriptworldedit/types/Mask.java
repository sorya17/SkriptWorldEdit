package me.tecspace.skriptworldedit.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import me.tecspace.skriptworldedit.api.MaskWrapper;
import org.skriptlang.skript.addon.SkriptAddon;

public class Mask {

    public static void register(SkriptAddon skriptAddon) {
        Classes.registerClass(new ClassInfo<>(MaskWrapper.class, "worldeditmask")
                .user("worldedit ?masks?")
                .name("WorldEdit Mask")
                .description("A WorldEdit Mask")
                .requiredPlugins("WorldEdit")
                .since("1.0")
                .parser(new Parser<>() {
                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(MaskWrapper mask, int flags) {
                        return mask.describe();
                    }

                    @Override
                    public String toVariableNameString(MaskWrapper mask) {
                        return mask.mask().toString();
                    }
                })
        );
    }
}